package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.oimputils.ImpressHelper;
import info.opencards.oimputils.OOoDocumentUtils;

import java.net.MalformedURLException;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ImpressApiEval {

    public static void main(String[] args) throws BootstrapException, com.sun.star.uno.Exception, MalformedURLException, InterruptedException {

        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");

//        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
//        xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);

        ImpressHelper.getSelectedSlideSorterSlides(xContext, xComponent);

//        XPane pane = xCM.getCommandController();

//        XDrawPage editorSlide = ImpressHelper.getCurrentEditorSlide(xComponent);
//        ImpressHelper.getPresentation(xComponent);
//
////         try to start the presentation using the presenter api.
//        System.out.println("presentation started");
//        Thread.sleep(5000);
//
//        ImpressHelper.stopFullScreenMode(xComponent);
    }
}
