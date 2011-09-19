package info.opencards.learnstrats.ltm;

import info.opencards.Utils;
import info.opencards.core.Item;
import info.opencards.util.globset.GlobLearnSettings;
import junit.framework.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Some utility-methods which ease date-computations.
 *
 * @author Holger Brandl
 */
public class ScheduleUtils {

    /**
     * Used for testing-purposes of the scheduling algorithm, where it is sometimes necessary to hack the value of
     * 'today'.
     */
    public static Date testToday;


    public static Date getToday() {
        if (testToday != null)
            return testToday;

        return new Date(System.currentTimeMillis());
    }


    public static boolean isToday(Date date) {
        Date today = getToday();
        return today.getYear() == date.getYear() && today.getMonth() == date.getMonth() && today.getDate() == date.getDate();
    }


    public static boolean isBeforeToday(Date date) {
        return date.compareTo(getToday()) < 0;
    }


    public static Date getIncDate(Date lastQueryDate, int numDays) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(lastQueryDate);       // Set it in the Calendar object
        cal.add(Calendar.DATE, numDays);  // add the day-increment according to the given learning scheme

        return cal.getTime();
    }


    public static final String LEARN_DATE = "numLearntDate";
    public static final String NUM_LEARNT_TODAY = "numLearntToday";


    public static int getNumLearntToday() {
        String lastLearnData = Utils.getPrefs().get(LEARN_DATE, DateFormat.getInstance().format(getToday()));
        try {
            Date lastLearnDate = DateFormat.getInstance().parse(lastLearnData);

            if (isToday(lastLearnDate)) {
                return Utils.getPrefs().getInt(NUM_LEARNT_TODAY, 0);
            } else {
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }


    public static void setNumLearntToday(int numLearntToday) {
        Utils.getPrefs().put(LEARN_DATE, DateFormat.getInstance().format(getToday()));
        Utils.getPrefs().putInt(NUM_LEARNT_TODAY, numLearntToday);
    }


    public static int getMaxCardToBeLearntToday() {
        return Math.max(0, getMaxNewCardsPerDay() - getNumLearntToday());
    }


    public static int getMaxNewCardsPerDay() {
        return Utils.getPrefs().getInt(GlobLearnSettings.NUM_ITEMS_PER_DAY, GlobLearnSettings.NUM_ITEMS_PER_DAY_DEFAULT);
    }


    /**
     * Reduce the number of new items to the given amount of <code>maxNumNew</code> and keep all no-new items.
     */
    public static ArrayList<Item> getNewReducedItCo(ArrayList<Item> items, int maxNumNew) {
        ArrayList<Item> redItCo = new LTMCollection();

        int numNewItemsIncluded = 0;
        for (Item item : items) {
            if (((LTMItem) item).isNew()) {
                if (numNewItemsIncluded < maxNumNew) {
                    redItCo.add(item);
                    numNewItemsIncluded++;
                }
            } else
                redItCo.add(item);
        }

        return redItCo;
    }


    /**
     * Extract all new items from a list and ignore already known items.
     */
    public static ArrayList<Item> getNewItems(List<? extends Item> scheduledFileItems) {
        ArrayList<Item> newItems = new ArrayList<Item>();
        for (Item scheduledFileItem : scheduledFileItems) {
            if (((LTMItem) scheduledFileItem).isNew())
                newItems.add(scheduledFileItem);
        }

        return newItems;
    }


    public static List<Item> getScheduledItems(List<Item> items) {
        ArrayList<Item> scheduledItems = new ArrayList<Item>();
        for (Item item : items) {
            if (((LTMItem) item).isScheduledForToday()) {
                scheduledItems.add(item);
            }
        }

        return scheduledItems;
    }


    /**
     * Returns the difference of two dates in days (fristDay - secondDay).
     * <p/>
     * The argument are thereby not assumed to be in any order: the names are just used in order to the ease the
     * understanding)
     */
    public static int getDayDiff(Date firstDate, Date secondDate) {
        long fristDay = (int) (firstDate.getTime() / (86400. * 1000.));
        long secondDay = (int) (secondDate.getTime() / (86400. * 1000.));

        return (int) (fristDay - secondDay);
    }


    /**
     * Computes a measure of urgency given a set of LTM-items. Thereby 'urgency' is defined as the mean of (scheduled
     * day - today) over all scheduled files.
     */
    public static double getUrgency(ArrayList<Item> items) {
        double urgency = 0;
        int numScheduled = 0;

        for (Item item : items) {
            LTMItem ltmItem = (LTMItem) item;
            if (!ltmItem.isScheduledForToday())
                continue;

            Date nextScheduleDate = ltmItem.getNextScheduledDate();
            int dayDiff = getDayDiff(getToday(), nextScheduleDate);

            urgency += dayDiff;
            numScheduled++;
        }

        return (int) (urgency / (double) numScheduled);
    }


    @Test
    public void testDateOperators() {
        Date christmas2K = new Date(100, 11, 23);
        Date christmas3K = new Date(300, 11, 24);

        Assert.assertTrue(isBeforeToday(christmas2K));
        Assert.assertFalse(isToday(christmas2K));

        Assert.assertFalse(isBeforeToday(christmas3K));
    }
}
