package info.opencards.core;


import com.thoughtworks.xstream.XStream;
import info.opencards.core.categories.Category;
import info.opencards.oimputils.StringCompressUtils;
import info.opencards.ui.AbstractLearnDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A wrapper for an existing file which contains some kind of flashcards (e.g. an Impress-presentation). Different
 * internal representations of these cards may be contained in the file to which an instance of this clas links to. This
 * is necessary in order to use differnt learning-approaches (LTM/STM-learning) in parallel.
 * <p/>
 * The implementation itself is not bounded to a specific card-file-format. Instead the necessary API to extract the
 * flashcard information from the file is encapsulated in an <code>CardFileSerializer</code> which need to be specified
 * using {@link CardFile#setSerializer(info.opencards.core.CardFileSerializer)} during runtime.
 *
 * @author Holger Brandl
 */
public class CardFile {

    private List<Category> myCats = new ArrayList<Category>();
    private File fileLocation;


    private transient FlashCardCollection fileItems;
    private transient CardFileSerializer serializer;

    public static String SERIALIZED_COLLECTION = "flashcardCollection";


    public CardFile(File cardFileLocation) {
        assert cardFileLocation != null;

        this.fileLocation = cardFileLocation;
    }


    /**
     * Returns a reference to list of  all categories this card-file is registered to.
     */
    public Collection<Category> belongsTo() {
        return new ArrayList<Category>(myCats);
    }


    public boolean addToCategory(Category c) {
//        if (!this.belongsTo().contains(c))
        return myCats.add(c);

//        return false;
    }


    public boolean removeFromCategory(Category c) {
        assert myCats.contains(c);
        myCats.remove(c);

        return true;
    }


    public File getFileLocation() {
        return fileLocation;
    }


    public String toString() {
        String catString = myCats.toString();
        return (fileLocation != null ? fileLocation.toString() : "inmemory card-set") + " (" + catString + ")";
    }


    public boolean equals(Object obj) {
        if (!(obj instanceof CardFile))
            return false;

        CardFile file = (CardFile) obj;
        return file.getFileLocation().equals(getFileLocation());
    }


    public FlashCardCollection getFlashCards() {
        if (fileItems == null) {
            // lets try to get the items of this file
            assert serializer != null : "serializer not set";
            String serialItCo = (String) serializer.readMetaData(this, SERIALIZED_COLLECTION);

            if (serialItCo == null || serialItCo.trim().length() == 0) {
                // because the file has no initalized learning model yet, we load its flashcards and save them into the meta-field
                fileItems = serializer.getFlashCards(this);
                this.flush();
            } else {

                try {
                    // to keep oc compatible to versions without older version check if compression is already in use
                    if (!serialItCo.startsWith("<info")) {
                        serialItCo = StringCompressUtils.uncompress2(serialItCo);
                    }

                    fileItems = (FlashCardCollection) new XStream().fromXML(serialItCo);

                } catch (Throwable t) {
                    // if we shouldn't be able to deserialize the learning state we reload the file completely
                    fileItems = serializer.getFlashCards(this);
                    this.flush();
                }
            }
        }

        return fileItems;
    }


    public boolean isDeserialized() {
        return !(fileItems == null);
    }


    public void setSerializer(CardFileSerializer serializer) {
        if (this.serializer != null && this.serializer.equals(serializer))
            return;

        this.serializer = serializer;
        fileItems = null;
    }


    public CardFileSerializer getSerializer() {
        return serializer;
    }


    public void flush() {
        if (fileItems == null)
            throw new RuntimeException("can not serialize null fileItems");

        // serialize to xml and then gzip the result
        String serialItCo = StringCompressUtils.compress2(new XStream().toXML(fileItems));
        serializer.writeMetaData(this, SERIALIZED_COLLECTION, serialItCo);
    }


    /**
     * Given a serialzer and a this methods merges changes of the deep serialzer into an existing reprentation.
     * <p/>
     * Note: We can not save the item-states as slide-properties because this would make a quick parsing of large sets
     * of files impossible.
     *
     * @return <code>true</code> if a synchronization was necessary. This means that some cards were either added or
     *         removed since the last use of OpenCards.
     */
    public boolean synchronize() {
        if (serializer == null) {
            throw new RuntimeException("no serializer set for '" + this + "'");
        }

        boolean neededSync = false;

        FlashCardCollection syncCards = serializer.getFlashCards(this);

        fileItems = getFlashCards();

        // now lets do some cool synchronization!!

        // now do a three way synchronization
        // 1) add missing (new, recently) added cards
        for (FlashCard flashCard : syncCards) {
            if (!fileItems.contains(flashCard)) {
                fileItems.add(flashCard);
                neededSync = true;
            }
        }

        // 2) remove no longer present cards
        for (int i = 0; i < fileItems.size(); i++) {
            FlashCard flashCard = fileItems.get(i);
            if (!syncCards.contains(flashCard)) {
                fileItems.remove(flashCard);

                // tag the card as deleted
                flashCard.setCardIndex(AbstractLearnDialog.INVALID_ITEM);
                neededSync = true;
                i--;
            }
        }

        assert syncCards.size() == fileItems.size() : "size difference after synchronization";

        // 3) fix slide positions and titles of items if necessary
        for (FlashCard card : fileItems) {
            FlashCard flashcard = syncCards.get(syncCards.indexOf(card));

            if (flashcard.getCardIndex() != card.getCardIndex())
                card.setCardIndex(flashcard.getCardIndex());

            if (!flashcard.getCardTitle().equals(card.getCardTitle()))
                card.setCardTitle(flashcard.getCardTitle());
        }

        return neededSync;
    }
}
