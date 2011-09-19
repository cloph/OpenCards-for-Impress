package info.opencards.oimputils.test;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.oimputils.OpenOfficeUtils;
import info.opencards.oimputils.SlidePaneCxtMenuInterceptor;

import javax.swing.*;
import java.io.File;


/*
*  Provides example code how to enable/disable
*  commands.
*/
public class CxtMenuInterceptorTest {


    /*
    *  @param args the command line arguments
    */
    public static void main(String[] args) throws BootstrapException {

        // connect
        XComponentContext xLocalContext = Bootstrap.bootstrap();
        OpenOfficeUtils.getLocale(xLocalContext);
        XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
        XComponent xComponent = OOoDocumentUtils.openDocument(xLocalContext, "private:factory/simpress");
        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/testpres.odp")));
        xComponent = OOoDocumentUtils.openDocumentInSameFrame(xComponent, fileURL);


        XModel x = cast(XModel.class, xComponent);
        XController xco = x.getCurrentController();
        XContextMenuInterception cxtInterceptor = cast(XContextMenuInterception.class, xco);

        cxtInterceptor.registerContextMenuInterceptor(new XContextMenuInterceptor() {
            public ContextMenuInterceptorAction notifyContextMenuExecute(ContextMenuExecuteEvent event) {
                System.out.println("new cxtEvent");
                return null;
            }
        });


        cxtInterceptor.registerContextMenuInterceptor(new SlidePaneCxtMenuInterceptor());

        new JFrame().setVisible(true);
    }
}
