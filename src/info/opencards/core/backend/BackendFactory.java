package info.opencards.core.backend;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.MockSupplier;
import info.opencards.oimputils.ImpressProxy;
import info.opencards.oimputils.ImpressSerializer;
import info.opencards.oimputils.TagImpressProxy;

import javax.swing.*;
import java.awt.*;


/**
 * A factory which is able to create various types of OpenCards-backends. All backends are stored internally and given a
 * the creation key the backend will be created only if it isn't already created.
 *
 * @author Holger Brandl
 */
public class BackendFactory {

    /**
     * Creates an OpenOffice Impress presentation and serialization backend.
     */
    // todo we should extract the owner somehow from the context (we could also use XFrame as arguement if this should help)
    // see also
    public static synchronized CardFileBackend getImpressBackend(Frame owner, XComponent xComponent, XComponentContext xCompContext) {
//        ImpressProxy impressProxy = new ImpressProxy(xComponent, xCompContext);
        ImpressProxy impressProxy = new TagImpressProxy(xComponent, xCompContext);
//        ImpressProxy impressProxy = new PresenterModeProxy(xComponent, xCompContext);
        ImpressSerializer impressSerializer = new ImpressSerializer(xCompContext);

        return new CardFileBackend(owner, impressProxy, impressSerializer);
    }


    /**
     * Creates a Mock-backend which is mainly used for testing purposes.
     */
    public static CardFileBackend getMockBackend(JFrame owner, MockSupplier mockSupplier) {
        return new CardFileBackend(owner, mockSupplier, mockSupplier);
    }
}
