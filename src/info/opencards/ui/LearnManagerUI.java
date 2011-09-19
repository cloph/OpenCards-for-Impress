/*
 * Created by JFormDesigner on Wed Aug 29 22:40:03 CEST 2007
 */

package info.opencards.ui;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.ModeListener;
import info.opencards.OpenCards;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.Item;
import info.opencards.core.backend.BackendFactory;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.core.categories.Category;
import info.opencards.core.categories.CategoryUtils;
import info.opencards.learnstrats.ltm.ScheduleUtils;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OdpAutoDiscovery;
import info.opencards.ui.actions.HelpAction;
import info.opencards.ui.catui.CategoryPanel;
import info.opencards.ui.catui.CategoryTree;
import info.opencards.ui.ltmstats.LongTermMemStatsPanel;
import info.opencards.ui.table.CardSetTable;
import info.opencards.ui.table.CardTableModel;
import info.opencards.ui.table.FilePopUp;
import info.opencards.ui.table.StartLearningAction;
import info.opencards.util.LayoutRestorer;
import info.opencards.util.globset.SettingsDialog;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * @author Holger Brandl
 */
public class LearnManagerUI extends JDialog implements CardFileSelectionListener {

    private CardFileBackend backend;

    private List<ModeListener> modeListeners = new ArrayList<ModeListener>();
    public CurFileSelectionManager selectionManager;

    private boolean isShiftPressed;
    private boolean isCtrlPressed;


    public LearnManagerUI(Frame owner, CardFileBackend backend) {
        super(owner);
        this.backend = backend;

        setAlwaysOnTop(true);
        initComponents();

        // serialize and restore split spane location changes
        restoreContentLayout();

        // setup cardfile-contents
        if (backend == null)
            return;

        selectionManager = new CurFileSelectionManager(cardfileTable);
        selectionManager.addCardFileSelectionListener(ltmStatsPanel);

        Category rootCat = CategoryUtils.deserializeCategoryModel(this);

//        categoryPanel.getCatTree().setRootCategory(null);
        final CategoryTree catTree = categoryPanel.getCatTree();
        catTree.setRootCategory(rootCat);
        catTree.setOwner(owner);

        cardfileTable.addMouseListener(new FilePopUp(cardfileTable, backend));
        cardfileTable.getSelectionModel().addListSelectionListener(selectionManager);

        // this listener needs to process selection events frist in order to set the Serializer
        CardFilesPreloader preLoader = new CardFilesPreloader(this, backend.getSerializer());
        catTree.addCardFilesSelectionListener(preLoader);

        catTree.addCardFilesSelectionListener((CardTableModel) cardfileTable.getModel());
        catTree.getRootCategory().addCategoryChangeListener(((CardTableModel) cardfileTable.getModel()).getCatListener());

        catTree.addCardFilesSelectionListener(selectionManager);

        helpButton.setAction(new HelpAction());

        final StartLearningAction startLearningAction = new StartLearningAction(this, backend);
        selectionManager.addCardFileSelectionListener(startLearningAction);
        selectionManager.addCardFileSelectionListener(this);

        numNewExceedLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        numNewExceedLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    // open the preferences for the long-term learning
                    SettingsDialog dialog = new SettingsDialog(LearnManagerUI.this);
                    dialog.setActiveSettingsPanel(1);
                    dialog.setModal(true);
                    dialog.setVisible(true);

                    refreshFileViews();
                }
            }
        });

        startLearnButton.setAction(startLearningAction);

        // this will update the all selection-listeners
        catTree.refire(catTree.getPathForRow(0));
//        cardsSplitPanel.setDividerLocation(0.6);

        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                hideLearnManager();
            }
        });

        Utils.actionOnEsc(this, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hideLearnManager();
            }
        });

        Utils.helpOnF1(this, null);

        // register for shift-pressings
        setupModifierKeyProcessing(startLearningAction);


        addWindowListener(new WindowAdapter() {

            public void windowGainedFocus(WindowEvent e) {
                setIgnoreRepaint(false);
            }
        });

        //note this shouldn't be necessary because the tree becomes always serialized when the dialog is closed
//        CategoryChangeAdapter catSerializer = new AutoCategorySerializer(categoryPanel.getCatTree().getRootCategory());
//        categoryPanel.getCatTree().getRootCategory().addCategoryChangeListener(catSerializer);
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration().getBounds();
        setBounds(LayoutRestorer.getInstance().getBounds("catUI", this, new Rectangle((int) (screenBounds.getWidth() / 2. - 400), (int) (screenBounds.getHeight() / 2. - 350), 800, 700)));
        setLocationRelativeTo(null);
    }


    /**
     * serialize and restore split spane location changes.
     */
    private void restoreContentLayout() {
        final String contentPaneDividerID = "contentPaneSplitLoc";

        contentSplitPanel.addPropertyChangeListener(new PropertyChangeListener() {


            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                    Utils.getPrefs().putInt(contentPaneDividerID, contentSplitPanel.getDividerLocation());
                }
            }
        });

        final String cardfilePaneDividerID = "cardfilePaneSplitLoc";
        cardsSplitPanel.addPropertyChangeListener(new PropertyChangeListener() {


            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                    Utils.getPrefs().putInt(cardfilePaneDividerID, cardsSplitPanel.getDividerLocation());
                }
            }
        });

        // restore divider locations
        contentSplitPanel.setDividerLocation(Utils.getPrefs().getInt(contentPaneDividerID, 180));
        cardsSplitPanel.setDividerLocation(Utils.getPrefs().getInt(cardfilePaneDividerID, 399));
    }


    private void setupModifierKeyProcessing(final StartLearningAction startLearningAction) {
        KeyStroke altDown = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, KeyEvent.SHIFT_DOWN_MASK, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altDown, "ALTDOWN");
        getRootPane().getActionMap().put("ALTDOWN", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                isShiftPressed = true;
                isCtrlPressed = false;
                startLearningAction.updateModKeyState();
            }
        });

        KeyStroke altUp = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altUp, "ALTUP");

        getRootPane().getActionMap().put("ALTUP", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                isShiftPressed = false;
                isCtrlPressed = false;
                startLearningAction.updateModKeyState();
            }
        });

        KeyStroke ctrlAltDown = KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, KeyEvent.CTRL_DOWN_MASK, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlAltDown, "CTRLDOWN");
        getRootPane().getActionMap().put("CTRLDOWN", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                isCtrlPressed = true;
                isShiftPressed = false;
                startLearningAction.updateModKeyState();
            }
        });

        KeyStroke ctrlAltUp = KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlAltUp, "CTRLUP");

        getRootPane().getActionMap().put("CTRLUP", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                isCtrlPressed = false;
                isShiftPressed = false;
                startLearningAction.updateModKeyState();
            }
        });
    }


    public CardFileBackend getBackend() {
        return backend;
    }


    public CardSetTable getCardfileTable() {
        return cardfileTable;
    }


    public void refreshFileViews() {
        categoryPanel.getCatTree().informCardFileSelectionListeners();
    }


    public void hideLearnManager() {

        setVisible(false);


        Category rootCategory = categoryPanel.getCatTree().getRootCategory();
        if (!isVisible()) {
            CategoryUtils.serializeCategoryModel(rootCategory);

            for (ModeListener modeListener : modeListeners) {
                modeListener.stoppedMode(OpenCards.LTM_MODE);
            }
        }

    }


    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);

//        ((CardTableModel) cardfileTable.getModel()).refreshTableData();
//        ltmStatsPanel.categorySelectionChanged(null, new HashSet<Category>());
        selectionManager.refireLastSelection();

        OdpAutoDiscovery.run(this, categoryPanel.getCatTree().getRootCategory(), this.getBackend().getSerializer());

    }


    /**
     * Adds a new listener.
     */
    public void addModeListener(ModeListener l) {
        if (l == null)
            return;

        modeListeners.add(l);
    }


    /**
     * Removes a listener.
     */
    public void removeModeListener(ModeListener l) {
        if (l == null)
            return;

        modeListeners.remove(l);
    }


    private void closeButtonActionPerformed() {
        hideLearnManager();
    }


    public void cardFileSelectionChanged(List<CardFile> curSelCardFiles) {
        Map<CardFile, List<Item>> fileListMap = ((CardTableModel) cardfileTable.getModel()).getDummyLtmManager().getNewButNotScheduledItems();

        int numNotSchedItems = 0;
        for (CardFile cardFile : fileListMap.keySet()) {
            numNotSchedItems += fileListMap.get(cardFile).size();
        }

        if (numNotSchedItems > 0) {
            int numMaxNewPerDay = ScheduleUtils.getMaxNewCardsPerDay();

            String msg = Utils.getRB().getString("LearnManagerUI.numNewExceeded");
            msg = msg.replace("MAX_ITEMS_PER_DAY", "<font color=blue>" + numMaxNewPerDay + "</font>");
            msg = msg.replace("NUM_NOT_SCHED_ITEMS", numNotSchedItems + "");

            numNewExceedLabel.setText("<html>" + msg + "</html>");
            panel1.add(numNewExceedLabel, BorderLayout.SOUTH);
            panel1.validate();
        } else {
            numNewExceedLabel.setText("");
            panel1.remove(numNewExceedLabel);
            panel1.validate();
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = info.opencards.Utils.getRB();
        JPanel dialogPane = new JPanel();
        JPanel buttonBar = new JPanel();
        helpButton = new JButton();
        JButton closeButton = new JButton();
        startLearnButton = new JButton();
        contentSplitPanel = new JSplitPane();
        categoryPanel = new CategoryPanel();
        JPanel contentPanel = new JPanel();
        cardsSplitPanel = new JSplitPane();
        JScrollPane scrollPane = new JScrollPane();
        cardfileTable = new CardSetTable();
        panel1 = new JPanel();
        ltmStatsPanel = new LongTermMemStatsPanel();
        useFullScreenCheckbox = new JCheckBox();
        numNewExceedLabel = new JLabel();

        //======== this ========
        setTitle(bundle.getString("LearnManagerUI.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{84, 0, 0, 0, 0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0};

                //---- helpButton ----
                helpButton.setText(bundle.getString("General.help"));
                buttonBar.add(helpButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- closeButton ----
                closeButton.setText(bundle.getString("General.close"));
                closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        closeButtonActionPerformed();
                    }
                });
                buttonBar.add(closeButton, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- startLearnButton ----
                startLearnButton.setText(bundle.getString("StartLearningAction.StartLearning"));
                buttonBar.add(startLearnButton, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== contentSplitPanel ========
            {
                contentSplitPanel.setDividerSize(4);
                contentSplitPanel.setDividerLocation(180);
                contentSplitPanel.setBorder(null);
                contentSplitPanel.setResizeWeight(0.2);

                //---- categoryPanel ----
                categoryPanel.setPreferredSize(new Dimension(180, 324));
                categoryPanel.setBorder(new CompoundBorder(
                        new TitledBorder(null, bundle.getString("LearnManagerUI.categoryPanel.border"), TitledBorder.LEADING, TitledBorder.TOP),
                        new EmptyBorder(1, 2, 2, 2)));
                categoryPanel.setBackground(null);
                contentSplitPanel.setLeftComponent(categoryPanel);

                //======== contentPanel ========
                {
                    contentPanel.setBorder(null);
                    contentPanel.setLayout(new BorderLayout());

                    //======== cardsSplitPanel ========
                    {
                        cardsSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
                        cardsSplitPanel.setDividerSize(4);
                        cardsSplitPanel.setBorder(new EmptyBorder(0, 2, 0, 0));
                        cardsSplitPanel.setResizeWeight(0.8);

                        //======== scrollPane ========
                        {
                            scrollPane.setPreferredSize(null);
                            scrollPane.setMaximumSize(null);
                            scrollPane.setMinimumSize(null);
                            scrollPane.setViewportView(cardfileTable);
                        }
                        cardsSplitPanel.setTopComponent(scrollPane);

                        //======== panel1 ========
                        {
                            panel1.setLayout(new BorderLayout());

                            //======== ltmStatsPanel ========
                            {
                                ltmStatsPanel.setPreferredSize(new Dimension(140, 140));
                                ltmStatsPanel.setMinimumSize(new Dimension(100, 100));
                            }
                            panel1.add(ltmStatsPanel, BorderLayout.CENTER);
                        }
                        cardsSplitPanel.setBottomComponent(panel1);
                    }
                    contentPanel.add(cardsSplitPanel, BorderLayout.CENTER);
                }
                contentSplitPanel.setRightComponent(contentPanel);
            }
            dialogPane.add(contentSplitPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setLocationRelativeTo(null);

        //---- useFullScreenCheckbox ----
        useFullScreenCheckbox.setText(bundle.getString("LearnManagerUI.this.fullscreen"));

        //---- numNewExceedLabel ----
        numNewExceedLabel.setBorder(new TitledBorder(""));
        numNewExceedLabel.setFont(numNewExceedLabel.getFont().deriveFont(numNewExceedLabel.getFont().getStyle() & ~Font.BOLD));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JButton helpButton;
    private JButton startLearnButton;
    private JSplitPane contentSplitPanel;
    private CategoryPanel categoryPanel;
    private JSplitPane cardsSplitPanel;
    private CardSetTable cardfileTable;
    private JPanel panel1;
    private LongTermMemStatsPanel ltmStatsPanel;
    private JCheckBox useFullScreenCheckbox;
    private JLabel numNewExceedLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static void main(String[] args) throws BootstrapException {
        Utils.configureUI();
        JFrame owner = new JFrame();
        owner.setLocationRelativeTo(null);

        owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        owner.setVisible(true);

//        CardFileBackend.createMockInstance(owner, new MockSupplier());

        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");
        CardFileBackend backend = BackendFactory.getImpressBackend(owner, xComponent, xContext);

        new LearnManagerUI(owner, backend).setVisible(true);

//        System.out.println("test");
//        java.util.List<CardFile> cardFiles = Arrays.asList(new CardFile(new File("testdata/testpres2.odp")));
//        new StartLearningAction(null, null).learnCardFiles(new ArrayList<CardFile>(cardFiles));

//        CardFileBackend.getInstance().getLearnManager().setVisible(true);
    }


    public boolean isShiftPressed() {
        return isShiftPressed;
    }


    public boolean isCtrlPressed() {
        return isCtrlPressed;
    }
}