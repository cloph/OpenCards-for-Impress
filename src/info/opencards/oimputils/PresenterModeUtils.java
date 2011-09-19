package info.opencards.oimputils;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.presentation.XPresentation;
import com.sun.star.presentation.XPresentation2;
import com.sun.star.presentation.XPresentationSupplier;
import com.sun.star.presentation.XSlideShowController;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import static info.opencards.oimputils.ImpressHelper.cast;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Some utility methods which ease the control of Impress when being in presentation (aka. full screen) mode
 *
 * @author Holger Brandl
 */
public class PresenterModeUtils {

    public static String PRESMODE_USE_FULL_SCREEN = "IsFullScreen";
    public static final String PRESMODE_PAUSE_MS = "Pause";
    public static final String PRESMODE_IS_TRANS_ON_CLICK = "IsTransitionOnClick";
    private static final String PRESMODE_IS_MOUSE_VISIBLE = "IsMouseVisible";
    private static final String PRESMODE_ALLOW_SHAPE_ANIMATIONS = "AllowAnimations";
    private static final String PRESMODE_USE_PEN = "UsePen";
    private static final String PRESMODE_NAVIGATOR = "StartWithNavigator";


    public static void main(String[] args) throws BootstrapException {

        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");
        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
        xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

//        prepareSlideEffectsForLearnSession(xComponent);

        XPresentation2 presentation = getPresentation(xComponent);
        XSlideShowController showController = startPresentation(presentation);

//        showController.addSlideShowListener(new UserInputCatcher());
//        showController.gotoSlideIndex(1);
//        presentation.end();s
//        Utils.sleep(1000);
//         showController = startPresentation(presentation);
//        showController.gotoSlideIndex(3);
    }


    public static void prepareSlideEffectsForLearnSession(XComponent xComponent) {

        try {
            XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);
            XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

            for (int i = 0; i < xDrawPages.getCount(); i++) {
                XDrawPage xDrawPage = ImpressHelper.cast(XDrawPage.class, xDrawPages.getByIndex(i));
                System.out.println("processing page " + DrawPageHelper.getSlideTitle(xDrawPage));
                configureSlideForQuestionMode(xDrawPage, xComponent);
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static String dumpXProps(XPropertySet xProps) {
        StringBuffer sb = new StringBuffer();
        for (Property property : xProps.getPropertySetInfo().getProperties()) {
            try {
                sb.append(property.Name + " : " + xProps.getPropertyValue(property.Name) + "\n");
            } catch (UnknownPropertyException e) {
                e.printStackTrace();
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }
        }

        System.err.println(sb);
        return sb.toString();
    }


    /**
     * Question mode perparation is done by making all non-question tags initially invisible.
     */
    public static void configureSlideForQuestionMode(XDrawPage xDrawPage, XComponent xComponent) {
        try {
            List<XShape> xShapes = XShapeUtils.getAnswerShapes(xDrawPage);

            for (XShape xShape : xShapes) {
                // add some slide effects
                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

                while (!xPropSet.getPropertyValue("TextEffect").equals(com.sun.star.presentation.AnimationEffect.NONE)) {
                    xPropSet.setPropertyValue("TextEffect", com.sun.star.presentation.AnimationEffect.NONE);
//                    Utils.sleep(100);
                    System.err.println(" new text effect value is " + xPropSet.getPropertyValue("TextEffect"));
                }

                xPropSet.setPropertyValue("Effect", com.sun.star.presentation.AnimationEffect.APPEAR);

                while (!xPropSet.getPropertyValue("TextEffect").equals(com.sun.star.presentation.AnimationEffect.NONE)) {
                    xPropSet.setPropertyValue("TextEffect", com.sun.star.presentation.AnimationEffect.NONE);
//                    Utils.sleep(100);
                    System.err.println(" new text effect value is " + xPropSet.getPropertyValue("TextEffect"));
                }

//                if(xPropSet.getPropertyValue("TextEffect")!= null)
            }

            // remove appearence effects from all other shapes
            unsetAppearEffect(XShapeUtils.getQuestionShapes(xDrawPage));

            // todo we have to fix the order of effects to allow custom slide effects to run properly
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }


        // do something to make impress aware of the slide change
//        ImpressHelper.makeImpressAwareOfSlideChange(xDrawPage, xComponent);
    }


    /**
     * Question mode perparation is done by making all question tags initially invisible.
     */
    public static void configureSlideForAnswerMode(XDrawPage xDrawPage, XComponent xComponent) {
        try {
            List<XShape> xShapes = XShapeUtils.getQuestionShapes(xDrawPage);

            for (XShape xShape : xShapes) {
                // add some slide effects
                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
                xPropSet.setPropertyValue("Effect", com.sun.star.presentation.AnimationEffect.APPEAR);
            }

            // remove appearence effects from all other shapes
            unsetAppearEffect(XShapeUtils.getAnswerShapes(xDrawPage));

            // todo we have to fix the order of effects to allow custom slide effects to run properly
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // do something to make impress aware of the slide change
//        ImpressHelper.makeImpressAwareOfSlideChange(xDrawPage, xComponent);
    }


    public static void unsetAppearEffect(List<XShape> questionShapes) throws UnknownPropertyException, WrappedTargetException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException {
        for (XShape xShape : questionShapes) {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

            if (xPropSet.getPropertyValue("Effect").equals(com.sun.star.presentation.AnimationEffect.APPEAR))
                xPropSet.setPropertyValue("Effect", com.sun.star.presentation.AnimationEffect.NONE);
        }
    }


    public static XSlideShowController startPresentation(XPresentation2 presentation) {
        // todo whey twice

        presentation.start();

        presentation.start();
        // wait until the slideshow has started and than return its controller
        int timoutMs = 500;
        while (presentation.getController() == null && timoutMs > 0) {
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timoutMs -= 3;
        }

        return presentation.getController();
    }


    public static Map<String, Object> configurePresenterMode(XPresentation2 presentation) {
        Map<String, Object> userProps = new HashMap<String, Object>();

        // configure the presentation
        // http://api.openoffice.org/docs/DevelopersGuide/Drawing/Drawing.xhtml#1_5_2_Presentation_Settings
        try {
            XPropertySet xPresPropSet = cast(XPropertySet.class, presentation);
//            xPresPropSet.setPropertyValue("IsEndless", true);
            userProps.put(PRESMODE_USE_FULL_SCREEN, xPresPropSet.getPropertyValue(PRESMODE_USE_FULL_SCREEN));
            xPresPropSet.setPropertyValue(PRESMODE_USE_FULL_SCREEN, true);

            userProps.put(PRESMODE_PAUSE_MS, xPresPropSet.getPropertyValue(PRESMODE_PAUSE_MS));
            xPresPropSet.setPropertyValue(PRESMODE_PAUSE_MS, 0);

            userProps.put(PRESMODE_IS_TRANS_ON_CLICK, xPresPropSet.getPropertyValue(PRESMODE_IS_TRANS_ON_CLICK));
            xPresPropSet.setPropertyValue(PRESMODE_IS_TRANS_ON_CLICK, false);

            userProps.put(PRESMODE_IS_MOUSE_VISIBLE, xPresPropSet.getPropertyValue(PRESMODE_IS_MOUSE_VISIBLE));
            xPresPropSet.setPropertyValue(PRESMODE_IS_MOUSE_VISIBLE, false);

            userProps.put(PRESMODE_ALLOW_SHAPE_ANIMATIONS, xPresPropSet.getPropertyValue(PRESMODE_ALLOW_SHAPE_ANIMATIONS));
            xPresPropSet.setPropertyValue(PRESMODE_ALLOW_SHAPE_ANIMATIONS, true);

            userProps.put(PRESMODE_USE_PEN, xPresPropSet.getPropertyValue(PRESMODE_USE_PEN));
            xPresPropSet.setPropertyValue(PRESMODE_USE_PEN, false);

            userProps.put(PRESMODE_NAVIGATOR, xPresPropSet.getPropertyValue(PRESMODE_NAVIGATOR));
            xPresPropSet.setPropertyValue(PRESMODE_NAVIGATOR, false);

            //todo we should make use of FirstPage
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return userProps;
    }


    public static void restoreUserProps(Map<String, Object> userPresModeProps, XPresentation2 presentation) {
        XPropertySet xPresPropSet = cast(XPropertySet.class, presentation);

        for (String propName : userPresModeProps.keySet()) {
            try {
                xPresPropSet.setPropertyValue(propName, userPresModeProps.get(propName));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }


    public static void endPresentation(XPresentation2 presentation) {
        presentation.end();
    }


    public static XPresentation2 getPresentation(XComponent xComponent) {
//        XCustomPresentationSupplier xCustPresSupplier = ImpressHelper.cast(XCustomPresentationSupplier.class, xComponent);
//        xCustPresSupplier.getCustomPresentations();

        // switch to presentation mode
        XPresentationSupplier xPresSupplier = ImpressHelper.cast(XPresentationSupplier.class, xComponent);
        XPresentation xPresentation = xPresSupplier.getPresentation();

        return cast(XPresentation2.class, xPresentation);
    }


}
