package info.opencards.oimputils.test;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XURLTransformer;
import info.opencards.Utils;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;

import java.io.File;


/*
*  Provides example code how to enable/disable
*  commands.
*/
public class MenuInterceptorTest {

    /*
    *  A list of command names
    */
    final static private String[] aCommandURLTestSet =
            {
                    new String("Open"),
                    new String("About"),
                    new String("SelectAll"),
                    new String("Quit"),
            };

    private static XComponentContext xRemoteContext = null;
    private static XMultiComponentFactory xRemoteServiceManager = null;
    private static XURLTransformer xTransformer = null;
    private static XMultiServiceFactory xConfigProvider = null;


    /*
    *  @param args the command line arguments
    */
    public static void main(String[] args) {

        try {
            // connect
            XComponentContext xLocalContext = Bootstrap.bootstrap();
            OpenOfficeUtils.getLocale(xLocalContext);
            XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
            XComponent xComponent = OOoDocumentUtils.openDocument(xLocalContext, "private:factory/simpress");
            String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
            xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

            Object urlResolver = xLocalServiceManager.createInstanceWithContext(
                    "com.sun.star.bridge.UnoUrlResolver", xLocalContext);
            XUnoUrlResolver xUnoUrlResolver = (XUnoUrlResolver) UnoRuntime.queryInterface(
                    XUnoUrlResolver.class, urlResolver);
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

            Object configProvider = xRemoteServiceManager.createInstanceWithContext(
                    "com.sun.star.configuration.ConfigurationProvider", xRemoteContext);
            xConfigProvider = (com.sun.star.lang.XMultiServiceFactory) UnoRuntime.queryInterface(
                    com.sun.star.lang.XMultiServiceFactory.class, configProvider);

            // First we need a defined starting point. So we have to remove
            // all commands from the disabled set!
            enableCommands();

            // Check if the commands are usable
            testCommands(false);

            // Disable the commands
            disableCommands();

            // Now the commands should not be usable anymore
            testCommands(true);

            // Remove disable commands to make Office usable again
//            enableCommands();
        }
        catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }


    /**
     * Test the commands that we enabled/disabled
     */
    private static void testCommands(boolean bDisabledCmds) throws com.sun.star.uno.Exception {
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
                for (int n = 0; n < aCommandURLTestSet.length; n++) {
                    // Prepare the URL
                    com.sun.star.util.URL[] aURL = new com.sun.star.util.URL[1];
                    aURL[0] = new com.sun.star.util.URL();
                    com.sun.star.frame.XDispatch xDispatch;

                    aURL[0].Complete = ".uno:" + aCommandURLTestSet[n];
                    xTransformer.parseSmart(aURL, ".uno:");

                    // Try to get a dispatch object for our URL
                    xDispatch = xDispatchProvider.queryDispatch(aURL[0], "", 0);

                    if (xDispatch != null) {
                        if (bDisabledCmds)
                            System.out.println("Something is wrong, I got dispatch object for "
                                    + aURL[0].Complete);
                        else
                            System.out.println("Ok, dispatch object for " + aURL[0].Complete);
                    } else {
                        if (!bDisabledCmds)
                            System.out.println("Something is wrong, I cannot get dispatch   object for "
                                    + aURL[0].Complete);
                        else
                            System.out.println("Ok, no dispatch object for " + aURL[0].Complete);
                    }
                    resetURL(aURL[0]);
                }
            } else
                System.out.println("Couldn't get XDispatchProvider from Frame!");
        } else
            System.out.println("Couldn't get current Frame from Desktop!");
    }


    /**
     * Ensure that there are no disabled commands in the user layer. The implementation removes all commands from the
     * disabled set!
     */
    private static void enableCommands() {
        // Set the root path for our configuration access
        com.sun.star.beans.PropertyValue[] lParams = new com.sun.star.beans.PropertyValue[1];

        lParams[0] = new com.sun.star.beans.PropertyValue();
        lParams[0].Name = new String("nodepath");
        lParams[0].Value = "/org.openoffice.Office.Commands/Execute/Disabled";

        try {
            // Create configuration update access to have write access to the configuration
            Object xAccess = xConfigProvider.createInstanceWithArguments("com.sun.star.configuration.ConfigurationUpdateAccess", lParams);

            com.sun.star.container.XNameAccess xNameAccess = (com.sun.star.container.XNameAccess)
                    UnoRuntime.queryInterface(com.sun.star.container.XNameAccess.class, xAccess);
            if (xNameAccess != null) {
                // We need the XNameContainer interface to remove the nodes by name
                com.sun.star.container.XNameContainer xNameContainer =
                        (com.sun.star.container.XNameContainer)
                                UnoRuntime.queryInterface(com.sun.star.container.XNameContainer.class, xAccess);

                // Retrieves the names of all Disabled nodes
                String[] aCommandsSeq = xNameAccess.getElementNames();
                for (int n = 0; n < aCommandsSeq.length; n++) {
                    try {
                        // remove the node
                        xNameContainer.removeByName(aCommandsSeq[n]);
                    }
                    catch (com.sun.star.lang.WrappedTargetException e) {
                    }
                    catch (com.sun.star.container.NoSuchElementException e) {
                    }
                }
            }

            // Commit our changes
            com.sun.star.util.XChangesBatch xFlush =
                    (com.sun.star.util.XChangesBatch) UnoRuntime.queryInterface(
                            com.sun.star.util.XChangesBatch.class, xAccess);
            xFlush.commitChanges();
        }
        catch (com.sun.star.uno.Exception e) {
            System.out.println("Exception detected!");
            System.out.println(e);
        }
    }


    /**
     * Disable all commands defined in the aCommandURLTestSet array
     */
    private static void disableCommands() {
        // Set the root path for our configuration access
        com.sun.star.beans.PropertyValue[] lParams = new com.sun.star.beans.PropertyValue[1];
        lParams[0] = new com.sun.star.beans.PropertyValue();
        lParams[0].Name = new String("nodepath");
        lParams[0].Value = "/org.openoffice.Office.Commands/Execute/Disabled";

        try {
            // Create configuration update access to have write access to the configuration
            Object xAccess = xConfigProvider.createInstanceWithArguments(
                    "com.sun.star.configuration.ConfigurationUpdateAccess", lParams);

            com.sun.star.lang.XSingleServiceFactory xSetElementFactory =
                    (com.sun.star.lang.XSingleServiceFactory) UnoRuntime.queryInterface(
                            com.sun.star.lang.XSingleServiceFactory.class, xAccess);

            com.sun.star.container.XNameContainer xNameContainer =
                    (com.sun.star.container.XNameContainer) UnoRuntime.queryInterface(
                            com.sun.star.container.XNameContainer.class, xAccess);

            if (xSetElementFactory != null && xNameContainer != null) {
                Object[] aArgs = new Object[0];

                for (int i = 0; i < aCommandURLTestSet.length; i++) {
                    // Create the nodes with the XSingleServiceFactory of the configuration
                    Object xNewElement = xSetElementFactory.createInstanceWithArguments(aArgs);
                    if (xNewElement != null) {
                        // We have a new node. To set the properties of the node we need
                        // the XPropertySet interface.
                        com.sun.star.beans.XPropertySet xPropertySet =
                                (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,
                                        xNewElement);

                        if (xPropertySet != null) {
                            // Create a unique node name.
                            String aCmdNodeName = new String("Command-");
                            aCmdNodeName += i;

                            // Insert the node into the Disabled set
                            xPropertySet.setPropertyValue("Command", aCommandURLTestSet[i]);
                            xNameContainer.insertByName(aCmdNodeName, xNewElement);
                        }
                    }
                }

                // Commit our changes
                com.sun.star.util.XChangesBatch xFlush = (com.sun.star.util.XChangesBatch)
                        UnoRuntime.queryInterface(com.sun.star.util.XChangesBatch.class, xAccess);
                xFlush.commitChanges();
            }
        }
        catch (com.sun.star.uno.Exception e) {
            System.out.println("Exception detected!");
            System.out.println(e);
        }
    }


    /**
     * reset URL so it can be reused
     *
     * @param aURL the URL that should be reseted
     */
    private static void resetURL(com.sun.star.util.URL aURL) {
        aURL.Protocol = "";
        aURL.User = "";
        aURL.Password = "";
        aURL.Server = "";
        aURL.Port = 0;
        aURL.Path = "";
        aURL.Name = "";
        aURL.Arguments = "";
        aURL.Mark = "";
        aURL.Main = "";
        aURL.Complete = "";
    }
}