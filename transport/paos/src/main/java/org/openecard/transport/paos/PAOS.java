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

package org.openecard.transport.paos;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.openecard.common.ECardConstants;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
import org.openecard.ws.MarshallingTypeException;
import org.openecard.ws.WSMarshaller;
import org.openecard.ws.WSMarshallerException;
import org.openecard.ws.WSMarshallerFactory;
import org.openecard.ws.soap.SOAPException;
import org.openecard.ws.soap.SOAPHeader;
import org.openecard.ws.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PAOS {

    private static final Logger logger = LoggerFactory.getLogger(PAOS.class);

    private final WSMarshaller m;
    private final URL endpoint;
    private final Dispatcher dispatcher;
    private final TlsClient tlsClient;


    public PAOS(URL endpoint, Dispatcher dispatcher) {
	this(endpoint, dispatcher, null);
    }

    public PAOS(URL endpoint, Dispatcher dispatcher, TlsClient tlsClient) {
	this.endpoint = endpoint;
	this.dispatcher = dispatcher;
	this.tlsClient = tlsClient;

	try {
	    m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
    }

    private String getRelatesTo(SOAPMessage msg) throws SOAPException {
	return getHeaderElement(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"));
    }

    private void setRelatesTo(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElement(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"), true);
	elem.setTextContent(value);
    }

    private String getHeaderElement(SOAPMessage msg, QName elem) throws SOAPException {
	Element headerElem = getHeaderElement(msg, elem, false);
	return (headerElem == null) ? null : headerElem.getTextContent().trim();
    }

    private Element getHeaderElement(SOAPMessage msg, QName elem, boolean create) throws SOAPException {
	Element result = null;
	SOAPHeader h = msg.getSOAPHeader();
	// try to find a header
	for (Element e : h.getChildElements()) {
	    if (e.getLocalName().equals(elem.getLocalPart()) && e.getNamespaceURI().equals(elem.getNamespaceURI())) {
		result = e;
		break;
	    }
	}
	// if no such element in header, create new
	if (result == null && create) {
	    result = h.addHeaderElement(elem);
	}
	return result;
    }

    private void addMessageIDs(SOAPMessage msg) throws SOAPException {
	String otherID = MessageGenerator.getRemoteID();
	String newID = MessageGenerator.createNewID(); // also swaps messages in
	// MessageGenerator
	if (otherID != null) {
	    // add relatesTo element
	    setRelatesTo(msg, otherID);
	}
	// add MessageID element
	setMessageID(msg, newID);
    }

    private void updateMessageID(SOAPMessage msg) throws PAOSException {
	try {
	    String id = getMessageID(msg);
	    if (id == null) {
		throw new PAOSException("No MessageID in PAOS header.");
	    }
	    if (!MessageGenerator.setRemoteID(id)) {
		// IDs don't match throw exception
		throw new PAOSException("MessageID from result doesn't match.");
	    }
	} catch (SOAPException e) {
	    logger.error(e.getMessage(), e);
	    throw new PAOSException(e.getMessage(), e);
	}
    }

    private String getMessageID(SOAPMessage msg) throws SOAPException {
	return getHeaderElement(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"));
    }

    private void setMessageID(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElement(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"), true);
	elem.setTextContent(value);
    }

    public Object processPAOSRequest(InputStream content) throws PAOSException {
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    updateMessageID(msg);

	    logger.debug("Message received:\n{}", m.doc2str(doc));

	    return m.unmarshal(msg.getSOAPBody().getChildElements().get(0));
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new PAOSException(e.getMessage(), e);
	}
    }

    public String createPAOSResponse(Object obj) throws MarshallingTypeException, SOAPException, TransformerException {
	SOAPMessage msg = createSOAPMessage(obj);
	String result = m.doc2str(msg.getDocument());

	logger.debug("Message sent:\n{}", result);

	return result;
    }

    public String createStartPAOS(String sessionIdentifier, List<ConnectionHandleType> connectionHandles) throws MarshallingTypeException, SOAPException, TransformerException {
	StartPAOS startPAOS = new StartPAOS();
	startPAOS.setSessionIdentifier(sessionIdentifier);
	startPAOS.setProfile(ECardConstants.Profile.ECARD_1_1);
	startPAOS.getConnectionHandle().addAll(connectionHandles);

	SOAPMessage soapMsg = createSOAPMessage(startPAOS);
	String responseStr = m.doc2str(soapMsg.getDocument());

	return responseStr;
    }

    private SOAPMessage createSOAPMessage(Object content) throws MarshallingTypeException, SOAPException {
	Document contentDoc = m.marshal(content);
	SOAPMessage msg = m.add2soap(contentDoc);
	SOAPHeader header = msg.getSOAPHeader();

	// fill header with paos stuff
	Element paos = header.addHeaderElement(new QName(ECardConstants.PAOS_VERSION_20, "PAOS"));
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "actor", ECardConstants.ACTOR_NEXT);
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "mustUnderstand", "1");

	Element version = header.addChildElement(paos, new QName(ECardConstants.PAOS_VERSION_20, "Version"));
	version.setTextContent(ECardConstants.PAOS_VERSION_20);

	Element endpointReference = header.addChildElement(paos, new QName(ECardConstants.PAOS_VERSION_20, "EndpointReference"));
	Element address = header.addChildElement(endpointReference, new QName(ECardConstants.PAOS_VERSION_20, "Address"));
	address.setTextContent("http://www.projectliberty.org/2006/01/role/paos");
	Element metaData = header.addChildElement(endpointReference, new QName(ECardConstants.PAOS_VERSION_20, "MetaData"));
	Element serviceType = header.addChildElement(metaData, new QName(ECardConstants.PAOS_VERSION_20, "ServiceType"));
	serviceType.setTextContent(ECardConstants.PAOS_NEXT);

	Element replyTo = header.addHeaderElement(new QName(ECardConstants.WS_ADDRESSING, "ReplyTo"));
	address = header.addChildElement(replyTo, new QName(ECardConstants.WS_ADDRESSING, "Address"));
	address.setTextContent("http://www.projectliberty.org/2006/02/role/paos");

	// add message IDs
	addMessageIDs(msg);
	return msg;
    }

    public StartPAOSResponse sendStartPAOS(StartPAOS message) throws Exception {
	Object msg = message;
	String hostname = endpoint.getHost();
	int port = endpoint.getPort();
	if (port == -1) {
	    port = endpoint.getDefaultPort();
	}
	String resource = endpoint.getFile();

	// loop and send makes a computer happy
	while (true) {
	    // set up connection
	    Socket socket = new Socket(hostname, port);
	    StreamHttpClientConnection conn;
	    if (tlsClient != null) {
		// TLS
		TlsProtocolHandler handler = new TlsProtocolHandler(socket.getInputStream(), socket.getOutputStream());
		handler.connect(tlsClient);
		conn = new StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream());
	    } else {
		// no TLS
		conn = new StreamHttpClientConnection(socket.getInputStream(), socket.getOutputStream());
	    }

	    HttpContext ctx = new BasicHttpContext();
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
	    DefaultConnectionReuseStrategy reuse = new DefaultConnectionReuseStrategy();
	    boolean isReusable;
	    // send as long as connection is valid
	    do {
		// prepare request
		BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", resource);
		req.setParams(conn.getParams());
		HttpRequestHelper.setDefaultHeader(req, hostname);
		String reqMsgStr = createPAOSResponse(msg);
		req.setHeader(ECardConstants.HEADER_KEY_PAOS, ECardConstants.HEADER_VALUE_PAOS);
		req.setHeader("Accept", "application/vnd.paos+xml");
		ContentType reqContentType = ContentType.create("application/vnd.paos+xml", "UTF-8");
		StringEntity reqMsg = new StringEntity(reqMsgStr, reqContentType);
		req.setEntity(reqMsg);
		req.setHeader(reqMsg.getContentType());
		req.setHeader("Content-Length", Long.toString(reqMsg.getContentLength()));
		// send request and receive response
		HttpResponse response = httpexecutor.execute(req, conn, ctx);
		conn.receiveResponseEntity(response);
		HttpEntity entity = response.getEntity();
		// consume entity
		Object requestObj = processPAOSRequest(entity.getContent());

		// break when message is startpaosresponse
		if (requestObj instanceof StartPAOSResponse) {
		    StartPAOSResponse startPAOSResponse = (StartPAOSResponse) requestObj;
		    return startPAOSResponse;
		}

		// send via dispatcher
		msg = dispatcher.deliver(requestObj);

		// check if connection can be used one more time
		isReusable = reuse.keepAlive(response, ctx);
	    } while (isReusable);
	}
    }

}
