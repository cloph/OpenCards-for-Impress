package info.opencards.oimputils;

import com.sun.star.container.XNamed;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;

import java.util.List;


/**
 * A <code>PresenterProxy</code> implementation which uses OpenOffice Impress as presentation backend.
 *
 * @author Holger Brandl
 */
// todo because this is the default for OC2 we should merge it into ImpressProxy (which should enjoy a slim down)
public class TagImpressProxy extends ImpressProxy {

    public TagImpressProxy(XComponent xComponent, XComponentContext xCompContext) {
        super(xComponent, xCompContext);
    }


    protected boolean showCardContent(int cardIndex, String tempSlideName) {
        XDrawPage dupSlide = duplicateSlide(cardIndex);

        // this is just a hack in order to find such slides in cases that a file was closed before this slide was removed
        ImpressHelper.cast(XNamed.class, dupSlide).setName(tempSlideName);
        List<XShape> xShapes = XShapeUtils.getQuestionShapes(dupSlide);
        XShapeUtils.removeShapes(dupSlide, xShapes);

        setCurrentSlide(dupSlide, getXComponent());

        return !xShapes.isEmpty();
    }


    protected boolean showCardQuestion(int cardIndex, String tempSlideName) {
        XDrawPage dupSlide = duplicateSlide(cardIndex);

        // this is just a hack in order to find such slides in cases that a file was closed before this slide was removed
        ImpressHelper.cast(XNamed.class, dupSlide).setName(tempSlideName);

        DrawPageHelper.retryCounter = 0;

        List<XShape> answerShapes = XShapeUtils.getAnswerShapes(dupSlide);
        XShapeUtils.removeShapes(dupSlide, answerShapes);
        setCurrentSlide(dupSlide, getXComponent());

        return !answerShapes.isEmpty();

//        //set auto-layout to use "Title Only"-layout
//        XPropertySet xPropSet = cast(XPropertySet.class, dupSlide);
//        xPropSet.setPropertyValue("Layout", 19); // 19==Title Only
    }
}