package info.opencards.oimputils;

import com.sun.star.awt.Size;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.*;
import com.sun.star.drawing.framework.*;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.*;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.*;
import info.opencards.core.backend.CardFileBackend;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;


/**
 * Some small helper methods which define an easy control API to Impress.
 *
 * @author Holger Brandl
 */
public class ImpressHelper {


    /**
     * Selects a the slides which corresponds to the given flashcard in impress.
     */
    public static void selectSlideInImpress(Item selectedValue, XComponent xComponent) throws Exception {

        // select the drawpage to show
        int itemIndex = selectedValue.getFlashCard().getCardIndex();
        XDrawPage nextPage = getDrawPageByIndex(xComponent, itemIndex);

        if (nextPage == null)
            System.err.println("Next page is NULL !!");

        XModel x = cast(XModel.class, xComponent);
        XController xco = x.getCurrentController();
        XDrawView xdv = cast(XDrawView.class, xco);

        xdv.setCurrentPage(nextPage);
    }


    /**
     * Simplify the query of uno interfaces.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T cast(Class<T> c, Object o) {
        return (T) UnoRuntime.queryInterface(c, o);
    }


    static FlashCardCollection getPresenterCards(XComponent xDrawComponent, int retryCounter) {
        FlashCardCollection cardCollection = new FlashCardCollection();
        Utils.log("extracting presenter cards...");

        try {
            Object documentSupplier = cast(XDrawPagesSupplier.class, xDrawComponent);
            Utils.log("doc supplier is : " + documentSupplier.toString());
            if (!(documentSupplier instanceof XDrawPagesSupplier))
                return cardCollection;

            XDrawPagesSupplier xDrawPagesSupplier = (XDrawPagesSupplier) documentSupplier;
            XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
            Utils.log("the number of xdraw-pages is : " + xDrawPages.getCount());

            removeTempSlides(xDrawComponent); // do we really need this one
            Utils.log("removed tmp-slide; start to iterate over xdrawPages");

            for (int i = 0; i < xDrawPages.getCount(); i++) {
                Utils.log(Level.FINER, "processing slide no" + i);
                XDrawPage xDrawPage = cast(XDrawPage.class, xDrawPages.getByIndex(i));


                // skip the slide if it is a temporary OpenCards slides (just a hack to overcome failed deleitions)
                if (ImpressHelper.cast(XNamed.class, xDrawPage).getName().startsWith(ImpressProxy.OC_TMP_SLIDE))
                    continue;

                // skip the slide if it does not contain a question and an answer
                if (XShapeUtils.getQuestionShapes(xDrawPage).isEmpty() || XShapeUtils.getAnswerShapes(xDrawPage).isEmpty())
                    continue;

                String slideTitle = DrawPageHelper.getSlideTitle(xDrawPage);

                if (slideTitle != null) {
                    slideTitle = ImpressSerializer.DEFAULT_FLASHCARD_TITLE;
                }

                Utils.log(Level.FINER, "generating flashcard for : " + slideTitle);
                FlashCard card = new FlashCard(getOrGenerateID(xDrawPage, cardCollection), slideTitle, i);

                // because we don't want to hack around bug ??? (copy via drag) we check for
                // duplicate ids here and abort if necessary; we can check using contains because
                // equals is based on id only.
                assert !cardCollection.contains(card) : "duplicate ID; please vote for bug ??? at openoffice.org";
                if (cardCollection.contains(card)) {
                    Utils.log("found duplicate entry : " + card.toString() + ". creating new unique id ");

                    //  we fix doubled slide id's here before the exception becomes thrown immediately in the next step when adding the item
                    card = new FlashCard(createNewSlideID(xDrawPage, cardCollection), slideTitle, i);
                }

                cardCollection.add(card);
            }
        } catch (DisposedException e) {
            // just try again if we have not tried this is without any change in OOo response before
            if (retryCounter > 0) {
                Utils.log("DisposedException catched while collecting file-items. Restarting procedure");

                // wait a few milliseconds
                Utils.sleep(50);

                // just try it again
                return getPresenterCards(xDrawComponent, retryCounter - 1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return cardCollection;
    }


    // this is rather a hack around the bug of OOo to return from blocking method although OpenOffice is still busy
    // (e.g. after switching from one file to another
    public static final int MAX_GET_SLIDES_RETRY = 3;


    public static void removeTempSlides(XComponent xDrawComponent) throws com.sun.star.lang.IndexOutOfBoundsException, WrappedTargetException {
        Object documentSupplier = cast(XDrawPagesSupplier.class, xDrawComponent);

        XDrawPagesSupplier xDrawPagesSupplier = (XDrawPagesSupplier) documentSupplier;
        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

        for (int i = 0; i < xDrawPages.getCount(); i++) {
            XDrawPage xDrawPage = cast(XDrawPage.class, xDrawPages.getByIndex(i));

            // this just removed not-removed temporary slides
            if (ImpressHelper.cast(XNamed.class, xDrawPage).getName().startsWith(ImpressProxy.OC_TMP_SLIDE)) {
                xDrawPages.remove(xDrawPage);
                i--;
                continue;
            }
        }
    }


    /**
     * Reads the opencards-ID of an XDrawPage. If it has no ID yet a positive random number will be generated. This
     * number will be new with respect to the IDs of the item in the argement <code>cardCollection</code>.
     */
    public static int getOrGenerateID(XDrawPage xDrawPage, FlashCardCollection cardCollection) {
        Integer slideID = DrawPageHelper.readDrawPageCustomProperty(xDrawPage);

        if (slideID != null) {
            return slideID;
        } else {
            return createNewSlideID(xDrawPage, cardCollection);
        }
    }


    public static int createNewSlideID(XDrawPage xDrawPage, FlashCardCollection cardCollection) {
        Integer slideID;
        slideID = Utils.getRandGen().nextInt(Integer.MAX_VALUE);

        // ensure that the card id is unique with respect to the existing flashcard-items
        while (cardCollection.getByID(slideID) != null) {
            slideID = Utils.getRandGen().nextInt(Integer.MAX_VALUE);
        }

        DrawPageHelper.writeDrawPageCustomProperty(xDrawPage, slideID);

        return slideID;
    }


    /**
     * Get the page count for standard pages
     */
    public static int numSlides(XComponent xComponent) {
        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

        return xDrawPages.getCount();
    }


    /**
     * Returns the current slide from a open presenation
     */
    public static XDrawPage getCurrentEditorSlide(XComponent xComponent) {
        XModel x = cast(XModel.class, xComponent);

        XController xco = x.getCurrentController();
        XDrawView xdv = cast(XDrawView.class, xco);

        return xdv.getCurrentPage();
    }


    /**
     * get draw page by index
     */
    static public XDrawPage getDrawPageByIndex(XComponent xComponent, int slideIndex) {
        try {
            XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);
            XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

            if (slideIndex >= xDrawPages.getCount()) {
                Utils.log("request for out of bounds XDrawPage failed (" + slideIndex + ")");
                return null;
            }

            Utils.log(Level.FINEST, "numSlides: " + numSlides(xComponent) + " getIndex" + slideIndex);
            return cast(XDrawPage.class, xDrawPages.getByIndex(slideIndex));

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * creates and inserts a draw page into the giving position, the method returns the new created page
     */
    static public XDrawPage insertNewDrawPageByIndex(XComponent xComponent, int nIndex) throws Exception {
        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
        return xDrawPages.insertNewByIndex(nIndex);

//         example
//        try {
//            // add a new dummy slide which will be used for showing the card fronts (aka slide titles)
//            int numSlides = ImpressHelper.numSlides(getXComponent());
//            dummyLearnSlide = DrawPageHelper.insertNewDrawPageByIndex(getXComponent(), numSlides);
//            cardFrontShape = DrawPageHelper.createTitleShape(dummyLearnSlide, getXComponent());
//            cast(XText.class, cardFrontShape).setString("ttt");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


    /**
     * removes the given page
     */
    static public void removeDrawPage(XComponent xComponent, XDrawPage xDrawPage) {
        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
        xDrawPages.remove(xDrawPage);
    }


    public static CardFile createCardFile(XComponent curXComponent) {
        CardFile cardFile = null;
        XStorable xStorable = cast(XStorable.class, curXComponent);
        if (xStorable.hasLocation()) {
            try {
                cardFile = CardFileCache.getCardFile(new File(new URL(xStorable.getLocation()).getFile()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else
            cardFile = new CardFile(null);
        return cardFile;
    }


    public static void resetCurSlide(XComponent curXComponent, CardFileBackend backend) {
        XDrawPage xDrawPage = getCurrentEditorSlide(curXComponent);

        CardFile cardFile = createCardFile(curXComponent);
        cardFile.setSerializer(backend.getSerializer());

        cardFile.synchronize();

        FlashCardCollection cardCollection = cardFile.getFlashCards();
        int selectedCardID = getOrGenerateID(xDrawPage, cardCollection);
        FlashCard selectedFlashCard = cardCollection.getByID(selectedCardID);

        for (Item item : cardCollection.getCardItems(selectedFlashCard)) {
            item.reset();
        }

        // make the changes persistent
        cardFile.flush();
    }


    /**
     * Make the slide sorter pane on the left side visible/invisible.
     */
    public static void showSlidePane(boolean doShow, final XComponentContext xContext, XComponent xComponent) {

        XModel x = cast(XModel.class, xComponent);
        XController xco = x.getCurrentController();

        XControllerManager xCM = cast(XControllerManager.class, xco);
        final XConfigurationController xCC = xCM.getConfigurationController();

        // this strang hack is necceary because the API of OO has changed between 2.x and 3.x
        final String paner = OpenOfficeUtils.getOOoVersion(xContext).startsWith("3.") ? "pane" : "floater";

        // wait some  time to avoid crashes due to still running oo-file-loading processes.
//        try {
//            Thread.sleep(250);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        XResourceId tasksID = ResourceId.createWithAnchorURL(xContext, "private:resource/view/TaskPane", "private:resource/" + paner + "/RightPane");
//        XResourceId rightPaneID = ResourceId.create(xContext, "private:resource/" + paner + "/RightPane");

        // get all resource ids of the impress
//        xCC.getCurrentConfiguration().getResources(ResourceId.createEmpty(xContext), "", AnchorBindingMode.INDIRECT);

        if (doShow) {
            Utils.log("get resource sorter id");
            XResourceId sorterID = ResourceId.createWithAnchorURL(xContext, "private:resource/view/SlideSorter", "private:resource/" + paner + "/LeftImpressPane");
            Utils.log("get resource pane id");
            XResourceId leftPaneID = ResourceId.create(xContext, "private:resource/" + paner + "/LeftImpressPane");

            Utils.log("unhide side panes");
            xCC.requestResourceActivation(sorterID, ResourceActivationMode.ADD);
            xCC.requestResourceActivation(leftPaneID, ResourceActivationMode.ADD);
            Utils.log("unhid side panes");

//            xCC.requestResourceActivation(tasksID, ResourceActivationMode.ADD);
//            xCC.requestResourceActivation(rightPaneID, ResourceActivationMode.ADD);

            // according to the API-doc this method should "hardly ever be called from outside the drawing framwork"
//            xCC.update();

        } else {

//            WaitForSlidePaneListener listener = new WaitForSlidePaneListener();
//            xCC.addConfigurationChangeListener(listener, "", new Integer(1));

            Utils.log("get resource sorter id");
            xCC.lock();
            XResourceId sorterID = ResourceId.createWithAnchorURL(xContext, "private:resource/view/SlideSorter", "private:resource/" + paner + "/LeftImpressPane");
            xCC.unlock();

            Utils.log("hide slide pane");
            xCC.requestResourceDeactivation(sorterID);
//            xCC.requestResourceDeactivation(tasksID);
            Utils.log("hid slide pane");

//            while (!listener.isSlidePaneHidden()) {
//                Utils.sleep(250);
//            }

//            System.err.println("done");

            // according to the API-doc this method should "hardly ever be called from outside the drawing framwork"
//            xCC.update();
        }
    }


    /**
     * Returns the slides that are being currently selected in the floating slide sorter pane.
     */


    public static Collection<XDrawPage> getSelectedSlideSorterSlides(XComponentContext xContext, XComponent xComponent) {
        XModel x = cast(XModel.class, xComponent);
        XController xco = x.getCurrentController();

        XControllerManager xCM = cast(XControllerManager.class, xco);
        XConfigurationController xCC = xCM.getConfigurationController();

        XResourceId id = ResourceId.createWithAnchorURL(xContext, "private:resource/view/SlideSorter", "private:resource/floater/LeftImpressPane");
//        XView xView = xCM.getViewController().getView(id);

        //todo finish this implementation
//        sorterPane.

        return new ArrayList<XDrawPage>();
    }


    public static void makeImpressAwareOfSlideChange(XDrawPage xDrawPage, XComponent xComponent) {
        createAndRemoveDummyShape(xDrawPage, xComponent);

//        XPropertySet xPageProps = cast(XPropertySet.class, xDrawPage);
//
//        try {
//            xPageProps.setPropertyValue("TransitionDirection", new Boolean(false));
//            xPageProps.setPropertyValue("TransitionDirection", new Boolean(true));
////            xPageProps.setPropertyValue("foobar", 1);
//        } catch (UnknownPropertyException e) {
//            e.printStackTrace();
//        } catch (PropertyVetoException e) {
//            e.printStackTrace();
//        } catch (com.sun.star.lang.IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (WrappedTargetException e) {
//            e.printStackTrace();
//        }
    }


    public static XShape createAndRemoveDummyShape(XDrawPage xDrawPage, XComponent xComponent) {
        XShape xShape = null;

        try {
            // query the document for the document-internal service factory
            XMultiServiceFactory xFactory = cast(XMultiServiceFactory.class, xComponent);

            // get the given Shape service from the factory
            Object xObj = xFactory.createInstance("com.sun.star.drawing.TextShape");
//            Point aPos = new Point(x, y);
//            Size aSize = new Size(width, height);

            // use its XShape interface to determine position and size before insertion
            xShape = cast(XShape.class, xObj);

            xShape.setPosition(new com.sun.star.awt.Point(1, 1));
            xShape.setSize(new Size(1, 1));

            /////////////////////////
            // add and remove shape to DrawPage

            // set text
//            XText xText = cast(XText.class, xShape);
//            xText.setString("My New Shape");

            XShapes xShapes = cast(XShapes.class, xDrawPage);
            xShapes.add(xShape);
            xShapes.remove(xShape);

        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        return xShape;
    }
//
//
//    private static class WaitForSlidePaneListener implements XConfigurationChangeListener {
//
//        int counter;
//        boolean isSlidePaneHidden = false;
//
//
//        public WaitForSlidePaneListener() {
//            counter = 0;
//        }
//
//
//        //        public void notifyConfigurationChange(ConfigurationChangeEvent configurationChangeEvent) {
//
//            Utils.log(configurationChangeEvent.toString());
//            counter++;
//
//            // remove the listener after the
//            if (counter > 5)
//                isSlidePaneHidden = true;
//        }
//
//
//        public boolean isSlidePaneHidden() {
//            return isSlidePaneHidden;
//        }
//
//
//        //        public void disposing(EventObject eventObject) {
//        }
//    }
}
