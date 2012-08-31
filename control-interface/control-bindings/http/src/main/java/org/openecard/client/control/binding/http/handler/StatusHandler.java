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

package org.openecard.client.control.binding.http.handler;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.net.URI;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.binding.http.HTTPException;
import org.openecard.client.control.binding.http.common.Http11Response;
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
public class StatusHandler extends ControlClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(StatusHandler.class);
    private final WSMarshaller m;

    /**
     * Creates a new StatusHandler.
     *
     * @param listeners ConnectorListeners
     */
    public StatusHandler(ControlListeners listeners) {
	super("/status", listeners);

	try {
	    m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    logger.error("Exception", e);
	    throw new RuntimeException(e);
	}
    }

    @Override
    public ClientRequest handleRequest(HttpRequest httpRequest) throws ControlException, Exception {
	try {
	    RequestLine requestLine = httpRequest.getRequestLine();

	    if (requestLine.getMethod().equals("GET")) {
		URI requestURI = URI.create(requestLine.getUri());

		if (requestURI.getQuery() != null && !requestURI.getQuery().isEmpty()) {
		    throw new HTTPException(HttpStatus.SC_BAD_REQUEST);
		}

		return new StatusRequest();
	    } else {
		throw new HTTPException(HttpStatus.SC_METHOD_NOT_ALLOWED);
	    }
	} catch (HTTPException e) {
	    throw e;
	} catch (Exception e) {
	    throw new HTTPException(HttpStatus.SC_BAD_REQUEST, e.getMessage());
	}
    }

    @Override
    public HttpResponse handleResponse(ClientResponse clientResponse) throws ControlException, Exception {
	HttpResponse httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);

	if (clientResponse instanceof StatusResponse) {
	    List<ConnectionHandleType> connectionHandles = ((StatusResponse) clientResponse).getConnectionHandles();
	    StringBuilder xml = new StringBuilder();
	    for (ConnectionHandleType handle : connectionHandles) {
		//FIXME 
		Document contentDoc = m.marshal(new JAXBElement(new QName("iso", "Status"), ConnectionHandleType.class, handle));
		String result = m.doc2str(contentDoc);
		xml.append(result);
	    }

	    httpResponse.setStatusCode(HttpStatus.SC_ACCEPTED);
	    ContentType contentType = ContentType.create(ContentType.TEXT_XML.getMimeType(), "UTF-8");
	    StringEntity entity = new StringEntity(xml.toString(), contentType);
	    httpResponse.setEntity(entity);
	}

	return httpResponse;
    }

}