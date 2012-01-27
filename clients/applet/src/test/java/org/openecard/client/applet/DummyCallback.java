/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.applet;

import java.util.Date;
import javax.xml.transform.TransformerException;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
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
        } catch (MarshallingTypeException ex) {
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
