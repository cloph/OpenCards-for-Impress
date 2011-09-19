package info.opencards.ui.actions;

import info.opencards.Utils;
import info.opencards.util.globset.SettingsDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class SettingsAction extends AbstractAction {

    private JDialog parentUI;


    public SettingsAction(JDialog parentUI) {
        putValue(NAME, Utils.getRB().getString("OpenCardsUI.settingsButton.text"));
        this.parentUI = parentUI;
    }


    public void actionPerformed(ActionEvent e) {
        new SettingsDialog(parentUI).setVisible(true);
    }
}
