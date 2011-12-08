package org.openecard.client.ws;

import org.openecard.client.common.GenericFactory;
import org.openecard.client.common.GenericFactoryException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSMarshallerFactory {

    private GenericFactory<WSMarshaller> factory;

    private WSMarshallerFactory() throws WSMarshallerException {
        try {
            factory = new GenericFactory<WSMarshaller>(WsdefProperties.properties(), "org.openecard.client.ws.marshaller.impl");
        } catch (GenericFactoryException ex) {
            throw new WSMarshallerException(ex);
        }
    }


    private static WSMarshallerFactory inst = null;

    public static WSMarshaller createInstance() throws WSMarshallerException {
        if (inst == null) {
            inst = new WSMarshallerFactory();
        }

        try {
            return inst.factory.getInstance();
        } catch (GenericFactoryException ex) {
            throw new WSMarshallerException(ex);
        }
    }

}
