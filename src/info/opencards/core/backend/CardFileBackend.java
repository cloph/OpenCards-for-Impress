package info.opencards.core.backend;

import info.opencards.core.CardFileSerializer;
import info.opencards.core.PresenterProxy;

import java.awt.*;


/**
 * A kind of hack which publishes the current OpenCards-components to interesed modules.
 * <p/>
 * In order to make the backend-API as lean as possible CardFileBackend only returns interfaces. Its idea is to provide
 * an abstract layer which provides the concrete presentation, serialization and learning implemenations to the core
 * infrastructure. This allows to use OC in a variety of contexts (e.g. OpenOffice Impress)
 *
 * @author Holger Brandl
 */
public class CardFileBackend {

    private Frame owner;

    private PresenterProxy presProxy;
    private CardFileSerializer cfSerializer;


    CardFileBackend(Frame owner, PresenterProxy presenter, CardFileSerializer serializer) {
        this.owner = owner;

        this.presProxy = presenter;
        this.cfSerializer = serializer;

    }


    public PresenterProxy getPresProxy() {
        return presProxy;
    }


    public CardFileSerializer getSerializer() {
        return cfSerializer;
    }


    public Frame getOwner() {
        return owner;
    }
}
