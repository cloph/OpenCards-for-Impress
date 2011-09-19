package info.opencards.ui.actions;

import info.opencards.Utils;
import info.opencards.util.AboutDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class AboutAction extends AbstractAction {

    private Dialog owner;


    public AboutAction(Dialog owner) {
        this.owner = owner;
        putValue(NAME, Utils.getRB().getString("OpenCardsUI.aboutButton.text"));
    }


    public void actionPerformed(ActionEvent e) {
        new AboutDialog(owner).setVisible(true);
    }
}
