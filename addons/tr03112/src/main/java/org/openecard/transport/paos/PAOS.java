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

import iso.std.iso_iec._24727.tech.schema.ResponseType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.impl.DefaultConnectionReuseStrategy;
import org.openecard.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.control.module.tctoken.TR03112Keys;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.openecard.common.util.FileUtils;
import org.openecard.control.module.tctoken.TlsConnectionHandler;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.HttpUtils;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.soap.SOAPException;
import org.openecard.ws.soap.SOAPHeader;
import org.openecard.ws.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * PAOS implementation for JAXB types.
 * This implementation can be configured to speak TLS by creating the instance with a TlsClient. The dispatcher instance
 * is used to deliver the messages to the instances implementing the webservice interfaces.
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PAOS {

    private static final Logger logger = LoggerFactory.getLogger(PAOS.class);

    public static final String HEADER_KEY_PAOS = "PAOS";
    // TODO: add service and actions as stated in https://dev.openecard.org/issues/191
    public static final String HEADER_VALUE_PAOS = "ver=\"" + ECardConstants.PAOS_VERSION_20 + "\"";

    public static final QName RELATES_TO = new QName(ECardConstants.WS_ADDRESSING, "RelatesTo");
    public static final QName REPLY_TO = new QName(ECardConstants.WS_ADDRESSING, "ReplyTo");
    public static final QName MESSAGE_ID = new QName(ECardConstants.WS_ADDRESSING, "MessageID");
    public static final QName ADDRESS = new QName(ECardConstants.WS_ADDRESSING, "Address");

    public static final QName PAOS_PAOS = new QName(ECardConstants.PAOS_VERSION_20, "PAOS");
    public static final QName PAOS_VERSION = new QName(ECardConstants.PAOS_VERSION_20, "Version");
    public static final QName PAOS_ENDPOINTREF = new QName(ECardConstants.PAOS_VERSION_20, "EndpointReference");
    public static final QName PAOS_ADDRESS = new QName(ECardConstants.PAOS_VERSION_20, "Address");
    public static final QName PAOS_METADATA = new QName(ECardConstants.PAOS_VERSION_20, "MetaData");
    public static final QName PAOS_SERVICETYPE = new QName(ECardConstants.PAOS_VERSION_20, "ServiceType");

    private final MessageIdGenerator idGenerator;
    private final WSMarshaller m;
    private final Dispatcher dispatcher;
    private final TlsConnectionHandler tlsHandler;
    

    /**
     * Creates a PAOS instance and configures it for a given endpoint.
     * If tlsClient is not null the connection must be HTTPs, else HTTP.
     *
     * @param dispatcher The dispatcher instance capable of dispatching the received messages.
     * @param tlsHandler The TlsClient containing the configuration of the yet to be established TLS channel, or
     *   {@code null} if TLS should not be used.
     * @throws PAOSException In case the PAOS module could not be initialized.
     */
    public PAOS(@Nonnull Dispatcher dispatcher, @Nonnull TlsConnectionHandler tlsHandler) throws PAOSException {
	this.dispatcher = dispatcher;
	this.tlsHandler = tlsHandler;

	try {
	    this.idGenerator = new MessageIdGenerator();
	    this.m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new PAOSException(e);
	}
    }

    private String getRelatesTo(SOAPMessage msg) throws SOAPException {
	return getHeaderElement(msg, RELATES_TO);
    }

    private void setRelatesTo(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElement(msg, RELATES_TO, true);
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
	String otherID = idGenerator.getRemoteID();
	String newID = idGenerator.createNewID(); // also swaps messages in
	// MessageIdGenerator
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
	    if (! idGenerator.setRemoteID(id)) {
		// IDs don't match throw exception
		throw new PAOSException("MessageID from result doesn't match.");
	    }
	} catch (SOAPException e) {
	    logger.error(e.getMessage(), e);
	    throw new PAOSException(e.getMessage(), e);
	}
    }

    private String getMessageID(SOAPMessage msg) throws SOAPException {
	return getHeaderElement(msg, MESSAGE_ID);
    }

    private void setMessageID(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElement(msg, MESSAGE_ID, true);
	elem.setTextContent(value);
    }

    private Object processPAOSRequest(InputStream content) throws PAOSException {
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    updateMessageID(msg);

	    if (logger.isDebugEnabled()) {
		try {
		    logger.debug("Message received:\n{}", m.doc2str(doc));
		} catch (TransformerException ex) {
		    logger.warn("Failed to log PAOS request message.", ex);
		}
	    }

	    return m.unmarshal(msg.getSOAPBody().getChildElements().get(0));
	} catch (MarshallingTypeException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new PAOSException(ex.getMessage(), ex);
	} catch (WSMarshallerException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new PAOSException(ex.getMessage(), ex);
	} catch (IOException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new PAOSException(ex.getMessage(), ex);
	} catch (SAXException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new PAOSException(ex.getMessage(), ex);
	}
    }

    private String createPAOSResponse(Object obj) throws MarshallingTypeException, SOAPException, TransformerException {
	SOAPMessage msg = createSOAPMessage(obj);
	String result = m.doc2str(msg.getDocument());

	logger.debug("Message sent:\n{}", result);

	return result;
    }

    private SOAPMessage createSOAPMessage(Object content) throws MarshallingTypeException, SOAPException {
	Document contentDoc = m.marshal(content);
	SOAPMessage msg = m.add2soap(contentDoc);
	SOAPHeader header = msg.getSOAPHeader();

	// fill header with paos stuff
	Element paos = header.addHeaderElement(PAOS_PAOS);
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "actor", ECardConstants.ACTOR_NEXT);
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "mustUnderstand", "1");

	Element version = header.addChildElement(paos, PAOS_VERSION);
	version.setTextContent(ECardConstants.PAOS_VERSION_20);

	Element endpointReference = header.addChildElement(paos, PAOS_ENDPOINTREF);
	Element address = header.addChildElement(endpointReference, PAOS_ADDRESS);
	address.setTextContent("http://www.projectliberty.org/2006/01/role/paos");
	Element metaData = header.addChildElement(endpointReference, PAOS_METADATA);
	Element serviceType = header.addChildElement(metaData, PAOS_SERVICETYPE);
	serviceType.setTextContent(ECardConstants.PAOS_NEXT);

	Element replyTo = header.addHeaderElement(REPLY_TO);
	address = header.addChildElement(replyTo, ADDRESS);
	address.setTextContent("http://www.projectliberty.org/2006/02/role/paos");

	// add message IDs
	addMessageIDs(msg);
	return msg;
    }

    /**
     * Sends start PAOS and answers all successor messages to the server associated with this instance.
     * Messages are exchanged until the server replies with a {@code StartPAOSResponse} message.
     *
     * @param message The StartPAOS message which is sent in the first message.
     * @return The {@code StartPAOSResponse} message from the server.
     * @throws DispatcherException In case there errors with the message conversion or the dispatcher.
     * @throws PAOSException In case there were errors in the transport layer.
     */
    public StartPAOSResponse sendStartPAOS(StartPAOS message) throws DispatcherException, PAOSException {
	Object msg = message;

	try {
	    // loop and send makes a computer happy
	    while (true) {
		// set up connection to PAOS endpoint
		StreamHttpClientConnection conn;
		try {
		    conn = openHttpStream(ProtocolVersion.TLSv11);
		} catch (IOException ex) {
		    logger.error("Connecting to the PAOS endpoint with TLSv1.1 failed. Falling back to TLSv1.0.", ex);
		    conn = openHttpStream(ProtocolVersion.TLSv10);
		}

		HttpContext ctx = new BasicHttpContext();
		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
		DefaultConnectionReuseStrategy reuse = new DefaultConnectionReuseStrategy();
		boolean isReusable;
		// send as long as connection is valid
		do {
		    // prepare request
		    String resource = tlsHandler.getResource();
		    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", resource);
		    req.setParams(conn.getParams());
		    HttpRequestHelper.setDefaultHeader(req, tlsHandler.getServerAddress());
		    req.setHeader(HEADER_KEY_PAOS, HEADER_VALUE_PAOS);
		    // this is how it would be correct
		    //req.setHeader("Accept", "text/html;q=0.2, application/vnd.paos+xml");
		    // and this is how it works :-/
		    req.setHeader("Accept", "text/html; application/vnd.paos+xml");
		    ContentType reqContentType = ContentType.create("application/vnd.paos+xml", "UTF-8");
		    HttpUtils.dumpHttpRequest(logger, "before adding content", req);
		    String reqMsgStr = createPAOSResponse(msg);
		    StringEntity reqMsg = new StringEntity(reqMsgStr, reqContentType);
		    req.setEntity(reqMsg);
		    req.setHeader(reqMsg.getContentType());
		    req.setHeader("Content-Length", Long.toString(reqMsg.getContentLength()));
		    // send request and receive response
		    HttpResponse response = httpexecutor.execute(req, conn, ctx);
		    int statusCode = response.getStatusLine().getStatusCode();
		    conn.receiveResponseEntity(response);
		    HttpEntity entity = response.getEntity();
		    byte[] entityData = FileUtils.toByteArray(entity.getContent());
		    HttpUtils.dumpHttpResponse(logger, response, entityData);
		    checkHTTPStatusCode(msg, statusCode);
		    // consume entity
		    Object requestObj = processPAOSRequest(new ByteArrayInputStream(entityData));

		    // break when message is startpaosresponse
		    if (requestObj instanceof StartPAOSResponse) {
			StartPAOSResponse startPAOSResponse = (StartPAOSResponse) requestObj;
			WSHelper.checkResult(startPAOSResponse);
			return startPAOSResponse;
		    }

		    // send via dispatcher
		    msg = dispatcher.deliver(requestObj);

		    // check if connection can be used one more time
		    isReusable = reuse.keepAlive(response, ctx);
		} while (isReusable);
	    }
	} catch (HttpException ex) {
	    throw new PAOSException("Failed to deliver or receive PAOS HTTP message.", ex);
	} catch (IOException ex) {
	    throw new PAOSException(ex);
	} catch (SOAPException ex) {
	    throw new PAOSException("Failed to create SOAP message instance from given JAXB message.", ex);
	} catch (URISyntaxException ex) {
	    throw new PAOSException("Hostname or port of the remote server are invalid.", ex);
	} catch (MarshallingTypeException ex) {
	    throw new DispatcherException("Failed to marshal JAXB object.", ex);
	} catch (InvocationTargetException ex) {
	    throw new DispatcherException("The dispatched method threw an exception.", ex);
	} catch (TransformerException ex) {
	    throw new DispatcherException(ex);
	} catch (WSException ex) {
	    throw new PAOSException(ex);
	}
    }


    private StreamHttpClientConnection openHttpStream(ProtocolVersion tlsVersion)
	    throws IOException, URISyntaxException {
	StreamHttpClientConnection conn;
	if (tlsHandler.usesTls()) {
	    TlsClientProtocol handler = tlsHandler.createTlsConnection(tlsVersion);
	    conn = new StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream());
	    saveServiceCertificate();
	} else {
	    // TODO: remove some time
	    // no TLS
	    Socket socket = ProxySettings.getDefault().getSocket(tlsHandler.getHostname(), tlsHandler.getPort());
	    conn = new StreamHttpClientConnection(socket.getInputStream(), socket.getOutputStream());
	}
	return conn;
    }

    /**
     * Stores the received eService certificate as {@link Certificate} in the dynamic context.
     * This will only take place when {@link DynamicAuthentication} is used as {@link TlsAuthentication}.
     */
    private void saveServiceCertificate() {
	try {
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    TlsAuthentication authentication = tlsHandler.getTlsClient().getAuthentication();
	    if (authentication instanceof DynamicAuthentication) {
		DynamicAuthentication noAuth = (DynamicAuthentication) authentication;
		// server certificate is the first one in the chain
		org.openecard.bouncycastle.crypto.tls.Certificate certificate = noAuth.getServerCertificate();
		dynCtx.put(TR03112Keys.ESERVICE_CERTIFICATE, certificate);
	    } else {
		String msg = "eService Certificate not saved in DynamicContext.";
		logger.debug(msg);
	    }
	} catch (IOException e) {
	    logger.error("Certificate couldn't be encoded.", e);
	}
    }

    /**
     * Check the status code returned from the server. 
     * If the status code indicates an error, a PAOSException will be thrown.
     * 
     * @param msg The last message we sent to the server
     * @param statusCode The status code we received from the server
     * @throws PAOSException If the server returned a HTTP error code
     */
    private void checkHTTPStatusCode(Object msg, int statusCode) throws PAOSException {
	if (statusCode < 200 || statusCode > 299) {
	    if (msg instanceof ResponseType) {
		ResponseType resp = (ResponseType) msg;
		try {
		    WSHelper.checkResult(resp);
		} catch (WSException ex) {
		    throw new PAOSException("Received HTML Error Code " + statusCode, ex);
		}
	    }
	    throw new PAOSException("Received HTML Error Code " + statusCode);
	}
    }

}
