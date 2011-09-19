package info.opencards.oimputils;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPageDuplicator;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XDrawView;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.*;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.util.globset.OOIntegrationSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A <code>PresenterProxy</code> implementation which uses OpenOffice Impress as presentation backend.
 *
 * @author Holger Brandl
 */
public class ImpressProxy implements PresenterProxy, XFrameActionListener {

    protected XComponent xComponent;
    protected XDrawPage curDummySlide;

    public static String OC_TMP_SLIDE = "OpenCards ";
    public static final String TEMP_LEARN_SLIDE = OC_TMP_SLIDE + "tempSlide";

    private List<ProxyEventListener> proxyEventListeners = new ArrayList<ProxyEventListener>();

    //maybe we should attempt to remove this field and extract the context somehow from the XComponent
    private XComponentContext xCompContext;
    private CardFile curCardFile;


    public ImpressProxy(XComponent xComponent, XComponentContext xCompContext) {
        assert xComponent != null;
        this.xComponent = xComponent;
        this.xCompContext = xCompContext;
    }


    public XComponent getXComponent() {
        return xComponent;
    }


    public boolean showCardFront(Item item) {
        //remove the recently added dummy slide
        if (curDummySlide != null) {
            removeLearnSlide();
        }

        if (!doOptionalSync(item))
            return false;

        int cardIndex = item.getFlashCard().getCardIndex();
        ReversePolicy cardRevPolicy = item.getFlashCard().getTodaysRevPolicy();

        return showReversePoliciedCardFront(cardIndex, cardRevPolicy, TEMP_LEARN_SLIDE);
    }


    /**
     * Returns true if the given card-index does not represent a valid flashcard.
     */
    public boolean showReversePoliciedCardFront(int cardIndex, ReversePolicy cardPolicy, String tempSlideName) {

        // note: the random-reverse mode is encoded directly in the flashcard in order to make its policy temporary
        //  persistent during ltm-sessions
        switch (cardPolicy) {
            case NORMAL:
                showCardQuestion(cardIndex, tempSlideName);
                break;
            case REVERSE:
                showCardContent(cardIndex, tempSlideName);
                break;
            default:
                throw new RuntimeException("unsupported reverse policy");
        }

        return true;
    }


    protected boolean showCardQuestion(int cardIndex, String tempSlideName) {
        XDrawPage dupSlide = duplicateSlide(cardIndex);

        // this is just a hack in order to find such slides in cases that a file was closed before this slide was removed
        ImpressHelper.cast(XNamed.class, dupSlide).setName(tempSlideName);

        DrawPageHelper.retryCounter = 0;
        DrawPageHelper.removeAllButTitleShape(dupSlide);
        setCurrentSlide(dupSlide, getXComponent());

//        //set auto-layout to use "Title Only"-layout
//        XPropertySet xPropSet = cast(XPropertySet.class, dupSlide);
//        xPropSet.setPropertyValue("Layout", 19); // 19==Title Only

        return true;
    }


    protected boolean showCardContent(int cardIndex, String slideName) {
        XDrawPage dupSlide = duplicateSlide(cardIndex);

        XText xText = cast(XText.class, DrawPageHelper.getTitleShape(dupSlide));
        if (xText != null)
            xText.setString("");

        ImpressHelper.cast(XNamed.class, dupSlide).setName(slideName);
        setCurrentSlide(dupSlide, getXComponent());

        return true;
    }


    public boolean showCompleteCard(Item item) {
        removeLearnSlide();

        if (!doOptionalSync(item)) {
            return false;
        }

        // select the next drawpage to be shown
        XDrawPage nextPage;
        try {
            nextPage = ImpressHelper.getDrawPageByIndex(getXComponent(), item.getFlashCard().getCardIndex());
            setCurrentSlide(nextPage, getXComponent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    /**
     * Performs a sync operation on the current <code>CardFile</code> if the the item to be shown directs to an
     * XDrawPage which oc-cardID differs from the flashcard associated to the item.
     *
     * @return <code>true</code> if the syncing was sucessful. A non-sucessfull sync is always due to the fact that a
     *         slide was deleted.
     */
    private boolean doOptionalSync(Item item) {
        if (item == null || curCardFile == null) {
            Utils.log("Error: Synching not possible because either item (" + item + ") or card-file are null");
            return false;
        }

        XDrawPage itemPage = ImpressHelper.getDrawPageByIndex(getXComponent(), item.getFlashCard().getCardIndex());
        Integer pageID = itemPage == null ? null : DrawPageHelper.readDrawPageCustomProperty(itemPage);
        if (pageID == null || pageID != item.getFlashCard().getCardID())
            curCardFile.synchronize();

        // now test whether syncing was sucessfull
        itemPage = ImpressHelper.getDrawPageByIndex(getXComponent(), item.getFlashCard().getCardIndex());
        pageID = itemPage == null ? null : DrawPageHelper.readDrawPageCustomProperty(itemPage);
        return pageID != null && pageID == item.getFlashCard().getCardID();
    }


    public static void setCurrentSlide(XDrawPage slide, XComponent xComponent) {
        XModel x = ImpressHelper.cast(XModel.class, xComponent);
        XController xco = x.getCurrentController();
        XDrawView xdv = ImpressHelper.cast(XDrawView.class, xco);
        xdv.setCurrentPage(slide);
    }


    protected XDrawPage duplicateSlide(int slideIndex) {
        XDrawPage nextPage = ImpressHelper.getDrawPageByIndex(getXComponent(), slideIndex);

        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, getXComponent());
        XDrawPageDuplicator xDrawPageDuplicator = cast(XDrawPageDuplicator.class, xDrawPagesSupplier);
        curDummySlide = xDrawPageDuplicator.duplicate(nextPage);

        return curDummySlide;
    }


    private void removeLearnSlide() {
        if (curDummySlide != null) {
            boolean isLastTemp = ImpressHelper.cast(XNamed.class, ImpressHelper.getDrawPageByIndex(getXComponent(), 0)).getName().equals(TEMP_LEARN_SLIDE);
            if (ImpressHelper.numSlides(getXComponent()) == 1 && isLastTemp) {
                try {
                    ImpressHelper.insertNewDrawPageByIndex(getXComponent(), 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ImpressHelper.removeDrawPage(getXComponent(), curDummySlide);
            curDummySlide = null;
        }
    }


    public boolean writeMetaData(String propName, String propValue) {
        return OpenOfficeUtils.writeDocumentProperty(getXComponent(), propName, propValue);
    }


    public String readMetaData(String propName) {
        return (String) OpenOfficeUtils.readDocumentProperty(getXComponent(), propName);
    }


    public void startFileSession(ItemCollection cardItemCollection) {
        Utils.log("ImProx: attaching frame-listener");
        OOoDocumentUtils.getComponentFrame(xComponent).addFrameActionListener(this);
        Utils.log("ImProx: attaching event-listener");
        OOoDocumentUtils.getComponentFrame(xComponent).addEventListener(this);

        // optionally hide slide sorter
        showEditModeSideBar(false);
    }


    private void showEditModeSideBar(boolean doShow) {
        Boolean defValue = OOIntegrationSettings.DO_HIDE_SLIDESORTER_DEFAULT;
        boolean isInPresentationMode = this instanceof PresenterModeProxy && ((PresenterModeProxy) this).isUseFullScreenMode();
        boolean hideSidePaneForLearning = Utils.getPrefs().getBoolean(OOIntegrationSettings.DO_HIDE_SLIDESORTER, defValue);

        if (hideSidePaneForLearning && !isInPresentationMode) {
            Utils.log("ImProx: disable slide-pane");
            ImpressHelper.showSlidePane(doShow, xCompContext, xComponent);
        }
    }


    public void stopFileSession() {
        OOoDocumentUtils.getComponentFrame(xComponent).removeEventListener(this);
        OOoDocumentUtils.getComponentFrame(xComponent).removeFrameActionListener(this);

        removeLearnSlide();

        // set the current file to null
        curCardFile = null;
    }


    public CardFile getCurCardFile() {
        return curCardFile;
    }


    public void stopLearnSession() {
        Utils.log("ImpProx: stopLearnSession");

        // optionally unhide slide sorter
        showEditModeSideBar(true);
    }


    public void openCardFile(CardFile cardFile) {
        curCardFile = cardFile;
        if (cardFile.getFileLocation() == null) {
            // running in memory mode
            return;
        }

        String fileURL = Utils.convert2OOurl(cardFile.getFileLocation());

        XComponent xC = getXComponent();
        assert xC != null;

        XComponent cardFileComponent = OOoDocumentUtils.getOpenXDocument(cardFile, xCompContext);
        if (cardFileComponent != null) {
            // the document is already open
            if (!OOoDocumentUtils.isCurrentDocument(getXComponent(), cardFile.getFileLocation())) {
                XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, cardFileComponent);
                XController xController = xModel.getCurrentController();
                XFrame frame = xController.getFrame();
                xComponent = cardFileComponent;
                frame.activate();
                frame.getComponentWindow().setFocus();
            }
        } else {
            xComponent = OOoDocumentUtils.openDocumentInSameFrame(xC, fileURL);
        }
    }


    /**
     * Adds a new listener.
     */
    public void addProxyEventListener(ProxyEventListener l) {
        if (l == null)
            return;

        proxyEventListeners.add(l);
    }


    /**
     * Removes a listener.
     */
    public void removeProxyEventListener(ProxyEventListener l) {
        if (l == null)
            return;

        proxyEventListeners.remove(l);
    }


    public void frameAction(FrameActionEvent frameActionEvent) {
//        System.out.println("frame action occured");

    }


    public void disposing(EventObject eventObject) {
        for (ProxyEventListener eventListener : proxyEventListeners) {
            eventListener.proxyDisposed();
        }
    }


    /**
     * Allows to debug slide-pane hiding
     */
    public static void main(String[] args) throws BootstrapException {
        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");
        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
        xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

//        XFrame xFrame = OOoDocumentUtils.getComponentFrame(xComponent);
        ImpressProxy impressProxy = new ImpressProxy(xComponent, xContext);

//        impressProxy.openCardFile(new CardFile(new File("testdata/testpres.odp")));

        impressProxy.showEditModeSideBar(false);

        System.err.println("test");


    }


    public void setXPointers(XComponent curXComponent, XComponentContext xContext) {
        this.xComponent = curXComponent;
        this.xCompContext = xContext;
    }
}
