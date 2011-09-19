package info.opencards.ui;

import info.opencards.Utils;
import info.opencards.core.PresenterProxy;
import info.opencards.learnstrats.ltm.LTMItem;
import info.opencards.learnstrats.ltm.ScheduleUtils;
import info.opencards.util.LayoutRestorer;
import info.opencards.util.globset.GlobLearnSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class SMLearnDialog extends AbstractLearnDialog {

    private Map<Integer, Boolean> keystates = new HashMap<Integer, Boolean>();


    public SMLearnDialog(Frame owner, PresenterProxy presProxy) {
        super(owner, presProxy);

        boolean useSimpleInterface = Utils.getPrefs().getBoolean(GlobLearnSettings.USE_SIMPLIFIED_INTERFACE, false);
        scoreButtonsContainer.setLayout(new GridLayout(1, useSimpleInterface ? 3 : 5));

        if (useSimpleInterface) {
            scoreButtonsContainer.add(fiveButton);
            scoreButtonsContainer.add(threeButton);
            scoreButtonsContainer.add(oneButton);
        } else {
            scoreButtonsContainer.add(fiveButton);
            scoreButtonsContainer.add(fourButton);
            scoreButtonsContainer.add(threeButton);
            scoreButtonsContainer.add(twoButton);
            scoreButtonsContainer.add(oneButton);
        }

        validateTree();
//        ((CardLayout) controlContainer.getLayout()).next(controlContainer);

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration().getBounds();
        Rectangle defBounds = new Rectangle((int) (screenBounds.getWidth() + screenBounds.getX()) - 530, (int) (screenBounds.getHeight() + screenBounds.getY() - 160), 510, 120);
        setBounds(LayoutRestorer.getInstance().getBounds("scheduledLearnUI", this, defBounds));


        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                double boundsRatio = getSizeRatio();

                boolean useSimpleInterface = Utils.getPrefs().getBoolean(GlobLearnSettings.USE_SIMPLIFIED_INTERFACE, false);

                if (boundsRatio > 1) {
                    scoreButtonsContainer.setLayout(new GridLayout(1, useSimpleInterface ? 3 : 5));
                } else {
                    scoreButtonsContainer.setLayout(new GridLayout(useSimpleInterface ? 3 : 5, 1));
                    progressBar.setString(progressBar.getString().split(" ")[0]);
                }

                validateTree();
                invalidate();
                repaint();
            }
        });
    }


    public boolean postProcessKeyEvent(KeyEvent e) {
//        super.postProcessKeyEvent(e);
        if (super.postProcessKeyEvent(e))
            return true;

        int keyCode = e.getKeyCode();

        if (!isShowingComplete())
            return false;

        if (e.paramString().startsWith("KEY_RELEASED")) {
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_RIGHT:
                    keystates.put(keyCode, false);
            }

            // test wether only one arrow-key was pressed and became released now
            Set<Integer> curKeys = keystates.keySet();
            if (keystates.size() == 1 && Utils.isAllFalse(keystates.values())) {
                switch (curKeys.iterator().next()) {
                    case KeyEvent.VK_LEFT:
                        fiveButton.getAction().actionPerformed(null);
                        keystates.clear();
                        break;
                    case KeyEvent.VK_DOWN:
                        threeButton.getAction().actionPerformed(null);
                        keystates.clear();
                        break;
                    case KeyEvent.VK_RIGHT:
                        oneButton.getAction().actionPerformed(null);
                        keystates.clear();
                        break;
                }

            } else if (keystates.size() == 2 && Utils.isAllFalse(keystates.values())) {
                if (!keystates.containsKey(KeyEvent.VK_DOWN)) {
                    keystates.clear();
                } else {
                    if (curKeys.contains(KeyEvent.VK_LEFT)) {
                        fourButton.getAction().actionPerformed(null);
                        keystates.clear();
                    } else if (curKeys.contains(KeyEvent.VK_RIGHT)) {
                        twoButton.getAction().actionPerformed(null);
                        keystates.clear();
                    }
                }
            }
        } else {
            // reset the keystate if some other keys were pressed before which didn't became
            if (Utils.isAllFalse(keystates.values()))
                keystates.clear();

            // update the current keystate
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_RIGHT:
                    keystates.put(keyCode, true);
            }
        }

        return false;
    }


    public double getSizeRatio() {
        return (double) getWidth() / (double) getHeight();
    }


    protected void showCompleteCardButtonActionPerformed() {
        super.showCompleteCardButtonActionPerformed();
        threeButton.requestFocusInWindow();
        keystates.clear();

        // set meaningful tooltips for the feedback-buttons if the option was selected in the OC-settings
        boolean showFeedbackToTips = Utils.getPrefs().getBoolean(GlobLearnSettings.DO_SHOW_FEEBACK_TOTIPS, GlobLearnSettings.DO_SHOW_FEEBACK_TOTIPS_DEFAULT);
        if (showFeedbackToTips) {
            List<JButton> fbButtons = Arrays.asList(oneButton, twoButton, threeButton, fourButton, fiveButton);
            LTMItem ltmItem = (LTMItem) curItem;

            Map<Integer, Integer> logIncDaysList = new LinkedHashMap<Integer, Integer>();

            for (JButton fbButton : fbButtons) {
                int actionScore = ((ScoreAction) fbButton.getAction()).getActionScore();
                LTMItem cloneItem = (LTMItem) ltmItem.clone();
                cloneItem.updateEFactor(actionScore);

                int days = ScheduleUtils.getDayDiff(cloneItem.getNextScheduledDate(), ScheduleUtils.getToday());
                logIncDaysList.put(actionScore, days);

                // set the appropriate tooltip
                DecimalFormat df = new DecimalFormat("##.##");
                String toolTipText = null;
                String nextTest = "--> next test in " + days + " days";
                ResourceBundle rb = Utils.getRB();

                switch (actionScore) {
                    case 1:
                        toolTipText = rb.getString("SMLearnDialog.feedback.notatall");
                        break;
                    case 2:
                        toolTipText = rb.getString("SMLearnDialog.feedback.hardly");
                        break;
                    case 3:
                        toolTipText = rb.getString("SMLearnDialog.feedback.soso") + nextTest;
                        break;
                    case 4:
                        toolTipText = rb.getString("SMLearnDialog.feedback.well") + nextTest;
                        break;
                    case 5:
                        toolTipText = rb.getString("SMLearnDialog.feedback.perfectly") + nextTest;
                        break;
                    default:
                        toolTipText = "!!!invalid feeback-code!!!";
                }

                fbButton.setToolTipText(toolTipText);
            }

            // print a debug message to the log-monitor
            StringBuffer sb = new StringBuffer();
            for (Integer actionCode : logIncDaysList.keySet())
                sb.append(actionCode + "->" + logIncDaysList.get(actionCode) + ";");
            Utils.log(sb.toString());
        }
    }
}
