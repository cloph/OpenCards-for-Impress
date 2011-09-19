package info.opencards;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import info.opencards.core.CardFile;
import info.opencards.core.backend.BackendFactory;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.oimputils.*;
import info.opencards.oimputils.cardimports.ImportManager;
import info.opencards.ui.CardFilePropsDialog;
import info.opencards.ui.LearnManagerUI;
import info.opencards.ui.actions.AboutAction;
import info.opencards.ui.actions.HelpAction;
import info.opencards.ui.actions.LastMinLearnAction;
import info.opencards.ui.actions.URLAction;
import info.opencards.ui.lastmin.CramLernSettingsPanel;
import info.opencards.util.GlobalExceptionHandler;
import info.opencards.util.InviteTranslatorsDialog;
import info.opencards.util.RedirectedLogger;
import info.opencards.util.globset.AdvancedSettings;
import info.opencards.util.globset.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.BackingStoreException;


/**
 * @noinspection HardCodedStringLiteral
 */
public final class OpenCards extends WeakBase
        implements com.sun.star.lang.XServiceInfo,
        com.sun.star.frame.XDispatchProvider,
        com.sun.star.lang.XInitialization,
        com.sun.star.frame.XDispatch {

    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = OpenCards.class.getName();

    private static final String[] m_serviceNames = {"com.sun.star.frame.ProtocolHandler"};

    private static LearnManagerUI mainUI;

    private static boolean wasConfigured;
    private static boolean isFirstDispatch = true;
    private boolean isFirstFrameDispatch = true;

    private static Map<XComponent, ModeListener> modeListeners = new HashMap<XComponent, ModeListener>();


    public static final String LASTMIN_MODE = "lastMinMode";
    public static final String LTM_MODE = "ltmMode";
    public static final String PRINT_MODE = "printMode";

    private static final java.util.List<String> ocCmds = Arrays.asList("ocHelp", "ltmLearning", "ocLastMinLearning",
            "ocPrefs", "ocAbout", "ocAdvan", "ocRemoveOCProps", "prepPrint", "impCards", "cardRepos", "resetcard",
            "confcard", "selectQuestionShapes", "tagQuestionShapes");


    public OpenCards(XComponentContext context) {
        m_xContext = context;

        if (!wasConfigured) {
            wasConfigured = true;

            Locale ooLocale = OpenOfficeUtils.getLocale(m_xContext);
            Locale.setDefault(ooLocale != null ? ooLocale : java.util.Locale.ENGLISH);

            // increment the startup-counter
            Utils.getPrefs().putInt(Utils.PROP_STARTUP_COUNTER, Utils.getPrefs().getInt(Utils.PROP_STARTUP_COUNTER, 0) + 1);

//            Utils.configureUI(); // note: if this is enabled, the last-min toolbar button is gray on startup

            if (Utils.getPrefs().getBoolean(AdvancedSettings.USE_GLOB_EX_HANDLER, AdvancedSettings.USE_GLOB_EX_HANDLER_DEFAULT)) {
                Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
            }

            if (Utils.getPrefs().getBoolean(AdvancedSettings.WRITE_LOGFILE, AdvancedSettings.WRITE_LOGFILE_DEFAULT)) {
                File logFile = new File(System.getProperty("user.home") + File.separator + ".opencards.log");
                new RedirectedLogger(false, logFile);
                Utils.log("Impress started");
            }

            Utils.configDir = OpenOfficeUtils.getOOUserDirectory(m_xContext);

            // preload all classes for the learn-managers
//            preloadDialogs();
        }
    }


    @Deprecated
    // preloading is no longer necessary because the performance bottleneck was not the ui but the deserialization of the data-models. 
    private void preloadDialogs() {
        new Thread() {

            public void run() {
                super.run();

                XComponent curXComponent = OOoDocumentUtils.getXComponent(m_xContext);
                CardFileBackend backend = BackendFactory.getImpressBackend(new JFrame(), curXComponent, m_xContext);
                Frame owner = backend.getOwner();

                new LearnManagerUI(owner, null);
                new CramLernSettingsPanel(null, backend);
            }
        }.start();
    }


    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName))
            xFactory = Factory.createComponentFactory(OpenCards.class, m_serviceNames);
        return xFactory;
    }


    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                m_serviceNames,
                xRegistryKey);
    }


    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
        return m_implementationName;
    }


    public boolean supportsService(String sService) {
        int len = m_serviceNames.length;

        for (int i = 0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }


    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }


    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String sTargetFrameName, int iSearchFlags) {
        if (aURL.Protocol.compareTo("info.opencards:") == 0 && ocCmds.contains(aURL.Path))
            return this;

        return null;
    }


    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
            com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
                new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for (int i = 0; i < nCount; ++i) {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                    seqDescriptors[i].FrameName,
                    seqDescriptors[i].SearchFlags);
        }
        return seqDispatcher;
    }


    // com.sun.star.lang.XInitialization:
    public void initialize(Object[] object)
            throws com.sun.star.uno.Exception {
        if (object.length > 0) {
            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
                    com.sun.star.frame.XFrame.class, object[0]);

            if (!modeListeners.containsKey(m_xFrame)) {
                modeListeners.put(m_xFrame, new ModeListener());
            }

            SlidePaneCxtMenuInterceptor.install(m_xContext);
        }
    }


    // com.sun.star.frame.XDispatch:
    public void dispatch(com.sun.star.util.URL aURL, com.sun.star.beans.PropertyValue[] aArguments) {
        if (aURL.Protocol.compareTo("info.opencards:") != 0)
            return;

        XComponent curXComponent = OOoDocumentUtils.getXComponent(m_xContext);

        JFrame owner = new JFrame();
        owner.setIconImage(new ImageIcon(Utils.getOCClass().getResource("/resources/icons/oclogo.png")).getImage());

        CardFileBackend backend = BackendFactory.getImpressBackend(owner, curXComponent, m_xContext);

        final com.sun.star.util.URL currentURL = aURL;
        final PropertyValue[] currentArgs = aArguments;
        if (isFirstDispatch) {
            isFirstDispatch = false;
            JDialog translatorsDialog = InviteTranslatorsDialog.inviteForTranslation(m_xContext, owner);
            if (translatorsDialog != null) {
                translatorsDialog.addWindowListener(new WindowAdapter() {

                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        dispatch(currentURL, currentArgs);
                    }
                });

                return;
            }
        }

        if (isFirstFrameDispatch) {
            isFirstFrameDispatch = false;

            // attach dispose-listener to make OOo to flush the OC-preferences on shutDown
            // the better solution Runtime.attachShutDownHook() is currently broken in OOo
            if (Utils.isLinux()) {
                XEventListener evtListener = new XEventListener() {

                    public void disposing(EventObject eventObject) {
                        try {
                            Utils.getPrefs().flush();
                        } catch (BackingStoreException e) {
                            e.printStackTrace();
                        }
                    }
                };

                OOoDocumentUtils.getComponentFrame(OOoDocumentUtils.getXComponent(m_xContext)).addEventListener(evtListener);
            }
        }

        String dispatchCmd = aURL.Path;

        if (!getModeListener().hasOpenModes()) {
//                    JOptionPane.showConfirmDialog(Utils.getOwnerDialog(mainUI), Utils.getRB().getString("OpenCards.noLtmDiag"));

            if (dispatchCmd.compareTo("ltmLearning") == 0) {
                if (mainUI == null) {
                    // maybe we should use a progress bar here, if takes too long to setup opencards
                    mainUI = new LearnManagerUI(owner, backend);
                }

                mainUI.addModeListener(getModeListener());

                // remap the backend to use the current oo-frame from now on
                ((ImpressProxy) mainUI.getBackend().getPresProxy()).setXPointers(curXComponent, m_xContext);
                ((ImpressSerializer) mainUI.getBackend().getSerializer()).setXCompContext(m_xContext);

                OOoDocumentUtils.getComponentFrame(curXComponent).addEventListener(new XEventListener() {
                    public void disposing(EventObject eventObject) {
                        mainUI.setVisible(false);
                    }
                });

//                    XStorable xStorable = cast(XStorable.class, curXComponent);
//                    if (!xStorable.hasLocation() && !NoFileWarnDialog.skipWarning()) {
//                        NoFileWarnDialog noFileWarnDialog = new NoFileWarnDialog(backend, mainUI);
//                        noFileWarnDialog.setVisible(true);
//
//                        return;
//                    }

                if (m_xContext != null) {

                    mainUI.setVisible(true);
                    getModeListener().startedMode(OpenCards.LTM_MODE, mainUI);
                }

                return;
            }

            if (dispatchCmd.compareTo("ocLastMinLearning") == 0) {
                LastMinLearnAction lastMinAction = new LastMinLearnAction(backend);

                CardFile cardFile = ImpressHelper.createCardFile(curXComponent);

                lastMinAction.setCardFile(cardFile);
                lastMinAction.actionPerformed(null, getModeListener());

                return;
            }

            if (dispatchCmd.compareTo("ocRemoveOCProps") == 0) {
                String pleaseConfirmMsg = Utils.getRB().getString("OpenCards.reallyCleanAll");
                String areSureMsg = Utils.getRB().getString("OpenCards.reallyCleanAllTitle");
                int answer = JOptionPane.showConfirmDialog(owner, pleaseConfirmMsg, areSureMsg, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION)
                    ImpressSerializer.cleanUpFile(curXComponent);

                return;
            }

            if (dispatchCmd.compareTo("prepPrint") == 0) {
                final PrintManager printManager = new PrintManager(owner, curXComponent, backend);

                printManager.addWindowListener(new WindowAdapter() {

                    public void windowClosed(WindowEvent e) {
                        getModeListener().stoppedMode(PRINT_MODE);
                    }
                });

                curXComponent.addEventListener(new XEventListener() {
                    public void disposing(EventObject eventObject) {
                        printManager.setVisible(false);
                    }
                });

                getModeListener().startedMode(OpenCards.PRINT_MODE, printManager);
                printManager.setVisible(true);
                return;
            }
        } else {
            if (dispatchCmd.equals("ltmLearning") || dispatchCmd.equals("ocLastMinLearning") ||
                    dispatchCmd.equals("ocRemoveOCProps") || dispatchCmd.equals("prepPrint")) {
                JOptionPane.showConfirmDialog(getModeListener().getCurrentUIParent(), Utils.getRB().getString("OpenCards.blockedDiag"), "OpenCards blocked!", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (dispatchCmd.compareTo("ocHelp") == 0) {
            new HelpAction().actionPerformed(null);
            return;
        }

        if (dispatchCmd.compareTo("cardRepos") == 0) {
            new URLAction(null, "http://flashcards.opencards.info").actionPerformed(null);
            return;
        }


        if (dispatchCmd.compareTo("ocPrefs") == 0) {
            new SettingsDialog(new JDialog(owner)).setVisible(true);
            return;
        }

        if (dispatchCmd.compareTo("impCards") == 0) {
            new ImportManager(owner, curXComponent);
            return;
        }

        if (dispatchCmd.compareTo("ocAbout") == 0) {
            new AboutAction(new Dialog(owner)).actionPerformed(null);
            return;
        }

        if (dispatchCmd.compareTo("resetcard") == 0) {
            ImpressHelper.resetCurSlide(curXComponent, backend);
            return;
        }

        // invoked via the slide configuration dialog only (maybe we should have a menu entry for the file settings itself) 
        if (dispatchCmd.compareTo("confcard") == 0) {
            CardFilePropsDialog.configureSlides(owner, curXComponent, backend);
            return;
        }

        if (dispatchCmd.compareTo("tagQuestionShapes") == 0) {
            XShapeUtils.tagSelectedShapesAsQuestion(ImpressHelper.getCurrentEditorSlide(curXComponent), curXComponent);
            return;
        }

        if (dispatchCmd.compareTo("selectQuestionShapes") == 0) {
            XShapeUtils.selectQuestionShapes(ImpressHelper.getCurrentEditorSlide(curXComponent), curXComponent, m_xContext);
            return;
        }
    }


    private ModeListener getModeListener() {
        return modeListeners.get(m_xFrame);
    }


    public void addStatusListener(com.sun.star.frame.XStatusListener xControl,
                                  com.sun.star.util.URL aURL) {
        // add your own code here
    }


    public void removeStatusListener(com.sun.star.frame.XStatusListener xControl,
                                     com.sun.star.util.URL aURL) {
        // add your own code here
    }

}
