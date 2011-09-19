package info.opencards.ui.actions;

import info.opencards.Utils;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class HelpAction extends URLAction {

    public static final String OC_BASE = "http://www.opencards.info/index.php";


    public HelpAction() {
        this("help");
    }


    public HelpAction(String helpSection) {
        this(Utils.getRB().getString("General.help"), helpSection);
    }


    public HelpAction(String actionName, String helpItem) {
        super(actionName, OC_BASE + "/" + helpItem);
    }
}
