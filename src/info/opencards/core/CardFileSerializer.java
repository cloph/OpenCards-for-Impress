package info.opencards.core;

/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public interface CardFileSerializer {

    /**
     * Returns the flashcards of the current cardFile. If <code>cardFile</code> is <code>null</code> the implementaiton
     * might try to extract items from another source (e.g. the current document if OpenOffice is used as backend)
     */
    public FlashCardCollection getFlashCards(CardFile cardFile);


    /**
     * Writes a meta-data snippet to the given card-file. If cardFile is <code>null</code> the implementation might try
     * to write to another arbitrary target (e.g. the current document if OpenOffice is used as backend).
     */
    public boolean writeMetaData(CardFile cardFile, String propName, Object propValue);


    /**
     * Reads a meta-data snippet from the given card-file. If cardFile is <code>null</code> the implementation might try
     * to read from another arbitrary source (e.g. the current document if OpenOffice is used as backend).
     */
    public Object readMetaData(CardFile cardFile, String propName);

}
