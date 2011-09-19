package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPageDuplicator;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XText;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.FlashCard;
import info.opencards.core.FlashCardCollection;
import info.opencards.oimputils.DrawPageHelper;
import info.opencards.oimputils.ImpressHelper;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.OOoDocumentUtils;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;


/**
 * Some tests which test the synchronization of <code>FlashcardCollection</code>s after card-file modifications.
 *
 * @author Holger Brandl
 */
public class FlashCollSyncTest {

    private CardFile cardFile;
    private XComponent document;


    @Before
    public void loadTestFile() throws BootstrapException {
        XComponentContext xContext = Bootstrap.bootstrap();

        File tempFile = Utils.createTempCopy(new File("testdata/testpres.odp"));
        cardFile = new CardFile(tempFile);
        cardFile.setSerializer(new ImpressSerializer(xContext));
        if (OOoDocumentUtils.getOpenXDocument(cardFile, xContext) == null)
            OOoDocumentUtils.openDocument(xContext, Utils.convert2OOurl(tempFile));

        document = OOoDocumentUtils.getOpenXDocument(cardFile, xContext);
    }


    @After
    public void clearAfterTest() throws CloseVetoException {
        OOoDocumentUtils.closeDocument(document);
    }


    @Test
    public void testSyncFlashCollectAfterSlideInsert() throws Exception {
        FlashCardCollection flashCardsBefore = (FlashCardCollection) cardFile.getFlashCards().clone();
        FlashCard cardSix = flashCardsBefore.getByIndex(6);
        Assert.assertTrue(cardSix.getCardIndex() == 6);

        final String insertTitle = "Inserted Slide";
        final int insertIndex = 3;

        // insert a slide and give it a name
        XDrawPage insertPage = ImpressHelper.insertNewDrawPageByIndex(document, insertIndex);
        DrawPageHelper.getTitleShape(insertPage);
        XText xText = cast(XText.class, DrawPageHelper.createTitleShape(insertPage, document));
        xText.setString(insertTitle);

        // now request a sync and compare the flashcard-sets afterwards
        cardFile.synchronize();
        FlashCardCollection cards = cardFile.getFlashCards();

        Assert.assertEquals(cards.size(), flashCardsBefore.size() + 1);

        // get the added flashcard and check its title
        ArrayList<FlashCard> diffCards = new ArrayList<FlashCard>(cards);
        diffCards.removeAll(flashCardsBefore);

        Assert.assertTrue(diffCards.size() == 1);
        Assert.assertTrue(diffCards.iterator().next().getCardTitle().equals(insertTitle));
        Assert.assertTrue(diffCards.iterator().next().getCardIndex() == insertIndex + 1); // +1 because the slide is inserter after insertIndex

        // test wether the indices after the inserted page were incremented
        Assert.assertTrue(cards.getByIndex(7).equals(cardSix));
    }


    @Test
    public void testSyncFlashCollectAfterSlideRemove() {
        FlashCardCollection flashCardsBefore = (FlashCardCollection) cardFile.getFlashCards().clone();
        FlashCard cardSix = flashCardsBefore.getByIndex(6);
        Assert.assertTrue(cardSix.getCardIndex() == 6);

        final String insertTitle = "Inserted Slide";
        final int remIndex = 3;

        // insert a slide and give it a name
        XDrawPage remPage = ImpressHelper.getDrawPageByIndex(document, remIndex);
        ImpressHelper.removeDrawPage(document, remPage);

        // now request a sync and compare the flashcard-sets afterwards
        cardFile.synchronize();
        FlashCardCollection cards = cardFile.getFlashCards();

        Assert.assertEquals(cards.size(), flashCardsBefore.size() - 1);

        // get the added flashcard and check its title
        ArrayList<FlashCard> diffCards = new ArrayList<FlashCard>(flashCardsBefore);
        diffCards.removeAll(cards);

        Assert.assertTrue(diffCards.size() == 1);
        Assert.assertTrue(diffCards.iterator().next().getCardIndex() == remIndex);

        // test wether the indices after the inserted page were incremented
        Assert.assertTrue(cards.getByIndex(5).equals(cardSix));
    }


    @Test
    public void testSyncFlashCollectAfterSlideRename() {
        FlashCardCollection flashCardsBefore = (FlashCardCollection) cardFile.getFlashCards().clone();
        FlashCard cardSix = flashCardsBefore.getByIndex(6);
        Assert.assertTrue(cardSix.getCardIndex() == 6);

        final String newSlideTitle = "This is a new slide title";
        final int renameIndex = 3;

        // insert a slide and give it a name
        XDrawPage drawPage = ImpressHelper.getDrawPageByIndex(document, renameIndex);
        DrawPageHelper.getTitleShape(drawPage);
        XText xText = cast(XText.class, DrawPageHelper.getTitleShape(drawPage));
        xText.setString(newSlideTitle);

        // now request a sync and compare the flashcard-sets afterwards
        cardFile.synchronize();
        FlashCardCollection cards = cardFile.getFlashCards();

        Assert.assertEquals(cards.size(), flashCardsBefore.size());

        // get the added flashcard and check its title
        ArrayList<FlashCard> diffCards = new ArrayList<FlashCard>(cards);
        diffCards.removeAll(flashCardsBefore);

        Assert.assertTrue(diffCards.size() == 0);
        Assert.assertTrue(cards.getByIndex(renameIndex).getCardTitle().equals(newSlideTitle));
    }


    @Test
    public void testSyncFlashCollectAfterSlideCopy() {
        FlashCardCollection flashCardsBefore = (FlashCardCollection) cardFile.getFlashCards().clone();
        FlashCard cardSix = flashCardsBefore.getByIndex(6);
        Assert.assertTrue(cardSix.getCardIndex() == 6);

        final String newSlideTitle = "This is a new slide title";
        final int copyIndex = 3;

        // insert a slide and give it a name
        FlashCard copyCard = flashCardsBefore.getByIndex(copyIndex);
        XDrawPage drawPage = ImpressHelper.getDrawPageByIndex(document, copyIndex);
        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, document);
        XDrawPageDuplicator xDrawPageDuplicator = cast(XDrawPageDuplicator.class, xDrawPagesSupplier);
        XDrawPage copySlide = xDrawPageDuplicator.duplicate(drawPage);

        // now request a sync and compare the flashcard-sets afterwards
        cardFile.synchronize();
        FlashCardCollection cards = cardFile.getFlashCards();

        Assert.assertEquals(cards.size(), flashCardsBefore.size() + 1);

        // get the added flashcard and check its title
        ArrayList<FlashCard> diffCards = new ArrayList<FlashCard>(cards);
        diffCards.removeAll(flashCardsBefore);

        Assert.assertTrue(diffCards.size() == 1);
        Assert.assertTrue(cards.getByIndex(copyIndex).getCardTitle().equals(copyCard.getCardTitle()));
        Assert.assertTrue(!copyCard.equals(diffCards.iterator().next()));
    }


    @Test
    public void testSyncFlashCollectAfterSlideMove() {

    }
}
