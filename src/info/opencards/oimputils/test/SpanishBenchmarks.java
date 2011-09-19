package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import info.opencards.Utils;
import info.opencards.core.FlashCardCollection;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;
import info.opencards.oimputils.StringCompressUtils;

import java.io.*;


/**
 * Evaluates the xml-serialization into ODP and compressing of large serialized FlashcardCollections.
 *
 * @author Holger Brandl
 */
public class SpanishBenchmarks {


    public static void main(String[] args) throws Throwable {

//        cleanUpSpanishODP();
//        evalSimpleCompression();
//        evalXStreamCompressing();

        XComponentContext xContext = Bootstrap.bootstrap();
        File testFile = new File("testdata/Spanish2.odp");

        //now create the string buffer and a temp-testFile to be used as test-envrionment
        StringBuffer metaBuffer = new StringBuffer(new XStream().toXML(loadSpanishFlashcards()));
        String curMetaData = metaBuffer.toString();

        Timer saveTimer = new Timer(), loadTimer = new Timer();

        System.err.println("next length is " + metaBuffer.length() / 1024. + ". writing to odp....");

        // now load the serialized properties
        saveTimer.start();

        boolean doCompress = true;
        if (doCompress)
            OpenOfficeUtils.writeClosedFileDocumentProperty(xContext, "foobar", StringCompressUtils.compress2(curMetaData), testFile);
        else
            OpenOfficeUtils.writeClosedFileDocumentProperty(xContext, "foobar", curMetaData, testFile);

        saveTimer.stop();

        System.err.println("savetime=" + saveTimer.getElapsedTime() / 1000.);

        // now load the serialized properties
        System.err.println("loading compressed card-set from odp.... ");

        loadTimer.start();
        String readProp;
        if (doCompress)
            readProp = StringCompressUtils.uncompress2((String) OpenOfficeUtils.readClosedFileDocumentProperty(xContext, "foobar", testFile));
        else
            readProp = (String) OpenOfficeUtils.readClosedFileDocumentProperty(xContext, "foobar", testFile);

        FlashCardCollection uncompressCards = (FlashCardCollection) new XStream().fromXML(readProp);
        loadTimer.stop();

        System.err.println("loadTime=" + loadTimer.getElapsedTime() / 1000.);

        assert uncompressCards != null;
    }


    private static void evalXStreamCompressing() throws IOException {
        FlashCardCollection cardCollection = (FlashCardCollection) new XStream().fromXML(new FileReader("spanish.xml"));
        System.err.println("finished loading...");

        XStream xstream = new XStream();
//        xstream.processAnnotations(FlashCard.class);
//        xstream.processAnnotations(LeitnerItem.class);
//        xstream.processAnnotations(LTMItem.class);
//        xstream.processAnnotations(FlashCardCollection.class);
//        xstream.registerConverter(new ShortFloatConverter());
//        xstream.setMode(XStream.ID_REFERENCES); // this blows up the zip by 20kb

        StringWriter stringWriter = new StringWriter();
        CompactWriter compactWriter = new CompactWriter(stringWriter);
        xstream.marshal(cardCollection, compactWriter);

        String convertString = stringWriter.toString();

        convertString = StringCompressUtils.compress2(convertString);
        System.err.println("compressed string is: " + convertString);

        FileWriter writer = new FileWriter("reducedspanish.xml");
        writer.append(convertString);
        writer.flush();
        writer.close();

        System.err.println("finished writing");

        loadSpanishFlashcards();
    }


    private static FlashCardCollection loadSpanishFlashcards() {
        try {
            BufferedReader compressReader = new BufferedReader(new FileReader("reducedspanish.xml"));
            String input = compressReader.readLine();
            FlashCardCollection faco = (FlashCardCollection) new XStream().fromXML(StringCompressUtils.uncompress2(input));
            System.err.println("finished reloading");

            return faco;
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert false;
        return null;
    }


    private static void cleanUpSpanishODP() throws BootstrapException {
        XComponentContext xContext = Bootstrap.bootstrap();

        File testFile = new File("testdata/Spanish2.odp");
        XComponent xComp = OOoDocumentUtils.openDocument(xContext, Utils.convert2OOurl(testFile));
        ImpressSerializer.cleanUpFile(xComp);

        OOoDocumentUtils.saveDocument(xComp);

        try {
            OOoDocumentUtils.closeDocument(xComp);
        } catch (CloseVetoException e) {
            e.printStackTrace();
        }
    }
}


