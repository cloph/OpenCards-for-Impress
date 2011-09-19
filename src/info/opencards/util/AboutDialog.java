/*
 * Created by JFormDesigner on Sat Jun 30 21:10:08 CEST 2007
 */

package info.opencards.util;

import info.opencards.Utils;
import info.opencards.ui.actions.ContributeAction;
import info.opencards.ui.actions.URLAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;


/**
 * @author Holger Brandl
 */
public class AboutDialog extends JDialog {

    public static final String OPENCARDS_VERSION = "1.5.1";


    public AboutDialog(Dialog owner) {
        super(owner);
        initComponents();

        sfNetButton.setAction(new URLAction("", new ImageIcon(Utils.getOCClass().getResource("/resources/icons/sflogo.png")), "https://sourceforge.net/projects/opencards/"));

        ocLabelIcon.setIcon(new ImageIcon(Utils.getOCClass().getResource(("/resources/oc-biglogo.png"))));
        ocLabelIcon.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                new URLAction(null, "http://www.opencards.info").actionPerformed(null);
            }
        });

        contributeButton.setAction(new ContributeAction(Utils.getRB().getString("AboutDialog.contributeButton.text")));

        String text = "<html><p><b>Version: " + OPENCARDS_VERSION +
                "</b></p>" +
                "</p></p>" +
                "<p><br>Homepage:     http://www.opencards.info</p>" +
                "<p></p>" +
                "<p>OpenCards is published under BSD-style license.</p>" +
                "<p>Copyright © 2008 Holger Brandl and contributors.</p>" +
                "<p></p>" +
                "<p>Contains XStream © 2003-2008 Joe Walnes.</p>" +
                "<p>Contains JFreeChart © 2008 Object Refinery Limited</p>" +
                "</html>";


        infoText.setText(text);

        addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                contributeButton.requestFocus();
            }
        });

        Utils.actionOnEsc(this, new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                closeButtonActionPerformed(null);
            }
        });
    }


    private void closeButtonActionPerformed(ActionEvent e) {
        dispose();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        JPanel panel3 = new JPanel();
        sfNetButton = new JButton();
        contributeButton = new JButton();
        closeButton = new JButton();
        JPanel contentPanel = new JPanel();
        infoText = new JLabel();
        ocLabelIcon = new JLabel();

        //======== this ========
        setResizable(false);
        setTitle(bundle.getString("AboutDialog.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel3 ========
        {
            panel3.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel3.setLayout(new GridBagLayout());
            ((GridBagLayout) panel3.getLayout()).columnWidths = new int[]{0, 0, 0, 1, 95, 0};
            ((GridBagLayout) panel3.getLayout()).rowHeights = new int[]{0, 0};
            ((GridBagLayout) panel3.getLayout()).columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout) panel3.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

            //---- sfNetButton ----
            sfNetButton.setIcon(null);
            panel3.add(sfNetButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            //---- contributeButton ----
            contributeButton.setText(bundle.getString("AboutDialog.contributeButton.text"));
            panel3.add(contributeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            //---- closeButton ----
            closeButton.setText(bundle.getString("General.close"));
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    closeButtonActionPerformed(e);
                }
            });
            panel3.add(closeButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel3, BorderLayout.SOUTH);

        //======== contentPanel ========
        {
            contentPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
            contentPanel.setLayout(new BorderLayout());

            //---- infoText ----
            infoText.setBorder(new EmptyBorder(20, 25, 25, 20));
            contentPanel.add(infoText, BorderLayout.CENTER);

            //---- ocLabelIcon ----
            ocLabelIcon.setIcon(new ImageIcon(getClass().getResource("/resources/oc-biglogo.png")));
            ocLabelIcon.setBorder(new EtchedBorder());
            contentPanel.add(ocLabelIcon, BorderLayout.NORTH);
        }
        contentPane.add(contentPanel, BorderLayout.CENTER);
        setSize(485, 420);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JButton sfNetButton;
    private JButton contributeButton;
    private JButton closeButton;
    private JLabel infoText;
    private JLabel ocLabelIcon;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
