package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.oimputils.DrawPageHelper;
import info.opencards.oimputils.ImpressHelper;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.OOoDocumentUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class DrawPageHelperTest {

    @Test
    public void testPersistentSlideProperties() throws BootstrapException, CloseVetoException {
        XComponentContext xContext = Bootstrap.bootstrap();

        File tempOdpFile = new File("testdata/testpres.odp");
        CardFile cardFile = new CardFile(tempOdpFile);
        cardFile.setSerializer(new ImpressSerializer(xContext));

        if (OOoDocumentUtils.getOpenXDocument(cardFile, xContext) == null)
            OOoDocumentUtils.openDocument(xContext, Utils.convert2OOurl(tempOdpFile));

        XComponent document = OOoDocumentUtils.getOpenXDocument(cardFile, xContext);
        XDrawPage editorSlide = ImpressHelper.getCurrentEditorSlide(document);
        Integer idBefore = DrawPageHelper.readDrawPageCustomProperty(editorSlide);

        Integer dummyID = Utils.getRandGen().nextInt();
        DrawPageHelper.writeDrawPageCustomProperty(editorSlide, dummyID);

        // save'n'close the document
        OOoDocumentUtils.saveDocument(document);
        OOoDocumentUtils.closeDocument(document);

        // reopen the document and test whether the custom property was made persistant
        OOoDocumentUtils.openDocument(xContext, Utils.convert2OOurl(new File("testdata/testpres.odp")));
        document = OOoDocumentUtils.getOpenXDocument(new CardFile(new File("testdata/testpres.odp")), xContext);
        editorSlide = ImpressHelper.getCurrentEditorSlide(document);
        Integer idAfter = DrawPageHelper.readDrawPageCustomProperty(editorSlide);

        System.out.println("before=" + idBefore + " dummy=" + dummyID + " after=" + idAfter);
        Assert.assertEquals(dummyID, idAfter);
    }
}
