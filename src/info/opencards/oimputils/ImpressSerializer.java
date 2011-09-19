package info.opencards.oimputils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.CardFileSerializer;
import info.opencards.core.FlashCardCollection;

import java.util.logging.Level;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ImpressSerializer implements CardFileSerializer {

    public static final String DEFAULT_FLASHCARD_TITLE = "OpenCards_flashcard";

    private XComponentContext xCompContext;


    public ImpressSerializer(XComponentContext xCompContext) {
        this.xCompContext = xCompContext;
    }


    public void setXCompContext(XComponentContext xCompContext) {
        this.xCompContext = xCompContext;
    }


    public FlashCardCollection getFlashCards(CardFile cardFile) {

        XComponent openXDocument = OOoDocumentUtils.getOpenXDocument(cardFile, xCompContext);
        if (openXDocument != null) {
            Utils.log("extracting items from open document: " + cardFile.toString());
            return ImpressHelper.getPresenterCards(openXDocument, ImpressHelper.MAX_GET_SLIDES_RETRY);

        } else {
            Utils.log("extracting items from closed document: " + cardFile.toString());
            return extractClosedFileItemCollection(xCompContext, cardFile);
        }
    }


    public boolean writeMetaData(CardFile cardFile, String propName, Object propValue) {
        Utils.log("writing meta-data (key=" + propName + "to '" + cardFile.toString() + "'");

        //check wether the file is open in OO and serialize to in-memory-file if possible
        XComponent openXDocument = OOoDocumentUtils.getOpenXDocument(cardFile, xCompContext);
        if (openXDocument != null) {
//        if (cardFile == null || cardFile.getFile() == null) {
//            XComponent xComponent = ImpressHelper.getXComponent(xCompContext);
            OpenOfficeUtils.writeDocumentProperty(openXDocument, propName, propValue);
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            OOoDocumentUtils.saveDocument(openXDocument);
            Utils.log("document saved!");
        } else if (cardFile.getFileLocation() != null) {
            //use the quick-access api to write the property
            OpenOfficeUtils.writeClosedFileDocumentProperty(xCompContext, propName, propValue, cardFile.getFileLocation());
        } else {
            Utils.log(Level.WARNING, "Writing of meta data was not possible due to missing access to document");
        }

        return false;
    }


    public Object readMetaData(CardFile cardFile, String propName) {

        XComponent openXDocument = OOoDocumentUtils.getOpenXDocument(cardFile, xCompContext);
        if (openXDocument != null || cardFile.getFileLocation() == null) {
            return OpenOfficeUtils.readDocumentProperty(openXDocument, propName);
        } else {
            //use the quick-access api to read the property
            return OpenOfficeUtils.readClosedFileDocumentProperty(xCompContext, propName, cardFile.getFileLocation());
        }
    }


    public boolean equals(Object obj) {
        if (obj instanceof ImpressSerializer) {
            return ((ImpressSerializer) obj).xCompContext.equals(xCompContext);
        }

        return false;
    }


    public static FlashCardCollection extractClosedFileItemCollection(XComponentContext xContext, CardFile cardFile) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();
            Object desktop = serviceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
            XComponentLoader xComponentLoader = ImpressHelper.cast(XComponentLoader.class, desktop);

            String fileURL = Utils.convert2OOurl(cardFile.getFileLocation());
            PropertyValue[] myFileProps = new PropertyValue[]{new PropertyValue()};
            myFileProps[0].Name = "Hidden";
            myFileProps[0].Value = Boolean.TRUE;

//            XComponent xDrawComponent = xComponentLoader.loadComponentFromURL("private:factory/simpress", "_hidden", 0, new PropertyValue[0]);
            XComponent xDrawComponent = xComponentLoader.loadComponentFromURL(fileURL, "_blank", 0, myFileProps);

            FlashCardCollection flashcards = ImpressHelper.getPresenterCards(xDrawComponent, ImpressHelper.MAX_GET_SLIDES_RETRY);

            // in order to make the new slide-ids persistent we save the file now
            OOoDocumentUtils.saveDocument(xDrawComponent);
            OOoDocumentUtils.closeDocument(xDrawComponent);

            Utils.log("closed document item extraction finished: " + cardFile.toString());

            return flashcards;
        } catch (Exception e) {
            Utils.log(Level.WARNING, e.toString());
            throw new RuntimeException(e);
        }
    }


    public static void cleanUpFile(XComponent xDrawComponent) {
        try {
            Object documentSupplier = ImpressHelper.cast(XDrawPagesSupplier.class, xDrawComponent);

            if (documentSupplier instanceof XDrawPagesSupplier) {
                XDrawPagesSupplier xDrawPagesSupplier = (XDrawPagesSupplier) documentSupplier;
                XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

                ImpressHelper.removeTempSlides(xDrawComponent);

                // reset slide names
                for (int i = 0; i < xDrawPages.getCount(); i++) {
                    XDrawPage xDrawPage = ImpressHelper.cast(XDrawPage.class, xDrawPages.getByIndex(i));
                    ImpressHelper.cast(XNamed.class, xDrawPage).setName("");
                }
            }

            // remove all custom oc-property nodes
            OpenOfficeUtils.removeAllOCProperties(xDrawComponent);
        } catch (Exception e) {
            Utils.log(Level.WARNING, e.toString());
            throw new RuntimeException(e);
        }
    }
}
