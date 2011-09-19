package info.opencards.oimputils.test;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertyContainer;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.document.XStandaloneDocumentInfo;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.thoughtworks.xstream.XStream;
import info.opencards.Utils;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;


/**
 * A small hacking environment used for understanding OOo.
 *
 * @author Holger Brandl
 */
public class SerializationTests {


    @Test
    public void testClosedFileSerialization() throws BootstrapException {
        File file = new File("testdata/testpres2.odp");

        XComponentContext xContext = Bootstrap.bootstrap();

        OpenOfficeUtils.writeClosedFileDocumentProperty(xContext, "foobar", new Integer(27), file);
        Object o = OpenOfficeUtils.readClosedFileDocumentProperty(xContext, "foobar", file);

        Assert.assertTrue(o instanceof Integer || o instanceof Long);
        Assert.assertTrue(((Long) o) == 27);
    }

//    //set auto-layout to use "Title Only"-layout
//        XPropertySet xPropSet = cast(XPropertySet.class, editorSlide);
//        xPropSet.setPropertyValue("Layout", 3); // 19==Title Only
//
//        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, editorSlide);
//
//        // try to find a shape which has is a title
//        for (int j = 0; j < xShapes.getCount(); j++) {
//            XShape xShape = cast(XShape.class, xShapes.getByIndex(j));
//            XPropertySet xPropSet2 = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
//
//            Object style = xPropSet2.getPropertyValue("Style");
//            XStyle usedXStyle = cast(XStyle.class, style);
//
//            System.out.println("shape name is: " + usedXStyle.getName());
//        }
//
//        DrawPageHelper.removeAllOutlineShape(editorSlide, false);

    //    @Test
    // note: 1mb seems to be noe problem which are ca. 500 cards


    public void findMetaDataLimit() throws BootstrapException {
        File file = Utils.createTempCopy(new File("testdata/testpres2.odp"));

        XComponentContext xContext = Bootstrap.bootstrap();

        StringBuffer metaBuffer = new StringBuffer();
        while (true) {
            metaBuffer.append(new XStream().toXML(metaBuffer));
            String curMetaData = metaBuffer.toString();
            System.err.println("current meta-data length is " + curMetaData.length());

            OpenOfficeUtils.writeClosedFileDocumentProperty(xContext, "foobar", curMetaData, file);
            Object o = OpenOfficeUtils.readClosedFileDocumentProperty(xContext, "foobar", file);

            Assert.assertTrue(curMetaData.equals(o));
        }
    }


    //    @Test
    public void useQuickAcessAPI() {
        try {
            // get the remote office component context
            XComponentContext xContext = Bootstrap.bootstrap();

            XMultiComponentFactory serviceManager = xContext.getServiceManager();

            XInterface xDocInterface = (XInterface) serviceManager.createInstanceWithContext("com.sun.star.document.StandaloneDocumentInfo", xContext);
            XStandaloneDocumentInfo xDocInfo = cast(XStandaloneDocumentInfo.class, xDocInterface);

            String fileURL = "file:///o:/bin/testdata/testpres2.odp";
            xDocInfo.loadFromURL(fileURL);
//            xDocInfo.loadFromURL("file:///o:/testdata/testpres2.ppt");


            XPropertySet xPropSet = cast(XPropertySet.class, xDocInfo);
            Object propValue = xPropSet.getPropertyValue("blau");
            if (AnyConverter.isString(propValue)) {
                String fieldValue = AnyConverter.toString(propValue);
                System.out.println("  value=" + fieldValue);
            }

            XPropertyContainer xPropContainer = cast(XPropertyContainer.class, xDocInfo);
            xPropContainer.addProperty("blau", PropertyAttribute.REMOVABLE, "baer");

//            xPropSet.getPropertySetInfo().setPropertyValue("blau", "baer");

            xDocInfo.setUserFieldName((short) 4, "hallowelt");
            xDocInfo.setUserFieldValue((short) 4, "houase");

            xDocInfo.storeIntoURL(fileURL);

            Object desktop = serviceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);

            XComponentLoader xComponentLoader = cast(XComponentLoader.class, desktop);
            PropertyValue[] loadProps = new PropertyValue[0];

//            XComponent xDrawComponent = xComponentLoader.loadComponentFromURL("private:factory/simpress", "_blank", 0, loadProps);
            XComponent xDrawComponent = xComponentLoader.loadComponentFromURL("file:///o:/bin/testdata/testpres.ppt", "_blank", 0, loadProps);

            OOoDocumentUtils.saveDocument(xDrawComponent);

//            selectSlideInImpress(new FlashCard("test", 3), xDrawComponent);

            // load the a new file in the same frame
            xDrawComponent = OOoDocumentUtils.openDocumentInSameFrame(xDrawComponent, "file:///o:/bin/testdata/testpres2.odp");

//            OOoDocumentUtils.writeAndReadProps(xDrawComponent);

            for (String serviceName : xContext.getServiceManager().getAvailableServiceNames()) {
                System.out.println("service: " + serviceName);
            }
        }
        catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
