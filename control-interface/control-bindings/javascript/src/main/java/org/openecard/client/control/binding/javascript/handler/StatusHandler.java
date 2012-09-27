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

package org.openecard.client.control.binding.javascript.handler;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListeners;
import org.openecard.client.control.module.status.StatusRequest;
import org.openecard.client.control.module.status.StatusResponse;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Implements a StatusHandler to get information about the functionality of the client.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class StatusHandler extends ControlJavaScriptHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusHandler.class);
    private final WSMarshaller m;

    /**
     * Creates a new StatusHandler.
     *
     * @param listeners ControlListeners
     */
    public StatusHandler(ControlListeners listeners) {
	super("status", listeners);
	
	try {
	    m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
    }
    
    @Override
    public ClientRequest handleRequest(Map data) throws ControlException, Exception {
	return new StatusRequest();
    }
    
    @Override
    public Object[] handleResponse(ClientResponse clientResponse) throws ControlException, Exception {
	ArrayList<String> xml = new ArrayList<String>();
	
	if (clientResponse instanceof StatusResponse) {
	    List<ConnectionHandleType> connectionHandles = ((StatusResponse) clientResponse).getConnectionHandles();
	    for (ConnectionHandleType handle : connectionHandles) {
		//FIXME 
		Document contentDoc = m.marshal(new JAXBElement(new QName("iso", "Status"), ConnectionHandleType.class, handle));
		String result = m.doc2str(contentDoc);
		xml.add(result);
	    }
	}
	
	return xml.toArray(new Object[xml.size()]);
    }
    
}
