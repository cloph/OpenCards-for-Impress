package info.opencards.oimputils;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import static info.opencards.oimputils.ImpressHelper.cast;


/**
 * An interceptor for the slide-pane context-menu, which just adds a new OpenCards submenu to it
 *
 * @author Holger Brandl
 */
public class SlidePaneCxtMenuInterceptor implements XContextMenuInterceptor {

    public static void install(final XComponentContext xContext) {
        new Thread() {

            public void run() {
                XModel x = cast(XModel.class, OOoDocumentUtils.getXComponent(xContext));
                if (x == null)
                    return;

                XController xco = x.getCurrentController();
                XContextMenuInterception cxtInterceptor = cast(XContextMenuInterception.class, xco);

                cxtInterceptor.registerContextMenuInterceptor(new SlidePaneCxtMenuInterceptor());
            }
        }.start();

    }


    public ContextMenuInterceptorAction notifyContextMenuExecute(com.sun.star.ui.ContextMenuExecuteEvent aEvent) {

        try {

            // Retrieve context menu container and query for service factory to
            // create sub menus, menu entries and separators
            XIndexContainer xContextMenu = aEvent.ActionTriggerContainer;

            // if it is not the slide-pane context menu we do nothing here
            XPropertySet firstMenuEntry = cast(XPropertySet.class, xContextMenu.getByIndex(1));
            if (!isMenuEntry(firstMenuEntry))
                return ContextMenuInterceptorAction.IGNORED;
            if (!firstMenuEntry.getPropertyValue("CommandURL").equals("slot:27080"))
                return ContextMenuInterceptorAction.IGNORED;

            XMultiServiceFactory xMenuElementFactory = cast(XMultiServiceFactory.class, xContextMenu);

            if (xMenuElementFactory == null)
                return ContextMenuInterceptorAction.IGNORED;

            // create root menu entry for sub menu and sub menu
            XPropertySet xRootMenuEntry =
                    cast(XPropertySet.class, xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));

            // create a line separator for our new help sub menu
            XPropertySet xSeparator =
                    cast(XPropertySet.class, xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));

            Short aSeparatorType = ActionTriggerSeparatorType.LINE;
            xSeparator.setPropertyValue("SeparatorType", aSeparatorType);

            // query sub menu for index container to get access
            com.sun.star.container.XIndexContainer xSubMenuContainer =
                    cast(XIndexContainer.class, xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerContainer"));

            // intialize root menu entry "Help"
            xRootMenuEntry.setPropertyValue("Text", "OpenCards");
            xRootMenuEntry.setPropertyValue("CommandURL", "slot:5410");
//            xRootMenuEntry.setPropertyValue("HelpURL", "5410");
            xRootMenuEntry.setPropertyValue("SubContainer", xSubMenuContainer);

            // create menu entries for the new sub menu

            XPropertySet xMenuEntry = cast(XPropertySet.class, xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
            xMenuEntry.setPropertyValue("Text", Utils.getRB().getString("OpenOffice.cxtmenu.configure"));
            xMenuEntry.setPropertyValue("CommandURL", "info.opencards:confcard");
            xSubMenuContainer.insertByIndex(0, xMenuEntry);

            xMenuEntry = cast(XPropertySet.class, xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
            xMenuEntry.setPropertyValue("Text", Utils.getRB().getString("OpenCardsUI.resetStacksButton.text"));
            xMenuEntry.setPropertyValue("CommandURL", "info.opencards:resetcard");
            xSubMenuContainer.insertByIndex(1, xMenuEntry);

            // add new sub menu into the given context menu (add separator before)
            xContextMenu.insertByIndex(xContextMenu.getCount() - 1, xSeparator);
            xContextMenu.insertByIndex(xContextMenu.getCount() - 1, xRootMenuEntry);

            // The controller should execute the modified context menu and stop notifying other interceptors.
            return ContextMenuInterceptorAction.EXECUTE_MODIFIED;

        } catch (Throwable ex) {
            // catch java exceptions - do something useful
        }

        return ContextMenuInterceptorAction.IGNORED;
    }


    static public boolean isMenuEntry(XPropertySet xMenuElement) {
        com.sun.star.lang.XServiceInfo xServiceInfo =
                (com.sun.star.lang.XServiceInfo) UnoRuntime.queryInterface(
                        com.sun.star.lang.XServiceInfo.class, xMenuElement);

        return xServiceInfo.supportsService("com.sun.star.ui.ActionTrigger");
    }
}
