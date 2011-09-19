package info.opencards.learnstrats.leitner;

import info.opencards.core.*;
import info.opencards.ui.AbstractLearnDialog;
import info.opencards.ui.lastmin.LeitnerLearnDialog;

import java.util.Collection;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class LeitnerProcessManager extends LearnProcessManager {

    public LeitnerProcessManager(CardFile cardFile, ItemValuater itemValuater, LeitnerLearnMethodFactory factory) {
        super(itemValuater, factory);

        this.lmFactory = factory;
        this.itemValuater = itemValuater;

        scheduler.clear();
        scheduler.put(cardFile, cardFile.getFlashCards().getLeitnerItems().getNotLastBoxCards());

        procIt = scheduler.keySet().iterator();

        if (itemValuater instanceof LeitnerLearnDialog) {
            ((LeitnerLearnDialog) itemValuater).setLeitnerSystem(cardFile.getFlashCards().getLeitnerItems());
        }
    }


    public void itemChanged(Item item, boolean stillOnSchedule, Integer feedback) {
    }


    public void processStatusInfo(String statusMsg, double completeness) {
        if (itemValuater instanceof AbstractLearnDialog) {
//            int completeness = (int) (100 * numProcessed / (double) numScheduled);
//            ((AbstractLearnDialog) itemValuater).updateStatus(completeness, (numScheduled - numProcessed) + " " + Utils.getRB().getString("AbstractLearnDialog.statusbar.text"));
            ((AbstractLearnDialog) itemValuater).updateStatus((int) completeness, statusMsg);
        }
    }


    public void setupSchedule(Collection<CardFile> curFiles) {
        procIt = scheduler.keySet().iterator();
    }


    protected ItemCollection getItemCollection(CardFile cardFile) {
        return cardFile.getFlashCards().getLeitnerItems();
    }
}
