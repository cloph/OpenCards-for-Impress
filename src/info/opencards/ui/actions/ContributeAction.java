package info.opencards.ui.actions;

/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ContributeAction extends URLAction {

    public static final String CONTRIBUTE_URL = "http://www.opencards.info/index.php/contribute-to-opencards";


    public ContributeAction(String name) {
        super(name, CONTRIBUTE_URL);
    }
}
