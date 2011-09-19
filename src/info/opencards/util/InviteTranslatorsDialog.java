/*
 * Created by JFormDesigner on Sun Dec 16 14:55:53 CET 2007
 */

package info.opencards.util;

import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.oimputils.OpenOfficeUtils;
import info.opencards.ui.actions.URLAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * @author Holger Brandl
 */
public class InviteTranslatorsDialog extends JDialog {

    public static final String SHOW_TRANLATE_INVITATION = "general.showTranlateInvitation";


    public InviteTranslatorsDialog(Frame owner) {
        super(owner);
        initComponents();


        String text = "<html><p> OpenCards was not yet localized to the interface language of your OpenOffice installation.</p>" +
                "</b></p>" +
                "<p><br>Translating OpenCards into your language is really simple and requires approximately 2 hours of time." +
                " So just click 'Tell me what to do' if you would like to help to translate OpenCards also into your language.</p>" +
                "<p></p>" +
                "<p>The OpenCards-team. :-)</p>" +
                "</html>";

        infoTextLabel.setText(text);

        addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                tellMeMoreButton.requestFocus();
            }
        });

        Utils.actionOnEsc(this, new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
    }


    private void cancelButtonActionPerformed() {
        dispose();
    }


    private void neverAgainButtonActionPerformed() {
        Utils.getPrefs().putBoolean(SHOW_TRANLATE_INVITATION, false);
        cancelButtonActionPerformed();
    }


    private void tellMeMoreButtonActionPerformed() {
        new URLAction("foobar", "http://www.opencards.info/index.php/contribute#translate").actionPerformed(null);
        cancelButtonActionPerformed();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        infoTextLabel = new JLabel();
        buttonBar = new JPanel();
        neverAgainButton = new JButton();
        cancelButton = new JButton();
        tellMeMoreButton = new JButton();

        //======== this ========
        setResizable(false);
        setTitle("No translation available");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BorderLayout());

                //---- infoTextLabel ----
                infoTextLabel.setText("text");
                contentPanel.add(infoTextLabel, BorderLayout.CENTER);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{180, 85, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0, 0.0};

                //---- neverAgainButton ----
                neverAgainButton.setText("Never show this again");
                neverAgainButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        neverAgainButtonActionPerformed();
                    }
                });
                buttonBar.add(neverAgainButton, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Close");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
                    }
                });
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- tellMeMoreButton ----
                tellMeMoreButton.setText("Tell me what to do");
                tellMeMoreButton.setFont(tellMeMoreButton.getFont().deriveFont(tellMeMoreButton.getFont().getStyle() | Font.BOLD));
                tellMeMoreButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        tellMeMoreButtonActionPerformed();
                    }
                });
                buttonBar.add(tellMeMoreButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(520, 250);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel infoTextLabel;
    private JPanel buttonBar;
    private JButton neverAgainButton;
    private JButton cancelButton;
    private JButton tellMeMoreButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static InviteTranslatorsDialog inviteForTranslation(XComponentContext m_xContext, Frame owner) {
        if (OpenOfficeUtils.getLocale(m_xContext) != null) {
            return null;
        }

        if (!Utils.getPrefs().getBoolean(SHOW_TRANLATE_INVITATION, true))
            return null;

        if (Utils.getPrefs().getInt(Utils.PROP_STARTUP_COUNTER, 0) < 2) {
            return null;
        }

        // show invitation to contribution-dialog (english only this time)
        InviteTranslatorsDialog dialog = new InviteTranslatorsDialog(owner);
        dialog.setVisible(true);

        return dialog;
    }


    public static void main(String[] args) {
        Utils.configureUI();
        new InviteTranslatorsDialog(null).setVisible(true);
    }
}
