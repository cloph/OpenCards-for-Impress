package info.opencards.util;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;

import java.io.File;
import java.util.prefs.BackingStoreException;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class FooBar {

    public static void main(String[] args) throws BackingStoreException {

        File file = new File("P:/Spanish.odp");
        XComponentContext xContext = null;
        try {
            xContext = Bootstrap.bootstrap();
        } catch (BootstrapException e) {
            e.printStackTrace();
        }

        OpenOfficeUtils.writeClosedFileDocumentProperty(xContext, CardFile.SERIALIZED_COLLECTION, "", file);

        OpenOfficeUtils.getLocale(xContext);
        assert xContext != null;
        XMultiComponentFactory xMCF = xContext.getServiceManager();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");

        // create an odp with a non-matching user-id
        String fileURL = Utils.convert2OOurl(file);
        OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

        CardFile cardFile1 = new CardFile(file);
        cardFile1.setSerializer(new ImpressSerializer(xContext));

        CardFile cardFile = cardFile1;
        cardFile.getSerializer().writeMetaData(cardFile, CardFile.SERIALIZED_COLLECTION, "");
        cardFile.flush();

    }

}
