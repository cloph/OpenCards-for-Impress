/*
 * Created by JFormDesigner on Sun Jul 01 00:35:36 CEST 2007
 */

package info.opencards.ui.lastmin;

import info.opencards.ModeListener;
import info.opencards.OpenCards;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.LearnProcListener;
import info.opencards.core.LearnProcessManager;
import info.opencards.core.backend.CardFileBackend;
import info.opencards.learnstrats.leitner.LeitnerLearnMethodFactory;
import info.opencards.learnstrats.leitner.LeitnerProcessManager;
import info.opencards.learnstrats.leitner.LeitnerSystem;
import info.opencards.ui.actions.CardFilePropsAction;
import info.opencards.ui.actions.ResetStacksAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Holger Brandl
 */
public class CramLernSettingsPanel extends JDialog {

    private DefaultStrategyPanel defStratPanel;
    private final CardFileBackend backend;
    private final CardFile cardFile;

    private List<ModeListener> modeListeners = new ArrayList<ModeListener>();


    public CramLernSettingsPanel(CardFile cardFile, CardFileBackend backend) {
        super(backend.getOwner());

        this.backend = backend;
        this.cardFile = cardFile;

        initComponents();
        if (cardFile == null)
            return;

        // set size and location
        setBounds(new Rectangle(450, 500));
        setLocationRelativeTo(null);

        cardFile.synchronize();
        LeitnerSystem leitnerSystem = cardFile.getFlashCards().getLeitnerItems();

        // register all available possible (multiple-)strategy panels
        leitnerStatePanel.setLeitnerSystem(leitnerSystem);

        defStratPanel = new DefaultStrategyPanel(leitnerSystem);
        stratPanelPanel.add(defStratPanel);
        stratPanelPanel.validate();

        resetBoxesButton.setAction(new ResetStacksAction(cardFile, leitnerSystem, backend, this));

        CardFilePropsAction propsAction = new CardFilePropsAction(defStratPanel, false, Utils.getRB().getString("CramLernSettingsPanel.fileProperties") + "...");
        propsAction.setCardFile(Arrays.asList(cardFile));
        configureFileButton.setAction(propsAction);

        Utils.closeOnEsc(this, true);
        Utils.helpOnF1(this, null);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    //todo we should move this to an action and disable the start learning button (+tooltip) as long as 0 items need to be learnt
    private void startLearningAction() {
        setVisible(false);

        LeitnerLearnMethodFactory factory = defStratPanel.getLearnMethodFactory();

        LearnProcessManager processManager = new LeitnerProcessManager(cardFile, new LeitnerLearnDialog(backend.getOwner(), backend.getPresProxy()), factory);

        LeitnerSystem leitnerSystem = cardFile.getFlashCards().getLeitnerItems();
        int numLeitnerCards = leitnerSystem.getAllCards().size();
        final boolean learningNecessary = numLeitnerCards > 0 && numLeitnerCards != leitnerSystem.getBox(leitnerSystem.numBoxes() - 1).size();

        processManager.addLearnProcessManagerProcessListener(new LearnProcListener() {
            public void processFinished(boolean wasInterrupted) {
                Utils.log("cramsets: finished learning process");

                if (!wasInterrupted && learningNecessary) {
                    // old behavior used prior to 0.16.3
//                    JOptionPane.showMessageDialog(new JFrame(), Utils.getRB().getString("CramLernSettingsPanel.sessionFinished"), "You did it !", JOptionPane.INFORMATION_MESSAGE);
                }

                for (ModeListener modeListener : modeListeners) {
                    modeListener.stoppedMode(OpenCards.LASTMIN_MODE);
                }

                Utils.log("cramsets: finished learning process - end");
            }
        });

        processManager.startProcessing();
    }


    private void cancelLearningAction() {
        dispose();
    }


    public void setVisible(boolean b) {
        startLearnButton.requestFocusInWindow();
        super.setVisible(b);
    }


    public void dispose() {
        super.dispose();

        for (ModeListener modeListener : modeListeners) {
            modeListener.stoppedMode(OpenCards.LASTMIN_MODE);
        }
    }


    private void setDefaultButtonActionPerformed() {
        defStratPanel.saveDefaults();
    }


    /**
     * Adds a new listener.
     */
    public void addModeListener(ModeListener l) {
        if (l == null)
            return;

        modeListeners.add(l);
    }


    /**
     * Removes a listener.
     */
    public void removeModeListener(ModeListener l) {
        if (l == null)
            return;

        modeListeners.remove(l);
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        panel4 = new JPanel();
        leitnerStatePanel = new LeitnerStatePanel();
        panel5 = new JPanel();
        configureFileButton = new JButton();
        resetBoxesButton = new JButton();
        stratPanelPanel = new JPanel();
        JPanel panel3 = new JPanel();
        setDefaultButton = new JButton();
        hSpacer1 = new JPanel(null);
        cancelButton = new JButton();
        startLearnButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("CramLernSettingsPanel.this.title"));
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel4 ========
        {
            panel4.setBorder(new TitledBorder(bundle.getString("LearnSettingsPanel.panel4.border")));
            panel4.setLayout(new BorderLayout());

            //---- leitnerStatePanel ----
            leitnerStatePanel.setMinimumSize(new Dimension(100, 100));
            leitnerStatePanel.setPreferredSize(new Dimension(100, 100));
            panel4.add(leitnerStatePanel, BorderLayout.CENTER);

            //======== panel5 ========
            {
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout) panel5.getLayout()).columnWidths = new int[]{0, 0, 0};
                ((GridBagLayout) panel5.getLayout()).rowHeights = new int[]{0, 0};
                ((GridBagLayout) panel5.getLayout()).columnWeights = new double[]{1.0, 1.0, 1.0E-4};
                ((GridBagLayout) panel5.getLayout()).rowWeights = new double[]{0.0, 1.0E-4};

                //---- configureFileButton ----
                configureFileButton.setText(bundle.getString("CardFileTable.cxtMenu.configureCards"));
                panel5.add(configureFileButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- resetBoxesButton ----
                resetBoxesButton.setText("Reset Cards");
                panel5.add(resetBoxesButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            panel4.add(panel5, BorderLayout.SOUTH);
        }
        contentPane.add(panel4, BorderLayout.NORTH);

        //======== stratPanelPanel ========
        {
            stratPanelPanel.setBorder(new TitledBorder(bundle.getString("LearnSettingsPanel.stratPanelPanel.border")));
            stratPanelPanel.setLayout(new BorderLayout());
        }
        contentPane.add(stratPanelPanel, BorderLayout.CENTER);

        //======== panel3 ========
        {
            panel3.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel3.setLayout(new GridBagLayout());
            ((GridBagLayout) panel3.getLayout()).columnWidths = new int[]{0, 0, 90, 100, 0};
            ((GridBagLayout) panel3.getLayout()).rowHeights = new int[]{0, 0};
            ((GridBagLayout) panel3.getLayout()).columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout) panel3.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

            //---- setDefaultButton ----
            setDefaultButton.setText(bundle.getString("CramLernSettingsPanel.setDefaultButton.text"));
            setDefaultButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setDefaultButtonActionPerformed();
                }
            });
            panel3.add(setDefaultButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 10), 0, 0));
            panel3.add(hSpacer1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 10), 0, 0));

            //---- cancelButton ----
            cancelButton.setText(bundle.getString("General.cancel"));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelLearningAction();
                }
            });
            panel3.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 10), 0, 0));

            //---- startLearnButton ----
            startLearnButton.setText(bundle.getString("StartLearningAction.StartLearning"));
            startLearnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startLearningAction();
                }
            });
            panel3.add(startLearnButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel3, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel4;
    private LeitnerStatePanel leitnerStatePanel;
    private JPanel panel5;
    private JButton configureFileButton;
    private JButton resetBoxesButton;
    private JPanel stratPanelPanel;
    private JButton setDefaultButton;
    private JPanel hSpacer1;
    private JButton cancelButton;
    private JButton startLearnButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}