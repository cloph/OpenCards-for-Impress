package info.opencards.core;

/**
 * An interface-layer which needs to be implemented in order to serve as a slide-set provider for OpenCards
 *
 * @author Holger Brandl
 */
public interface PresenterProxy {

    /**
     * Shows the question side of the current item.
     */
    public boolean showCardFront(Item item);


    /**
     * Shows answer and question of the given item
     */
    public boolean showCompleteCard(Item item);


    /**
     * Starts the learning session for a previously opened card file
     *
     * @param cardItemCollection The items that are about to be evaluated.
     */
    public void startFileSession(ItemCollection cardItemCollection);


    /**
     * Opens the given card file
     */
    void openCardFile(CardFile cardFile);


    /**
     * Stops the current file session.
     */
    public void stopFileSession();


    /**
     * Stops the learning session.
     */
    void stopLearnSession();


    /**
     * Register for proxy disposing events.
     */
    void addProxyEventListener(ProxyEventListener l);

    /**
     * Unregister for proxy disposing events.
     */
    void removeProxyEventListener(ProxyEventListener l);


}
