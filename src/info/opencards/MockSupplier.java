package info.opencards;

import info.opencards.core.*;

import java.util.Arrays;


/**
 * A test implementation of the <code>PresenterProxy</code>-interface, which is intended to allow development, testing
 * and debugging of new oc functionality without running the complete uno-deploy-loop each time.
 *
 * @author Holger Brandl
 */
public class MockSupplier implements PresenterProxy, CardFileSerializer {

    private static FlashCardCollection mockItems = new FlashCardCollection();


    static {
        mockItems.addAll(Arrays.asList(createItem("rot"), createItem("gruen"), createItem("blau"),
                createItem("weiss"), createItem("schwarz"), createItem("gelb"), createItem("gelb")));
    }


    private Item curItem = mockItems.getItems(Item.class).get(0);


    public boolean showCardFront(Item selectedCard) {
        assert selectedCard != null;

        curItem = selectedCard;
        System.out.println("showing card front of : " + selectedCard);
        return true;
    }


    public boolean showCompleteCard(Item selectedCard) {
        assert selectedCard != null;
        curItem = selectedCard;

        System.out.println("showing complete card  : " + selectedCard);
        return true;
    }


    /**
     * Used for testing purposes.
     */

    public Item getCurItem() {
        return curItem;
    }


    public FlashCardCollection getFlashCards(CardFile cardFile) {
        throw new RuntimeException("not implemented yet");
    }


    public boolean writeMetaData(CardFile cardFile, String propName, Object propValue) {
        System.out.println("write meta-data");

        return true;
    }


    public Object readMetaData(CardFile cardFile, String propName) {
        System.out.println("read meta-data");
        return "";
    }


    public void startFileSession(ItemCollection cardItemCollection) {
        System.out.println("starting learn session....");
    }


    public void stopFileSession() {
        System.out.println("learning stopped");
    }


    public void openCardFile(CardFile cardFile) {
    }


    public void stopLearnSession() {
    }


    public void addProxyEventListener(ProxyEventListener l) {

    }


    public void removeProxyEventListener(ProxyEventListener l) {

    }


    static int counter = 0;


    public static FlashCard createItem(String title) {
        FlashCard card = new FlashCard(counter, title, counter++);
        new Item(card);

        return card;
    }
}
