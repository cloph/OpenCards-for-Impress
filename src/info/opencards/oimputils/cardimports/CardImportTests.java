package info.opencards.oimputils.cardimports;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.oimputils.OOoDocumentUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * A collection of unit-tests that ensure that various formats and encoding can be imported into flashcards
 *
 * @author Holger Brandl
 */
public class CardImportTests {


    @Test
    public void testImportDiffEncodings() throws BootstrapException, IOException {
        XComponentContext xContext = Bootstrap.bootstrap();

        for (File file : new File("testdata").listFiles()) {
            if (!file.getName().startsWith("flashcards"))
                continue;

            System.out.println("processing : " + file.getName());

            Map<String, String> map = ImportManager.readCsvFile(file, "\t");

            // to make the generated impress presentations different add an addtional slide which just contains the encoding name
            Charset encoding = new SmartEncodingInputStream(new FileInputStream(file)).getEncoding();
            map.put("Encoding", encoding.displayName());


            // now create the slides based on the read lookup table
            XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");
            boolean hasInsertedCards = ImportManager.insertCardsIntoPresentation(map, xComponent);

            // assert that some slides became created
            Assert.assertTrue("import for endoding '" + encoding.displayName() + "' failed", hasInsertedCards);
        }
    }


    @Test
    public void importFile() throws BootstrapException {

//        Map<String, String> map = ImportManager.readCsvFile(new File("testdata/sample_unicode.txt"));
//        Map<String, String> map = ImportManager.readCsvFile(new File("testdata/ascitest.txt"));
        Map<String, String> map = ImportManager.readCsvFile(new File("testdata/flashcards_utf16le.txt"), "\t");


        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");

        boolean hasInsertedCards = ImportManager.insertCardsIntoPresentation(map, xComponent);

        Assert.assertTrue(hasInsertedCards);
    }
}
