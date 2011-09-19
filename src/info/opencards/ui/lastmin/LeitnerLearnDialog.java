package info.opencards.ui.lastmin;

import info.opencards.core.Item;
import info.opencards.core.PresenterProxy;
import info.opencards.learnstrats.leitner.LeitnerSystem;
import info.opencards.ui.AbstractLearnDialog;
import info.opencards.util.LayoutRestorer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class LeitnerLearnDialog extends AbstractLearnDialog {

    public LeitnerStatePanel boxPanel;


    public LeitnerLearnDialog(Frame owner, PresenterProxy presProxy) {
        super(owner, presProxy);

        scoreButtonsContainer.setLayout(new GridLayout(1, 2));

        // rename here directly if because with two buttons it should be always leitner
        scoreButtonsContainer.add(fiveButton);
        scoreButtonsContainer.add(oneButton);

        validateTree();

        final KeyEventPostProcessor keyEventProc = new KeyEventPostProcessor() {

            public boolean postProcessKeyEvent(KeyEvent e) {
                if (!e.paramString().startsWith("KEY_RELEASED"))
                    return false;

                if (isShowingComplete()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            fiveButton.getAction().actionPerformed(null);
                            break;
                        case KeyEvent.VK_RIGHT:
                            oneButton.getAction().actionPerformed(null);
                            break;
                    }

                    return true;
                }

                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyEventProc);
        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyEventProc);
            }
        });

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration().getBounds();
        Rectangle defBounds = new Rectangle((int) (screenBounds.getWidth() + screenBounds.getX()) - 530, (int) (screenBounds.getHeight() + screenBounds.getY() - 240), 510, 220);
        setBounds(LayoutRestorer.getInstance().getBounds("leitnerLearnUI", this, defBounds));
    }


    protected void showCompleteCardButtonActionPerformed() {
        super.showCompleteCardButtonActionPerformed();
        fiveButton.requestFocusInWindow();
    }


    public void score(Item item) {
        super.score(item);

        // update the box-panel
        boxPanel.higlightItem(item);
    }


    public void setLeitnerSystem(LeitnerSystem leitnerSystem) {
        boxPanel = new LeitnerStatePanel();
        boxPanel.setLeitnerSystem(leitnerSystem);
        boxPanel.setPreferredSize(new Dimension(100, 100));
        boxPanel.setMinimumSize(new Dimension(0, 0));
        learnGraphContainer.add(boxPanel);

        validateTree();
    }
}
