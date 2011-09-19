package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.oimputils.ImpressHelper;
import info.opencards.oimputils.OOoDocumentUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class TestOOoStability {

    public static void main(String[] args) throws BootstrapException, InterruptedException {

        XComponentContext xContext = Bootstrap.bootstrap();
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, "private:factory/simpress");

        Collection<File> odpFiles = Arrays.asList(new File("D:/gepol/cards").listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".odp");
            }
        }));

        //open files fastly and try to hide the side bar to see if OO crashes or not
        for (File odpFile : odpFiles) {
            xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, Utils.convert2OOurl(odpFile));
            System.err.println("loaded file " + odpFile);

            // now hide the sidepane
            ImpressHelper.showSlidePane(false, xContext, xComponent);

            Thread.sleep(1000);
        }
    }

}
