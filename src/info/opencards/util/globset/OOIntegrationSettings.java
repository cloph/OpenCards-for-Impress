/*
 * Created by JFormDesigner on Sun Aug 12 19:47:43 CEST 2007
 */

package info.opencards.util.globset;

import info.opencards.Utils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ResourceBundle;


/**
 * @author Holger Brandl
 */
public class OOIntegrationSettings extends AbstractSettingsPanel {

    public static final String DO_HIDE_SLIDESORTER = "oointegration.hideSlideSorter";
    public static final Boolean DO_HIDE_SLIDESORTER_DEFAULT = true;


    public OOIntegrationSettings() {
        initComponents();
    }


    void resetPanelSettings() {
        Utils.getPrefs().remove(DO_HIDE_SLIDESORTER);

        loadDefaults();
    }


    void applySettingsChanges() {
        Utils.getPrefs().putBoolean(DO_HIDE_SLIDESORTER, hideSliSorCheckbox.isSelected());

    }


    protected void loadDefaults() {
        hideSliSorCheckbox.setSelected(Utils.getPrefs().getBoolean(DO_HIDE_SLIDESORTER, DO_HIDE_SLIDESORTER_DEFAULT));
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        panel1 = new JPanel();
        hideSliSorCheckbox = new JCheckBox();
        checkBox2 = new JCheckBox();
        checkBox1 = new JCheckBox();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{0, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{0, 0, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{1.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 1.0, 1.0E-4};

        //======== panel1 ========
        {
            panel1.setBorder(new CompoundBorder(
                    new TitledBorder("Impress Integration"),
                    new EmptyBorder(5, 5, 5, 5)));
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout) panel1.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) panel1.getLayout()).rowHeights = new int[]{0, 0, 0};
            ((GridBagLayout) panel1.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
            ((GridBagLayout) panel1.getLayout()).rowWeights = new double[]{0.0, 0.0, 1.0E-4};

            //---- hideSliSorCheckbox ----
            hideSliSorCheckbox.setText(bundle.getString("OOIntegrationSettings.hideSidePane"));
            panel1.add(hideSliSorCheckbox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
        }
        add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        //---- checkBox2 ----
        checkBox2.setText("Autorun slide animations");
        checkBox2.setEnabled(false);

        //---- checkBox1 ----
        checkBox1.setText("Do fullscreen learning");
        checkBox1.setEnabled(false);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        loadDefaults();
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JCheckBox hideSliSorCheckbox;
    private JCheckBox checkBox2;
    private JCheckBox checkBox1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
