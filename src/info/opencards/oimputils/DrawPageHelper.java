package info.opencards.oimputils;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.*;
import com.sun.star.frame.XModel;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.presentation.XHandoutMasterSupplier;
import com.sun.star.presentation.XPresentationPage;
import com.sun.star.style.XStyle;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import info.opencards.Utils;
import static info.opencards.oimputils.ImpressHelper.cast;


/**
 * A collection of static utility methods related to XDrawPage creation and modification.
 *
 * @author Holger Brandl
 */
public class DrawPageHelper {

    static final String SLIDE_ID_PREFIX = "OpenCardsID ";// hack only :-)
    public static Point TITLE_POSITION;
    public static Size TITLE_SIZE;


    /**
     * get the page count for master pages
     */
    static public int getMasterPageCount(XComponent xComponent) throws Exception {
        XMasterPagesSupplier xMasterPagesSupplier = cast(XMasterPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
        return xDrawPages.getCount();
    }


    /**
     * get master page by index
     */
    static public XDrawPage getMasterPageByIndex(XComponent xComponent, int nIndex) throws Exception {
        XMasterPagesSupplier xMasterPagesSupplier =
                (XMasterPagesSupplier) UnoRuntime.queryInterface(
                        XMasterPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
        return (XDrawPage) xDrawPages.getByIndex(nIndex);
    }


    /**
     * creates and inserts a new master page into the giving position, the method returns the new created page
     */
    static public XDrawPage insertNewMasterPageByIndex(XComponent xComponent, int nIndex) throws Exception {
        XMasterPagesSupplier xMasterPagesSupplier = cast(XMasterPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
        return xDrawPages.insertNewByIndex(nIndex);
    }


    /**
     * removes the given page
     */
    static public void removeMasterPage(XComponent xComponent, XDrawPage xDrawPage) throws Exception {
        XMasterPagesSupplier xMasterPagesSupplier = cast(XMasterPagesSupplier.class, xComponent);
        XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
        xDrawPages.remove(xDrawPage);
    }


    /**
     * return the corresponding masterpage for the giving drawpage
     */
    static public XDrawPage getMasterPage(XDrawPage xDrawPage) throws Exception {
        XMasterPageTarget xMasterPageTarget = cast(XMasterPageTarget.class, xDrawPage);
        return xMasterPageTarget.getMasterPage();
    }


    /**
     * in impress documents each normal draw page has a corresponding notes page
     */
    static public XDrawPage getNotesPage(XDrawPage xDrawPage) throws Exception {
        XPresentationPage aPresentationPage = cast(XPresentationPage.class, xDrawPage);
        return aPresentationPage.getNotesPage();
    }


    /**
     * in impress each documents has one handout page
     */
    static public XDrawPage getHandoutMasterPage(XComponent xComponent) throws Exception {
        XHandoutMasterSupplier aHandoutMasterSupplier = cast(XHandoutMasterSupplier.class, xComponent);
        return aHandoutMasterSupplier.getHandoutMasterPage();
    }


    public static XShape createTitleShape(XDrawPage xDrawPage, XComponent xComponent) {
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
            XShapes xShapes = cast(XShapes.class, xDrawPage);
            xShapes.add(xShape);

            xShape.setPosition(new com.sun.star.awt.Point(TITLE_POSITION.X, TITLE_POSITION.Y));
            xShape.setSize(new Size(TITLE_SIZE.Width, TITLE_SIZE.Height));

            /////////////////////////
            //apply title style

            XModel xModel = cast(XModel.class, xComponent);
            com.sun.star.style.XStyleFamiliesSupplier xSFS = cast(com.sun.star.style.XStyleFamiliesSupplier.class, xModel);
            com.sun.star.container.XNameAccess xFamilies = xSFS.getStyleFamilies();

            String familiyName = xFamilies.getElementNames()[1];
            Object obj = xFamilies.getByName(familiyName); // normaly "Default" is used here
            com.sun.star.container.XNameAccess xStyles = cast(com.sun.star.container.XNameAccess.class, obj);
            obj = xStyles.getByName("title");
            com.sun.star.style.XStyle xTitle1Style = cast(com.sun.star.style.XStyle.class, obj);

            XPropertySet xPropSet = cast(XPropertySet.class, xShape);
            xPropSet.setPropertyValue("Style", xTitle1Style);

            /////////////////////////

            // add shape to DrawPage

            // set text
            XText xText = cast(XText.class, xShape);
            xText.setString("My New Shape");
        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        return xShape;
    }


    public static XShape getTitleShape(XDrawPage xDrawPage) {
        try {
            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);

            // try to find a shape which has is a title
            for (int j = 0; j < xShapes.getCount(); j++) {
                XShape xShape = cast(XShape.class, xShapes.getByIndex(j));
                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

                Object style = xPropSet.getPropertyValue("Style");
                XStyle usedXStyle = cast(XStyle.class, style);

                if (usedXStyle.getName().equals("title")) {
                    return xShape;
                }
            }
        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        }

        return null;
    }


    // this is rather a hack around the bug of OOo to return from blocking method although OpenOffice is still busy
    // (e.g. after switching from one file to another
    public static int retryCounter;


    public static void removeAllButTitleShape(XDrawPage xDrawPage) {
        try {
            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);

            // try to find a shape which has is a title
            for (int j = 0; j < xShapes.getCount(); j++) {
                XShape xShape = cast(XShape.class, xShapes.getByIndex(j));
                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

                Object style = xPropSet.getPropertyValue("Style");
                XStyle usedXStyle = cast(XStyle.class, style);

                String styleName = usedXStyle.getName();
                if (styleName != null && !styleName.equals("title")) {
                    xShapes.remove(xShape);
                    j--;
                }
            }
        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (DisposedException e) {
            // just try again if we have not tried this is without any change in OOo response before
            if (retryCounter < 5) {
                retryCounter++;
                Utils.log("DisposedException catched. Reinvoking removeAllButTitleShape()");

                // wait a few milliseconds
                Utils.sleep(50);

                // just try it again
                removeAllButTitleShape(xDrawPage);
            }
        }
    }


    public static String getSlideTitle(XDrawPage xDrawPage) throws Exception {

        XShape xShape = getTitleShape(xDrawPage);

        if (xShape == null)
            return null;

        TITLE_POSITION = xShape.getPosition();
        TITLE_SIZE = xShape.getSize();

        // extract its text
        XText xText = cast(XText.class, xShape);
        return xText.getString();
    }


    public static Integer readDrawPageCustomProperty(XDrawPage xDrawPage) {
        try {
            // now read the title of the slide, test wether it starts with "SlideID" and extract the ID if true
            String slideName = ImpressHelper.cast(XNamed.class, xDrawPage).getName();
            if (!slideName.startsWith(SLIDE_ID_PREFIX))
                return null;

            return Integer.parseInt(slideName.replaceAll(SLIDE_ID_PREFIX, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static void writeDrawPageCustomProperty(XDrawPage xDrawPage, int slideID) {
        try {
            ImpressHelper.cast(XNamed.class, xDrawPage).setName(SLIDE_ID_PREFIX + slideID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
