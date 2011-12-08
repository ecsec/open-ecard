package org.openecard.client.applet;

import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.ws.WSMarshaller;
import java.util.Date;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;

/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class DummyCallback implements EventCallback {

    static {
        try {
            m = WSMarshallerFactory.createInstance();
        } catch (WSMarshallerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final WSMarshaller m;
    
    @Override
    public void signalEvent(EventType eventType, Object eventData) {
        String data = null;
        try {
            data = m.doc2str(m.marshal(eventData));
        } catch (JAXBException ex) {
            System.err.println(ex.getMessage());
        } catch (TransformerException ex) {
            System.err.println(ex.getMessage());
        }
        
        Date d = new Date(System.currentTimeMillis());
        System.out.println("Date: " + d);
        System.out.println("Event: " + eventType.name());
        System.out.println("Data: " + data);
        System.out.println("");
    }
    
}
