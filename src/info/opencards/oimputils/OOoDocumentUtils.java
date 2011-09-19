package info.opencards.oimputils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.*;
import com.sun.star.io.IOException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import static info.opencards.oimputils.ImpressHelper.cast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;


/**
 * Some very high-level helper methods which wrap some OO-functionality.
 *
 * @author Holger Brandl
 */
public class OOoDocumentUtils {


    /**
     * The reference to the newly loaded document.
     */
    public static XComponent openDocumentInSameFrame(XComponent xDrawComponent, String sNextUrl) {
        XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xDrawComponent);
        if (xModel == null) {
            Utils.log(Level.SEVERE, "ERROR: can not open document in same frame");

            // this won't work
            return openDocument(cast(XComponentContext.class, xDrawComponent), sNextUrl);
        }

        XController xController = xModel.getCurrentController();

        String dummyFrameName = "opencards";

        XFrame frame = xController.getFrame();
        frame.setName(dummyFrameName);
        XComponentLoader xComponentLoader = cast(XComponentLoader.class, frame);

        try {
            // note: the third parameter encodes the searchgflags as described in http://docs.sun.com/app/docs/doc/819-1326/6n3mloku3?l=de&a=view

            // todo openissue that could greatly improve the stabilty of OC: Is this method completely blocking or not?
            // If not: How can we block the return of the method until is completely loaded?

            xDrawComponent = xComponentLoader.loadComponentFromURL(sNextUrl, dummyFrameName, 2, new PropertyValue[0]);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return xDrawComponent;
    }


    public static XComponent openDocument(XComponentContext xContext, String fileURL) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();
            Object desktop = serviceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);

            XComponentLoader xComponentLoader = cast(XComponentLoader.class, desktop);
            PropertyValue[] loadProps = new PropertyValue[0];

            return xComponentLoader.loadComponentFromURL(fileURL, "_blank", 0, loadProps);
        } catch (com.sun.star.uno.Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Saves a document if it already has save-location.
     *
     * @return <code>true</code> if the document was saved. <code>false</code> if there is no save-location defined for
     *         <code>xDrawComponent</code>, and it was not saved.
     */
    public static boolean saveDocument(XComponent xDrawComponent) {
        XStorable xStorable = cast(XStorable.class, xDrawComponent);
        xStorable.getLocation();

//        XModel xModel = cast(com.sun.star.frame.XModel.class, xDrawComponent);
//        if (xModel != null) {
//            // Check for modifications => break save process if there is nothing to do.
//            XModifiable xModified = cast(XModifiable.class, xModel);
//            if (xModified.isModified()) {
//                XStorable xStore = cast(com.sun.star.frame.XStorable.class, xModel);
//                xStore.store();
//            }
//        }

        try {
            if (xStorable.hasLocation()) {
                xStorable.store();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public static void closeDocument(XComponent xDrawComponent) throws CloseVetoException {
        XCloseable closable = cast(XCloseable.class, xDrawComponent);
        closable.close(true);
    }


    public static XComponent getXComponent(XComponentContext xCompContext) {

        XMultiComponentFactory xMCF = xCompContext.getServiceManager();

        Object desktop = null;
        try {
            desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        XDesktop xDesktop = cast(XDesktop.class, desktop);
        return xDesktop.getCurrentComponent();
    }


    public static XDesktop getXDesktop(XComponentContext xCompContext) {
        XMultiComponentFactory xMCF = xCompContext.getServiceManager();

//        for (String serviceName : xMCF.getAvailableServiceNames()) {
//            System.out.println(""+serviceName);
//        }

        Object desktop = null;
        try {
            desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        return cast(XDesktop.class, desktop);
    }


    public static XComponent getOpenXDocument(CardFile cardFile, XComponentContext xCompContext) {
        if (cardFile == null || cardFile.getFileLocation() == null)
            return getXComponent(xCompContext);

        XDesktop xDesktop = getXDesktop(xCompContext);
//        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, xDesktop);

        XEnumerationAccess curComponents = xDesktop.getComponents();

        XEnumeration xEnumeration = curComponents.createEnumeration();
        while (xEnumeration.hasMoreElements()) {
            try {
                XComponent xComp = cast(XComponent.class, xEnumeration.nextElement());
                if (isCurrentDocument(xComp, cardFile.getFileLocation())) return xComp;

            } catch (NoSuchElementException e) {
                e.printStackTrace();
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static boolean isCurrentDocument(XComponent xComp, File documentFile) {
        XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class, xComp);

        if (xmodel != null) {
            String url = xmodel.getURL();

            if (url.trim().length() > 0) {
                try {
                    File curOOFile = new File(new URL(url).getFile().replace("%20", " ")); // seems to be buggy
//                    File curOOFile = new File(new URL(url).getFile());
                    File filterCard = new File(documentFile.getAbsolutePath().replace("%20", " "));

                    if (curOOFile.getAbsolutePath().equals(filterCard.getAbsolutePath()))
                        return true;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    public static XFrame getComponentFrame(XComponent xComponent) {
        XModel drawDocModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
        XController xController = drawDocModel.getCurrentController();

        return xController.getFrame();
    }
}
