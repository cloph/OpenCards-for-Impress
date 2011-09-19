package info.opencards.oimputils;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.lang.XComponent;
import com.sun.star.presentation.XPresentation2;
import com.sun.star.presentation.XSlideShowController;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.Item;
import info.opencards.core.ItemCollection;
import info.opencards.core.ReversePolicy;

import java.util.Map;


/**
 * An OpenCards card-presentation proxy that makes use of the full-screen mode of OpenOffice Impress
 *
 * @author Holger Brandl
 */
public class PresenterModeProxy extends TagImpressProxy {

    boolean useFullScreenMode = true;

    private XSlideShowController showController;
    private XPresentation2 presentation;
    public Map<String, Object> userPresModeProps;


    public PresenterModeProxy(XComponent xComponent, XComponentContext xCompContext) {
        super(xComponent, xCompContext);
    }


    protected boolean showCardQuestion(int cardIndex, String tempSlideName) {
        if (!isUseFullScreenMode()) {
            return super.showCardQuestion(cardIndex, tempSlideName);
        }

        // this does not work in in the current impress presentation because seem to show no effect for the first time
//        XDrawPage xDrawPage = getXDrawPageByIndex(cardIndex);
//        PresenterModeUtils.configureSlideForQuestionMode(xDrawPage, xComponent);

        showController.gotoSlideIndex(cardIndex);
        return true;
    }


    private XDrawPage getXDrawPageByIndex(int cardIndex) {
        try {
            return showController.getSlideByIndex(cardIndex);
        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return null;
    }


    protected boolean showCardContent(int cardIndex, String tempSlideName) {
        if (!isUseFullScreenMode())
            return super.showCardContent(cardIndex, tempSlideName);

//        // configure the drawpage
//        try {
//            XDrawPage xDrawPage = showController.getSlideByIndex(cardIndex);
//            PresenterModeUtils.configureSlideForAnswerMode(xDrawPage);
//        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//
//        showController.gotoSlideIndex(cardIndex);
        showController.gotoSlideIndex(cardIndex);

        return true;
    }


    public boolean showCompleteCard(Item item) {
        if (!isUseFullScreenMode())
            return super.showCompleteCard(item);

        // go to the items' slide if not already there
        if (showController.getCurrentSlideIndex() != item.getFlashCard().getCardIndex())
            showController.gotoSlideIndex(item.getFlashCard().getCardIndex());

        // run as many effects as there are question-shapes
        XDrawPage xDrawPage = showController.getCurrentSlide();

        if (item.getFlashCard().getTodaysRevPolicy().equals(ReversePolicy.NORMAL)) {
            // show hidden answer
            for (int i = 0; i < XShapeUtils.getAnswerShapes(xDrawPage).size(); i++) {
                showController.gotoNextEffect();
            }
        } else {
            // show hidden question shapes
            for (int i = 0; i < XShapeUtils.getQuestionShapes(xDrawPage).size(); i++) {
                showController.gotoNextEffect();
            }
        }

        return true;
    }


    public void startFileSession(ItemCollection cardItemCollection) {
        super.startFileSession(cardItemCollection);

        if (isUseFullScreenMode()) {
//            PresenterModeUtils.prepareSlideEffectsForLearnSession(xComponent);

            // configure slides for learning
            for (Item item : cardItemCollection) {
                XDrawPage drawPage = ImpressHelper.getDrawPageByIndex(xComponent, item.getFlashCard().getCardIndex());

//                // clean up all effects
//                try {
//                    PresenterModeUtils.unsetAppearEffect(XShapeUtils.getAnswerShapes(drawPage));
//                } catch (UnknownPropertyException e) {
//                    e.printStackTrace();
//                } catch (WrappedTargetException e) {
//                    e.printStackTrace();
//                } catch (PropertyVetoException e) {
//                    e.printStackTrace();
//                } catch (com.sun.star.lang.IllegalArgumentException e) {
//                    e.printStackTrace();
//                }

                if (item.getFlashCard().getTodaysRevPolicy().equals(ReversePolicy.NORMAL)) {
                    PresenterModeUtils.configureSlideForQuestionMode(drawPage, xComponent);
                } else {
                    PresenterModeUtils.configureSlideForAnswerMode(drawPage, xComponent);
                }
            }


            // start the presentation
            Utils.sleep(1000);
            presentation = PresenterModeUtils.getPresentation(xComponent);
//            userPresModeProps = PresenterModeUtils.configurePresenterMode(presentation);
            showController = PresenterModeUtils.startPresentation(presentation);
//            showController.setAlwaysOnTop(true);
        }
    }


    public void stopFileSession() {
        if (isUseFullScreenMode()) {
            PresenterModeUtils.restoreUserProps(userPresModeProps, presentation);
            PresenterModeUtils.endPresentation(presentation);
        }

        super.stopFileSession();
    }


    public void stopLearnSession() {
        super.stopLearnSession();

    }


    public boolean isUseFullScreenMode() {
        return useFullScreenMode;
    }


    public void setUseFullScreenMode(boolean useFullScreenMode) {
        this.useFullScreenMode = useFullScreenMode;
    }
}
