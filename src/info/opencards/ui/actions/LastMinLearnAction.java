package info.opencards.ui.actions;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.ModeListener;
import info.opencards.OpenCards;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.backend.BackendFactory;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.ui.FileOwnerValidator;
import info.opencards.ui.lastmin.CramLernSettingsPanel;
import info.opencards.util.ScaleableIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class LastMinLearnAction extends AbstractAction {


    private CardFileBackend backend;
    private CardFile cardFile;


    public LastMinLearnAction(CardFileBackend backend) {
        assert backend != null;
        this.backend = backend;

        putValue(NAME, Utils.getRB().getString("OpenCardsUI.learnCardsButton.text"));
        putValue(SMALL_ICON, new ScaleableIcon("/resources/icons/5vorUhr.png"));

//        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
    }


    public void actionPerformed(ActionEvent e) {
        actionPerformed(e, null);
    }


    public void actionPerformed(Object o, ModeListener modeListener) {
        // make the setting dialog invisible
        // show the the learn setting dialog

        CardFile learnFile = cardFile;
        if (learnFile == null) {
            learnFile = new CardFile(null);
        } else {
            // make sure that the card file is opened
            backend.getPresProxy().openCardFile(learnFile);
        }

        learnFile.setSerializer(backend.getSerializer());

        CramLernSettingsPanel cramLearnPanel = new CramLernSettingsPanel(learnFile, backend);

        if (modeListener != null) {
            modeListener.startedMode(OpenCards.LASTMIN_MODE, cramLearnPanel);
            cramLearnPanel.addModeListener(modeListener);
        }

        cramLearnPanel.setVisible(true);

        if (learnFile.getFlashCards().size() == 0) {
            showOcSlideCompatibilityDialog(cramLearnPanel, Utils.getRB().getString("LastMinLearnAction.warnnoslides.title"));
        }

        // ensure that file-id and user-id match (but only if the file is a serialized file with a real location)
        if (learnFile.getFileLocation() != null)
            FileOwnerValidator.ensureFileOwnership(learnFile, cramLearnPanel);
    }


    public void setCardFile(CardFile cardFile) {
        this.cardFile = cardFile;

        setEnabled(cardFile != null);
    }


    public static void main(String[] args) throws BootstrapException {
        XComponentContext xContext = Bootstrap.bootstrap();

        File odpFile = Utils.createTempCopy(new File("testdata/testpres.odp"));
        System.out.println("processing " + odpFile);
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, Utils.convert2OOurl(odpFile));

        CardFileBackend backend = BackendFactory.getImpressBackend(new Frame(), xComponent, xContext);

        LastMinLearnAction lastMinAction = new LastMinLearnAction(backend);

        lastMinAction.setCardFile(new CardFile(odpFile));
//        lastMinAction.setCardFile(null);

        lastMinAction.actionPerformed(null);
    }


    public static void showOcSlideCompatibilityDialog(Component parent, String title) {
        ImageIcon icon = new ImageIcon(Utils.getOCClass().getResource(("/resources/icons/layout-warning.png")));
        JOptionPane.showMessageDialog(parent, Utils.getRB().getString("LastMinLearnAction.warnnoslides.description"),
                title, JOptionPane.WARNING_MESSAGE, icon);
    }
}
