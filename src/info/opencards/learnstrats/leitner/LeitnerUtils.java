package info.opencards.learnstrats.leitner;

import java.util.List;


/**
 * Some static utility methods which ease the usage of our leitner system implementation.
 *
 * @author Holger Brandl
 */
@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
public class LeitnerUtils {


    /**
     * Returns true, if there are unlearnt cards wihtin the boxes. (Unlearnt cards are cards which have not reached the
     * final last box.
     */
    public static boolean hasUnlearntCards(LeitnerSystem leitnerSystem) {
        List<List<LeitnerItem>> boxes = leitnerSystem.getBoxes();
        for (int i = 0; i < boxes.size() - 1; i++) {
            if (!boxes.get(i).isEmpty())
                return true;
        }

        return false;
    }


    public static int count(List<LeitnerItem> flashCards, int learnStatus) {
        int counter = 0;

        for (LeitnerItem flashCard : flashCards) {
            if (flashCard.getCurBox() == learnStatus)
                counter++;
        }

        return counter;
    }


    /**
     * Prints a complete ascii-dump of the given leitner system.
     */
    public static String print(LeitnerSystem leitnerSystem) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < leitnerSystem.numBoxes(); i++) {
            List<LeitnerItem> box = leitnerSystem.getBox(i);

            int numLearnt = LeitnerUtils.count(box, LeitnerSystem.LEARNT);
            int numFailed = LeitnerUtils.count(box, LeitnerSystem.FAILED);
            int numUnknown = LeitnerUtils.count(box, LeitnerSystem.UNKNOWN);

            s.append("box " + i + " : learnt: " + numLearnt + "   Failed: " + numFailed + "   Unknown " + numUnknown + "\n");
        }

        return s.toString();
    }
}
