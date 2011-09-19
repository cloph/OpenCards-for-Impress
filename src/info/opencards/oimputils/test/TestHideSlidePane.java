package info.opencards.oimputils.test;

import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XDispatch;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;
import info.opencards.Utils;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;

import java.io.File;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class TestHideSlidePane {

    private static XComponentContext xRemoteContext;
    private static XMultiComponentFactory xRemoteServiceManager;
    private static XURLTransformer xTransformer;


    public static void main(String[] args) throws BootstrapException, com.sun.star.uno.Exception {
        XComponentContext xLocalContext = Bootstrap.bootstrap();
        OpenOfficeUtils.getLocale(xLocalContext);

        XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
        XComponent xComponent = OOoDocumentUtils.openDocument(xLocalContext, "private:factory/simpress");

        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
        xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);


        Object initialObject = xLocalContext.getServiceManager();
        XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, initialObject);
        Object context = xPropertySet.getPropertyValue("DefaultContext");
        xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(
                XComponentContext.class, context);
        xRemoteServiceManager = xRemoteContext.getServiceManager();
        Object transformer = xRemoteServiceManager.createInstanceWithContext(
                "com.sun.star.util.URLTransformer", xRemoteContext);
        xTransformer = (com.sun.star.util.XURLTransformer) UnoRuntime.queryInterface(
                com.sun.star.util.XURLTransformer.class, transformer);

        testCommands();

    }


    private static void testCommands() throws com.sun.star.uno.Exception {
        // We need the desktop to get access to the current frame
        Object desktop = xRemoteServiceManager.createInstanceWithContext(
                "com.sun.star.frame.Desktop", xRemoteContext);
        com.sun.star.frame.XDesktop xDesktop = (com.sun.star.frame.XDesktop) UnoRuntime.queryInterface(
                com.sun.star.frame.XDesktop.class, desktop);
        com.sun.star.frame.XFrame xFrame = xDesktop.getCurrentFrame();
        com.sun.star.frame.XDispatchProvider xDispatchProvider;
        if (xFrame != null) {
            // We have a frame. Now we need access to the dispatch provider.
            xDispatchProvider = (com.sun.star.frame.XDispatchProvider) UnoRuntime.queryInterface(
                    com.sun.star.frame.XDispatchProvider.class, xFrame);
            if (xDispatchProvider != null) {
                // As we have the dispatch provider we can now check if we get a dispatch
                // object or not.
                // Prepare the URL
                URL[] aURL = new URL[1];
                aURL[0] = new com.sun.star.util.URL();

                aURL[0].Complete = ".uno:About";
                xTransformer.parseSmart(aURL, ".uno:");

                // Try to get a dispatch object for our URL
                XDispatch xDispatch = xDispatchProvider.queryDispatch(aURL[0], "", 0);

                if (xDispatch != null) {
                    System.out.println("Ok, dispatch object for " + aURL[0].Complete);
                } else {
                    System.out.println("Something is wrong, I cannot get dispatch   object for "
                            + aURL[0].Complete);
                }
            } else
                System.out.println("Couldn't get XDispatchProvider from Frame!");
        } else
            System.out.println("Couldn't get current Frame from Desktop!");
    }


}
