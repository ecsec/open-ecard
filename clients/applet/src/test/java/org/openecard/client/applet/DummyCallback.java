/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

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
