/*
 * Created by JFormDesigner on Sun Jul 01 10:57:26 CEST 2007
 */

package info.opencards.ui.lastmin;

import info.opencards.Utils;
import info.opencards.learnstrats.leitner.*;
import info.opencards.util.globset.LeitnerSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


/**
 * @author Holger Brandl
 */
public class DefaultStrategyPanel extends JPanel {

    public static final String LEITLEARN_TIME_LIMIT = "leitlearn.timeLimitSpinner";
    public static final String LEITLEARN_CARD_LIMIT = "leitlearn.cardLimitSpinner";
    public static final String LEITLEARN_REVERSE_CARDS = "leitlearn.reverseCardsSides";
    public static final String LEITLEARN_MODE = "leitlearn.mode";
    private LeitnerSystem leitnerSystem;


    public DefaultStrategyPanel(LeitnerSystem leitnerSystem) {
        this.leitnerSystem = leitnerSystem;
        initComponents();
    }


    public LeitnerLearnMethodFactory getLearnMethodFactory() {
        serializeLearnConf2Session();

        if (limitTimeOption.isSelected()) {
            return TimeLimitLeitner.getFactory((Integer) timeLimitSpinner.getValue());
        } else if (limitCardsOption.isSelected()) {
            return CardLimitLeitner.getFactory((Integer) cardLimitSpinner.getValue());
        } else if (learnAllOption.isSelected()) {
            return LearnAllLeitner.getFactory();
        }

        throw new RuntimeException("nothing is selected");
    }


    private void serializeLearnConf2Session() {
        leitnerSystem.setProperty(LEITLEARN_TIME_LIMIT, timeLimitSpinner.getValue());
        leitnerSystem.setProperty(LEITLEARN_CARD_LIMIT, cardLimitSpinner.getValue());

        if (limitTimeOption.isSelected()) {
            leitnerSystem.setProperty(LEITLEARN_MODE, 0);

        } else if (limitCardsOption.isSelected()) {
            leitnerSystem.setProperty(LEITLEARN_MODE, 1);

        } else if (learnAllOption.isSelected()) {
            leitnerSystem.setProperty(LEITLEARN_MODE, 3);
        }
    }


    public void saveDefaults() {
        Preferences prefs = Utils.getPrefs();
        prefs.putInt(LEITLEARN_TIME_LIMIT, (Integer) timeLimitSpinner.getValue());
        prefs.putInt(LEITLEARN_CARD_LIMIT, (Integer) cardLimitSpinner.getValue());

        if (limitTimeOption.isSelected()) {
            prefs.putInt(LEITLEARN_MODE, 1);

        } else if (limitCardsOption.isSelected()) {
            prefs.putInt(LEITLEARN_MODE, 2);

        } else if (learnAllOption.isSelected()) {
            prefs.putInt(LEITLEARN_MODE, 0);
        }

        Utils.flushPrefs();
    }


    public void restoreDefaults() {
        boolean usePresSettings = Utils.getPrefs().getBoolean(LeitnerSettings.USE_PERSISTENT_LEARN_SETTINGS, LeitnerSettings.USE_PERSISTENT_LEARN_SETTINGS_DEFAULT);

        // check whether the session contains learn-settings amd whether we should prefer file instead of system settings
        if (usePresSettings && leitnerSystem.getProps().containsKey(LEITLEARN_MODE)) {
            restorePreviousSessionConf(leitnerSystem);
        } else {
            Preferences prefs = Utils.getPrefs();

            timeLimitSpinner.setValue(prefs.getInt(LEITLEARN_TIME_LIMIT, 10));
            cardLimitSpinner.setValue(prefs.getInt(LEITLEARN_CARD_LIMIT, 10));

            int mode = prefs.getInt(LEITLEARN_MODE, 0);

            switch (mode) {
                case 0:
                    learnAllOption.setSelected(true);
                    break;
                case 1:
                    limitTimeOption.setSelected(true);
                    break;
                case 2:
                    limitCardsOption.setSelected(true);
                    break;
            }
        }
    }


    private void restorePreviousSessionConf(LeitnerSystem leitnerSystem) {
        timeLimitSpinner.setValue(leitnerSystem.getProperty(LEITLEARN_TIME_LIMIT, null));
        cardLimitSpinner.setValue(leitnerSystem.getProperty(LEITLEARN_CARD_LIMIT, null));

        int mode = (Integer) leitnerSystem.getProperty(LEITLEARN_MODE, null);
        switch (mode) {
            case 0:
                limitTimeOption.setSelected(true);
                break;
            case 1:
                limitCardsOption.setSelected(true);
                break;
            case 2:
                learnAllOption.setSelected(true);
                break;
        }
    }


    public String toString() {
        return "Leitner System";
    }


    private void limitTimeOptionItemStateChanged() {
        timeLimitSpinner.setEnabled(limitTimeOption.isSelected());
    }


    private void limitCardsOptionItemStateChanged() {
        cardLimitSpinner.setEnabled(limitCardsOption.isSelected());
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        panel2 = new JPanel();
        learnAllOption = new JRadioButton();
        limitTimeOption = new JRadioButton();
        JLabel timeLimitLabel = new JLabel();
        timeLimitSpinner = new JSpinner();
        JLabel minutesLabel = new JLabel();
        limitCardsOption = new JRadioButton();
        JLabel cardLimitLabel = new JLabel();
        cardLimitSpinner = new JSpinner();
        JLabel cardsLabel = new JLabel();
        sessionTypeGroup = new ButtonGroup();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout) panel2.getLayout()).columnWidths = new int[]{25, 55, 52, 0, 0};
            ((GridBagLayout) panel2.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 20, 0};
            ((GridBagLayout) panel2.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0E-4};
            ((GridBagLayout) panel2.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- learnAllOption ----
            learnAllOption.setText(bundle.getString("DefaultStrategyPanel.learnAllOption.text"));
            learnAllOption.setSelected(true);
            panel2.add(learnAllOption, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- limitTimeOption ----
            limitTimeOption.setText(bundle.getString("DefaultStrategyPanel.limitTimeOption.text"));
            limitTimeOption.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    limitTimeOptionItemStateChanged();
                }
            });
            panel2.add(limitTimeOption, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- timeLimitLabel ----
            timeLimitLabel.setText(bundle.getString("DefaultStrategyPanel.timeLimitLabel.text"));
            panel2.add(timeLimitLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- timeLimitSpinner ----
            timeLimitSpinner.setModel(new SpinnerNumberModel(10, 1, null, 1));
            timeLimitSpinner.setEnabled(false);
            panel2.add(timeLimitSpinner, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- minutesLabel ----
            minutesLabel.setText(bundle.getString("DefaultStrategyPanel.minutesLabel.text"));
            panel2.add(minutesLabel, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- limitCardsOption ----
            limitCardsOption.setText(bundle.getString("DefaultStrategyPanel.limitCardsOption.text"));
            limitCardsOption.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    limitCardsOptionItemStateChanged();
                }
            });
            panel2.add(limitCardsOption, new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- cardLimitLabel ----
            cardLimitLabel.setText(bundle.getString("DefaultStrategyPanel.cardLimitLabel.text"));
            panel2.add(cardLimitLabel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- cardLimitSpinner ----
            cardLimitSpinner.setModel(new SpinnerNumberModel(10, 1, null, 1));
            cardLimitSpinner.setEnabled(false);
            panel2.add(cardLimitSpinner, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- cardsLabel ----
            cardsLabel.setText(bundle.getString("DefaultStrategyPanel.cardsLabel.text"));
            panel2.add(cardsLabel, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
        }
        add(panel2, BorderLayout.CENTER);

        //---- sessionTypeGroup ----
        sessionTypeGroup = new ButtonGroup();
        sessionTypeGroup.add(learnAllOption);
        sessionTypeGroup.add(limitTimeOption);
        sessionTypeGroup.add(limitCardsOption);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        restoreDefaults();
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel2;
    private JRadioButton learnAllOption;
    private JRadioButton limitTimeOption;
    private JSpinner timeLimitSpinner;
    private JRadioButton limitCardsOption;
    private JSpinner cardLimitSpinner;
    private ButtonGroup sessionTypeGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}