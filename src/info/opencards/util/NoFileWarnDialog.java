/*
 * Created by JFormDesigner on Fri Sep 14 00:57:27 CEST 2007
 */

package info.opencards.util;

import info.opencards.Utils;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.ui.LearnManagerUI;
import info.opencards.ui.actions.LastMinLearnAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Holger Brandl
 */
public class NoFileWarnDialog extends JDialog {

    public static final String STOP_WARNING = "stopWarnNoFile";
    private Dialog owner;
    private CardFileBackend backend;
    private LearnManagerUI ltmUI;


    public NoFileWarnDialog(CardFileBackend backend, LearnManagerUI ltmUI) {
        super(backend.getOwner());

        this.backend = backend;
        this.ltmUI = ltmUI;

        initComponents();

        stopWarning.setSelected(skipWarning());
    }


    public static boolean skipWarning() {
        return Utils.getPrefs().getBoolean(STOP_WARNING, false);
    }


    public void dispose() {
        Utils.getPrefs().putBoolean(STOP_WARNING, stopWarning.isSelected());

        super.dispose();
    }


    private void saveNow() {
        dispose();
    }


    private void discardCurrentPresentation() {
        dispose();

        ltmUI.setVisible(true);
    }


    private void lastMinButtonActionPerformed() {
        dispose();

        new LastMinLearnAction(backend).actionPerformed(null);
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        textPane2 = new JTextPane();
        buttonBar = new JPanel();
        stopWarning = new JCheckBox();
        saveNowButton = new JButton();
        lastMinButton = new JButton();
        ignoreButton = new JButton();

        //======== this ========
        setTitle("Warning");
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new CardLayout());

                //---- textPane2 ----
                textPane2.setBackground(null);
                textPane2.setText("OpenCards is designed to ease the long-term learning of flash-card files. Therefore the LearnManger of OpenCards is only able to handle saved presentations. Your current presentation was not saved so far.  Because OpenCards will automatically load your scheduled presentations into the current frame, all changes in your current document will get lost if you continue. \n\nIf you would like to memorize your current presentation without saving it, please consier to use the \"last minute\" learning scheme of OpenCards. ");
                textPane2.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                textPane2.setEditable(false);
                contentPanel.add(textPane2, "card1");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 0, 85, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0};

                //---- stopWarning ----
                stopWarning.setText("Don't show this message again");
                buttonBar.add(stopWarning, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- saveNowButton ----
                saveNowButton.setText("Save now");
                saveNowButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveNow();
                    }
                });
                buttonBar.add(saveNowButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- lastMinButton ----
                lastMinButton.setText("LastMinute");
                lastMinButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        lastMinButtonActionPerformed();
                    }
                });
                buttonBar.add(lastMinButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- ignoreButton ----
                ignoreButton.setText("Ignore");
                ignoreButton.setFont(ignoreButton.getFont().deriveFont(ignoreButton.getFont().getStyle() | Font.BOLD));
                ignoreButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        discardCurrentPresentation();
                    }
                });
                buttonBar.add(ignoreButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(550, 250);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTextPane textPane2;
    private JPanel buttonBar;
    private JCheckBox stopWarning;
    private JButton saveNowButton;
    private JButton lastMinButton;
    private JButton ignoreButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
