package info.opencards.oimputils;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionSupplier;

import java.util.ArrayList;


/**
 * Some helper  methods to modify, tag or create XShapes
 *
 * @author Holger Brandl
 */
public class XShapeUtils {

    public static final String OC_QUESTION_SHAPE = "opencards_question";
    public static final String OC_ANSWER_SHAPE = "opencards_answer";


    public static void tagSelectedShapesAsQuestion(XDrawPage xDrawPage, XComponent xComponent) {
        try {

            // untag all shapes
            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);

            // try to find a shape which has is a title
            for (int j = 0; j < xShapes.getCount(); j++) {
                XShape xShape = ImpressHelper.cast(XShape.class, xShapes.getByIndex(j));
                unsetQuestionTag(xShape);
            }

            // now tag all selected shapes

            XModel x = ImpressHelper.cast(XModel.class, xComponent);
            XController xco = x.getCurrentController();
            XSelectionSupplier selectionSupplier = ImpressHelper.cast(XSelectionSupplier.class, xco);
            Object selection = selectionSupplier.getSelection();

            xShapes = ImpressHelper.cast(XShapes.class, selection);

            if (xShapes == null) {
                // no shape is selected
                return;
            }

            for (int i = 0; i < xShapes.getCount(); i++) {
                XShape xShape = ImpressHelper.cast(XShape.class, xShapes.getByIndex(i));
                System.out.println("" + i);

                setQuestionTag(xShape);
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static void setQuestionTag(XShape xShape) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, WrappedTargetException {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
        xPropSet.setPropertyValue("Name", OC_QUESTION_SHAPE);
    }


    public static void unsetQuestionTag(XShape xShape) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, WrappedTargetException {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
        xPropSet.setPropertyValue("Name", OC_ANSWER_SHAPE);
    }


    public static void selectQuestionShapes(XDrawPage xDrawPage, XComponent xComponent, XComponentContext m_xContext) {
        try {
            XMultiComponentFactory globalServiceManager = m_xContext.getServiceManager();
            XShapes questionShapes = ImpressHelper.cast(XShapes.class, globalServiceManager.createInstanceWithContext("com.sun.star.drawing.ShapeCollection", m_xContext));

            for (XShape questionXShape : getQuestionShapes(xDrawPage)) {
                questionShapes.add(questionXShape);
            }

            // now finally select the objects
            XModel x = ImpressHelper.cast(XModel.class, xComponent);
            XController xco = x.getCurrentController();
            XSelectionSupplier selectionSupplier = ImpressHelper.cast(XSelectionSupplier.class, xco);
            selectionSupplier.select(questionShapes);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static java.util.List<XShape> getQuestionShapes(XDrawPage xDrawPage) {
        try {
            XShapes allXShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);


            java.util.List<XShape> questionXShapes = new ArrayList<XShape>();
            for (int j = 0; j < allXShapes.getCount(); j++) {
                XShape xShape = ImpressHelper.cast(XShape.class, allXShapes.getByIndex(j));

                boolean isTaggedAsQuestion = isTaggedAsQuestion(xShape);
                if (isTaggedAsQuestion) {
                    questionXShapes.add(xShape);
                }
            }

            // if no questions shapes have been found, return the slide title shape instead if it has not been explicitely tagged as answer
            // the latter might be the case, if a slide is not supposed to be an OpenCards flashcard.
            if (questionXShapes.isEmpty()) {
                XShape titleShape = DrawPageHelper.getTitleShape(xDrawPage);
                if (titleShape != null && !isTaggedAsAnswer(titleShape)) {
                    questionXShapes.add(titleShape);
                }
            }

            return questionXShapes;

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static boolean isTaggedAsQuestion(XShape xShape) throws UnknownPropertyException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

        Object shapeName = OpenOfficeUtils.readProperty(xPropSet, "Name");
        boolean isTaggedAsQuestion = shapeName != null && ((String) shapeName).trim().equals(OC_QUESTION_SHAPE);
        return isTaggedAsQuestion;
    }


    public static boolean isTaggedAsAnswer(XShape xShape) throws UnknownPropertyException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

        Object shapeName = OpenOfficeUtils.readProperty(xPropSet, "Name");
        boolean isTaggedAsAnswer = shapeName != null && ((String) shapeName).trim().equals(OC_ANSWER_SHAPE);
        return isTaggedAsAnswer;
    }


    public static java.util.List<XShape> getAnswerShapes(XDrawPage xDrawPage) {
        try {
            XShapes allXShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);

            java.util.List<XShape> answerShapes = new ArrayList<XShape>();
            java.util.List<XShape> questionShapes = getQuestionShapes(xDrawPage);


            for (int j = 0; j < allXShapes.getCount(); j++) {
                XShape xShape = ImpressHelper.cast(XShape.class, allXShapes.getByIndex(j));
                if (!questionShapes.contains(xShape)) {
                    answerShapes.add(xShape);
                }

//                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
//
//                Object shapeName = OpenOfficeUtils.readProperty(xPropSet, "Name");
//                if (shapeName != null && ((String) shapeName).trim().equals(OC_QUESTION_SHAPE)) {
//                    questionXShapes.add(xShape);
//                }
            }

            return answerShapes;

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static void removeShapes(XDrawPage xDrawPage, java.util.List<XShape> questionShapes) {
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);
        for (XShape questionShape : questionShapes) {
            xShapes.remove(questionShape);
        }
    }
}
