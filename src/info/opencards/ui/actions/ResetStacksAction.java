package info.opencards.ui.actions;

import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.learnstrats.leitner.LeitnerSystem;
import info.opencards.util.globset.LeitnerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ResetStacksAction extends AbstractAction {

    private CardFile cardFile;
    private LeitnerSystem leitnerSystem;
    private CardFileBackend backend;
    private Window awtParent;


    public ResetStacksAction(CardFile cardFile, LeitnerSystem leitnerSystem, CardFileBackend backend, Window awtParent) {
        this.cardFile = cardFile;
        this.leitnerSystem = leitnerSystem;
        this.backend = backend;

        this.awtParent = awtParent;

        putValue(NAME, Utils.getRB().getString("OpenCardsUI.resetStacksButton.text"));
//        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
    }


    public void actionPerformed(ActionEvent e) {
        Preferences prefs = Utils.getPrefs();

        String confirmMsg = Utils.getRB().getString("CardFileResetAction.resetReally");
        String title = Utils.getRB().getString("ResetStacksAction.resetReallyTitle");

        int status = JOptionPane.showConfirmDialog(awtParent, confirmMsg, title, JOptionPane.YES_NO_OPTION);

        if (status == JOptionPane.OK_OPTION) {
            leitnerSystem.reconfigure(prefs.getInt(LeitnerSettings.NUM_LEITNER_BOXES, LeitnerSettings.NUM_LEITNER_BOXES_DEFAULT),
                    prefs.getInt(LeitnerSettings.INIT_LEITNER_BOXES, LeitnerSettings.INIT_LEITNER_BOXES_DEFAULT));
            leitnerSystem.reset();

            // this is not really necessary but should make the ui a little bit more snappy by awoiding to block
            // the awt-thread while serializing the reseted flashcard set to the odp-meta-data field
            new Thread() {

                public void run() {
                    cardFile.flush();
                }
            }.start();
        }
    }
}
