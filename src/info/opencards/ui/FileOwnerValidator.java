/*
 * Created by JFormDesigner on Mon Apr 21 22:35:12 CEST 2008
 */

package info.opencards.ui;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.thoughtworks.xstream.XStream;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.FlashCardCollection;
import info.opencards.learnstrats.leitner.LeitnerItem;
import info.opencards.learnstrats.ltm.LTMItem;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;
import info.opencards.ui.actions.CardFileResetAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Tests whether a card-file is registered to the current user. To do so each oc-installation gets assigned a random id
 * which is supposed to be unique. This ID becomes embedded into all created flashcard-files. Whenever a file is opened
 * OC checks whether the file-id equals the system-id. If this is not the case the user can <ol><li>Reset the learning
 * state of the file (and change the id to the systems id) <li> Add the id to a list of accepted ids</ol>
 *
 * @author Holger Brandl
 */
public class FileOwnerValidator extends JDialog {

    private static final String OC_USER_ID = "OpenCards-UserID";


    int resultCode = -1;
    private static final int ADAPT_OPTION = 112;
    private static final int RESET_OPTION = 113;
    private static final int DO_NOTHING = 114;


    public static void ensureFileOwnership(CardFile cardFile, JDialog parent) {
        FlashCardCollection faco = cardFile.getFlashCards();

        Long facoUserID = (Long) faco.getProperty(OC_USER_ID, null);

        if (facoUserID == null) { // ensures backward compatibility of OC
            faco.setProperty(OC_USER_ID, getSystemUserID());
            facoUserID = getSystemUserID();

            cardFile.flush();
        }

        if (facoUserID.equals(getSystemUserID())) {
            // we're already done if both numbers are already matching
            return;
        }

        // if this line is reached the system id and the file id do NOT match.
        FileOwnerValidator fileOwnerValidator = new FileOwnerValidator(parent);

        // replace the wildcard filename with the actual filename of the current cardfile
        String baseMsgTxt = Utils.getRB().getString("FileOwnerValidator.msgText");
        String newMsgText = baseMsgTxt.replace("<<filename>>", "'" + cardFile.getFileLocation().getName() + "'");
        fileOwnerValidator.msgText.setText(newMsgText);

        fileOwnerValidator.setVisible(true);

        if (fileOwnerValidator.resultCode == RESET_OPTION) {
            // reset the learning state of the current falshcard collections
            faco.setProperty(OC_USER_ID, getSystemUserID());
            new CardFileResetAction(Arrays.asList(LTMItem.class, LeitnerItem.class)).resetCardFile(cardFile);

        } else if (fileOwnerValidator.resultCode == ADAPT_OPTION) {
            // update the system-id to the user-id
            setSystemUserID(facoUserID);

        } else if (fileOwnerValidator.resultCode == JOptionPane.CANCEL_OPTION) {
            System.out.println("do nothing");
        }
    }


    public FileOwnerValidator(Frame owner) {
        super(owner);

        initComponents();
        Utils.closeOnEsc(this, true);
    }


    public FileOwnerValidator(Dialog owner) {
        super(owner);
        initComponents();

        resetFileButton.requestFocusInWindow();
    }


    private void doNothingButtonActionPerformed(ActionEvent e) {
        resultCode = DO_NOTHING;
        dispose();
    }


    private void adaptIdButtonActionPerformed(ActionEvent e) {
        resultCode = ADAPT_OPTION;
        dispose();
    }


    private void resetFileButtonActionPerformed(ActionEvent e) {
        resultCode = RESET_OPTION;
        dispose();
    }


    public static Long getSystemUserID() {
        try {
            Preferences prefs = Utils.getPrefs();
            if (!Arrays.asList(prefs.keys()).contains(OC_USER_ID))
                setSystemUserID(new Random().nextLong());

            long sysID = prefs.getLong(OC_USER_ID, -1);
            if (sysID == -1)
                throw new RuntimeException();

            return sysID;
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }

    }


    public static void setSystemUserID(long newSystemID) {
        Utils.getPrefs().putLong(OC_USER_ID, newSystemID);
//        Utils.flushPrefs();
    }


    public static void main(String[] args) {
//        CardFile cardFile = loadCardFileFromFile(Utils.copyFile(new File("testdata/testpres.odp"), new File("lalala.odp")));
//        randomizeID(cardFile);

//        FileOwnerValidator.ensureFileOwnership(cardFile, new JDialog());
        Utils.getPrefs().putInt("holgitest", 47);
        Utils.getPrefs().putInt("holgitest2", 48);
        Utils.getPrefs().put("testrect", new XStream().toXML(new Rectangle(3, 2, 3, 4)));

//        cardFile.flush();
    }


    private static void randomizeID(CardFile cardFile) {
        cardFile.getFlashCards().setProperty(OC_USER_ID, new Random().nextLong());
    }


    public static CardFile loadCardFileFromFile(File file) {
        XComponentContext xContext = null;
        try {
            xContext = Bootstrap.bootstrap();
        } catch (BootstrapException e) {
            e.printStackTrace();
        }

        OpenOfficeUtils.getLocale(xContext);
        assert xContext != null;
        XMultiComponentFactory xMCF = xContext.getServiceManager();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");

        // create an odp with a non-matching user-id
        String fileURL = Utils.convert2OOurl(file);
        OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

        CardFile cardFile = new CardFile(file);
        cardFile.setSerializer(new ImpressSerializer(xContext));

        return cardFile;
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        label1 = new JLabel();
        msgText = new JLabel();
        buttonBar = new JPanel();
        doNothingButton = new JButton();
        adaptIdButton = new JButton();
        resetFileButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("FileOwnerValidator.this.title"));
        setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BorderLayout());

                //======== panel1 ========
                {
                    panel1.setBorder(new EmptyBorder(5, 5, 5, 25));
                    panel1.setLayout(new BorderLayout());

                    //---- label1 ----
                    label1.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
                    panel1.add(label1, BorderLayout.CENTER);
                }
                contentPanel.add(panel1, BorderLayout.WEST);

                //---- msgText ----
                msgText.setText(bundle.getString("FileOwnerValidator.msgText"));
                contentPanel.add(msgText, BorderLayout.CENTER);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 0, 0, 0};
                ((GridBagLayout) buttonBar.getLayout()).rowHeights = new int[]{0, 0};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 1.0, 1.0, 1.0E-4};
                ((GridBagLayout) buttonBar.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

                //---- doNothingButton ----
                doNothingButton.setText(bundle.getString("FileOwnerValidator.doNothingButton.text"));
                doNothingButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doNothingButtonActionPerformed(e);
                    }
                });
                buttonBar.add(doNothingButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- adaptIdButton ----
                adaptIdButton.setText(bundle.getString("FileOwnerValidator.adaptIdButton.text"));
                adaptIdButton.setToolTipText(bundle.getString("FileOwnerValidator.adaptIdButton.toolTipText"));
                adaptIdButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adaptIdButtonActionPerformed(e);
                    }
                });
                buttonBar.add(adaptIdButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- resetFileButton ----
                resetFileButton.setText(bundle.getString("OpenCardsUI.resetStacksButton.text"));
                resetFileButton.setFont(resetFileButton.getFont().deriveFont(resetFileButton.getFont().getStyle() | Font.BOLD));
                resetFileButton.setSelected(true);
                resetFileButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        resetFileButtonActionPerformed(e);
                    }
                });
                buttonBar.add(resetFileButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JLabel label1;
    private JLabel msgText;
    private JPanel buttonBar;
    private JButton doNothingButton;
    private JButton adaptIdButton;
    private JButton resetFileButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
