package info.opencards;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ModeListener {

    private Set<String> curModes = new HashSet<String>();
    private Component curUiParent;


    public boolean isInMode(String mode) {
        return curModes.contains(mode);
    }


    public void startedMode(String modeName, Component modeParent) {
        curModes.add(modeName);
        curUiParent = modeParent;
    }


    public void stoppedMode(String modeName) {
        curModes.remove(modeName);
        curUiParent = null;
    }


    public Component getCurrentUIParent() {
        return curUiParent;
    }


    public boolean containsMode(String... modes) {
        for (String mode : modes) {
            if (curModes.contains(mode))
                return true;
        }

        return false;
    }


    public boolean hasOpenModes() {
        return !curModes.isEmpty();
    }
}
