/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EmptyResponseDataType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import oasis.names.tc.dss._1_0.core.schema.ResponseBaseType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.FileUtils;
import org.openecard.binding.tctoken.TlsConnectionHandler;
import org.openecard.httpcore.HttpRequestHelper;
import org.openecard.httpcore.HttpUtils;
import org.openecard.httpcore.StreamHttpClientConnection;
import org.openecard.sal.protocol.eac.transport.paos.MessageIdGenerator;
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
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.common.DynamicContext;
import org.openecard.common.interfaces.DocumentSchemaValidator;
import org.openecard.common.interfaces.DocumentValidatorException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.Promise;
import org.openecard.common.util.ValueGenerators;


/**
 * PAOS implementation for JAXB types.
 * This implementation can be configured to speak TLS by creating the instance with a TlsClient. The dispatcher instance
 * is used to deliver the messages to the instances implementing the webservice interfaces.
 *
 * @author Johannes Schmoelz
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class PAOS {

    private static final Logger LOG = LoggerFactory.getLogger(PAOS.class);

    public static final String HEADER_KEY_PAOS = "PAOS";

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

    private final String headerValuePaos;
    private final MessageIdGenerator idGenerator;
    private final WSMarshaller m;
    private final Dispatcher dispatcher;
    private final TlsConnectionHandler tlsHandler;

    private final String serviceString;
    private final DocumentSchemaValidator schemaValidator;
    private final Promise<DocumentValidatorException> validationError;

    /**
     * Creates a PAOS instance and configures it for a given endpoint.
     * If tlsClient is not null the connection must be HTTPs, else HTTP.
     *
     * @param dispatcher The dispatcher instance capable of dispatching the received messages.
     * @param tlsHandler The TlsClient containing the configuration of the yet to be established TLS channel, or
     *   {@code null} if TLS should not be used.
     * @param schemaValidator Schema Validator used to validate incoming messages.
     * @throws PAOSException In case the PAOS module could not be initialized.
     */
    public PAOS(@Nonnull Dispatcher dispatcher, @Nonnull TlsConnectionHandler tlsHandler,
	    @Nonnull DocumentSchemaValidator schemaValidator) throws PAOSException {
	this.dispatcher = dispatcher.getFilter();
	this.tlsHandler = tlsHandler;
	this.schemaValidator = schemaValidator;
	this.validationError = new Promise<>();
	this.serviceString = buildServiceString();
	this.headerValuePaos = String.format("ver=\"%s\" %s", ECardConstants.PAOS_VERSION_20, this.serviceString);

	try {
	    this.idGenerator = new MessageIdGenerator();
	    this.m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException ex) {
	    LOG.error(ex.getMessage(), ex);
	    throw new PAOSException(ex);
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
	SOAPHeader h = msg.getSoapHeader();
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
		throw new PAOSException(NO_MESSAGE_ID);
	    }
	    if (! idGenerator.setRemoteID(id)) {
		// IDs don't match throw exception
		throw new PAOSException(MESSAGE_ID_MISSMATCH);
	    }
	} catch (SOAPException e) {
	    LOG.error(e.getMessage(), e);
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

    private Object processPAOSRequest(InputStream content) throws PAOSException, DocumentValidatorException {
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    Element body = msg.getSoapBody().getChildElements().get(0);
	    updateMessageID(msg);

	    if (LOG.isDebugEnabled()) {
		try {
		    LOG.debug("Message received:\n{}", m.doc2str(doc));
		} catch (TransformerException ex) {
		    LOG.warn("Failed to log PAOS request message.", ex);
		}
	    }

	    // fix profile attribute if it is not present
	    // while there are the eID-Servers to blame, some don't get it right and actually Profile is a useless attribute anyway
	    if (body.getLocalName().equals("StartPAOSResponse") && ! body.hasAttribute("Profile")) {
		LOG.warn("Received message without Profile attribute, adding one for proper validation.");
		body.setAttribute("Profile", ECardConstants.Profile.ECARD_1_1);

		try {
		    // copy or the validation produces strange errors
		    String docStr = m.doc2str(body);
		    Document newDoc = m.str2doc(docStr);
		    body = newDoc.getDocumentElement();
		} catch (SAXException | TransformerException ex) {
		    throw new PAOSException("Failed to copy document.", ex);
		}
	    }

	    // validate input message
	    schemaValidator.validate(body);

	    return m.unmarshal(body);
	} catch (MarshallingTypeException ex) {
	    LOG.error(ex.getMessage(), ex);
	    throw new PAOSException(ex.getMessage(), ex);
	} catch (WSMarshallerException ex) {
	    String msg = "Failed to read/process message from PAOS server.";
	    LOG.error(msg, ex);
	    throw new PAOSException(MARSHALLING_ERROR, ex);
	} catch (IOException | SAXException ex) {
	    String msg = "Failed to read/process message from PAOS server.";
	    LOG.error(msg, ex);
	    throw new PAOSException(SOAP_MESSAGE_FAILURE, ex);
	}
    }

    private String createPAOSResponse(Object obj) throws MarshallingTypeException, SOAPException, TransformerException {
	SOAPMessage msg = createSOAPMessage(obj);
	String result = m.doc2str(msg.getDocument());

	LOG.debug("Message sent:\n{}", result);

	return result;
    }

    private SOAPMessage createSOAPMessage(Object content) throws MarshallingTypeException, SOAPException {
	Document contentDoc = m.marshal(content);

	try {
	    String docStr = m.doc2str(contentDoc);
	    Document newDoc = m.str2doc(docStr);
	    schemaValidator.validate(newDoc);
	} catch (DocumentValidatorException ex) {
	    LOG.warn("Schema validation of outgoing message failed.", ex);
	} catch (SAXException | TransformerException ex) {
	    throw new MarshallingTypeException("Failed to copy document.", ex);
	}

	SOAPMessage msg = m.add2soap(contentDoc);
	SOAPHeader header = msg.getSoapHeader();

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
     * @throws PAOSConnectionException
     */
    public StartPAOSResponse sendStartPAOS(StartPAOS message) throws DispatcherException, PAOSException,
	    PAOSConnectionException {
	Object msg = message;
	StreamHttpClientConnection conn = null;
	HttpContext ctx = new BasicHttpContext();
	HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
	DefaultConnectionReuseStrategy reuse = new DefaultConnectionReuseStrategy();
	boolean connectionDropped = false;
	ResponseBaseType lastResponse = null;
	String firstOecMinorError = null;
	byte[] fakeSlotHandle = null;
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	String internalSessionIdentifier = null;
	final List<ConnectionHandleType> connectionHandles = message.getConnectionHandle();
	if (connectionHandles != null) {
	    for (ConnectionHandleType connectionHandle : connectionHandles) {
		if (fakeSlotHandle == null) {
		    fakeSlotHandle = ValueGenerators.generateRandom(32);
		}
		connectionHandle.setSlotHandle(fakeSlotHandle);

		ChannelHandleType channelHandle = connectionHandle.getChannelHandle();
		if (channelHandle != null) {
		    String currentSessionIdentifier = channelHandle.getSessionIdentifier();
		    if (currentSessionIdentifier != null) {
			internalSessionIdentifier = currentSessionIdentifier;
			channelHandle.setSessionIdentifier(null);
		    }
		}
	    }
	}

	try {
	    // loop and send makes a computer happy
	    while (true) {
		// set up connection to PAOS endpoint
		// if this one fails we may not continue
		conn = openHttpStream();

		boolean isReusable;
		// send as long as connection is valid
		try {
		    do {
			// save the last message we sent to the eID-Server.
			if (msg instanceof ResponseBaseType) {
			    lastResponse = (ResponseBaseType) msg;
			    // save first minor code if there is one returned from our stack
			    if (firstOecMinorError == null) {
				Result r = lastResponse.getResult();
				if (r != null) {
				    String minor = r.getResultMinor();
				    if (minor != null) {
					firstOecMinorError = minor;
				    }
				}
			    }
			}
			// prepare request
			String resource = tlsHandler.getResource();
			BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", resource);
			HttpRequestHelper.setDefaultHeader(req, tlsHandler.getServerAddress());
			req.setHeader(HEADER_KEY_PAOS, headerValuePaos);
			req.setHeader("Accept", "text/xml, application/xml, application/vnd.paos+xml");

			ContentType reqContentType = ContentType.create("application/vnd.paos+xml", "UTF-8");
			HttpUtils.dumpHttpRequest(LOG, "before adding content", req);
			String reqMsgStr = createPAOSResponse(msg);
			StringEntity reqMsg = new StringEntity(reqMsgStr, reqContentType);
			req.setEntity(reqMsg);
			req.setHeader(reqMsg.getContentType());
			req.setHeader("Content-Length", Long.toString(reqMsg.getContentLength()));
			// send request and receive response
			LOG.debug("Sending HTTP request.");
			HttpResponse response = httpexecutor.execute(req, conn, ctx);
			LOG.debug("HTTP response received.");
			int statusCode = response.getStatusLine().getStatusCode();

			try {
			    checkHTTPStatusCode(statusCode);
			} catch (PAOSConnectionException ex) {
			    // The eID-Server or at least the test suite may have aborted the communication after an
			    // response with error. So check the status of our last response to the eID-Server
			    if (lastResponse != null) {
				WSHelper.checkResult(lastResponse);
			    }
			    throw ex;
			}

			conn.receiveResponseEntity(response);
			HttpEntity entity = response.getEntity();
			byte[] entityData = FileUtils.toByteArray(entity.getContent());
			HttpUtils.dumpHttpResponse(LOG, response, entityData);
			try {
			    // consume entity
			    Object requestObj = processPAOSRequest(new ByteArrayInputStream(entityData));

			    // break when message is startpaosresponse
			    if (requestObj instanceof StartPAOSResponse) {
				StartPAOSResponse startPAOSResponse = (StartPAOSResponse) requestObj;

				// if the last error was a schema validation error, then we must communicate that to caller
				DocumentValidatorException validateEx = validationError.derefNonblocking();
				if (validateEx != null) {
				    LOG.debug("Previous validation error found, terminating process.");
				    throw validateEx;
				}

				// Some eID-Servers ignore error from previous steps so check whether our last message was ok.
				// This does not in case we sent a correct message with wrong content and the eID-Server returns
				// an ok.
				if (lastResponse != null) {
				    WSHelper.checkResult(lastResponse);
				}
				WSHelper.checkResult(startPAOSResponse);
				return startPAOSResponse;
			    }
			    if (requestObj instanceof DIDAuthenticate) {
				DIDAuthenticate asDidAuthenticate = (DIDAuthenticate)requestObj;
				ConnectionHandleType currentHandle = asDidAuthenticate.getConnectionHandle();
				if (currentHandle != null) {
				    byte[] currentSlotHandle = currentHandle.getSlotHandle();
				    byte[] fixedSlotHandle = fixSlotHandle(fakeSlotHandle, currentSlotHandle, dynCtx);
				    currentHandle.setSlotHandle(fixedSlotHandle);

				    if (internalSessionIdentifier != null) {
					ChannelHandleType currentChannelHandle = currentHandle.getChannelHandle();
					if (currentChannelHandle == null) {
					    currentChannelHandle = new ChannelHandleType();
					    currentHandle.setChannelHandle(currentChannelHandle);
					}
					String currentSessionIdentifier = currentChannelHandle.getSessionIdentifier();
					if (!internalSessionIdentifier.equals(currentSessionIdentifier)) {
					    currentChannelHandle.setSessionIdentifier(internalSessionIdentifier);
					}
				    }
				}
			    } else if (requestObj instanceof Transmit) {
				Transmit asTransmit = (Transmit)requestObj;
				byte[] currentSlotHandle = asTransmit.getSlotHandle();
				byte[] fixedSlotHandle = fixSlotHandle(fakeSlotHandle, currentSlotHandle, dynCtx);
				asTransmit.setSlotHandle(fixedSlotHandle);
			    }

			    // send via dispatcher
			    msg = dispatcher.deliver(requestObj);
			} catch (DocumentValidatorException ex) {
			    LOG.error("PAOS input message failed to validate.", ex);

			    // the ecard API forces us to interpret the message because the response must be the equivalent message not a fault
			    Object responseObj = synthesizeObj(new ByteArrayInputStream(entityData), ex);
			    if (responseObj != null) {
				msg = responseObj;
				if (! validationError.isDelivered()) {
				    validationError.deliver(ex);
				}
			    } else {
				// do the right thing and throw an error
				throw ex;
			    }
			}

			// check if connection can be used one more time
			isReusable = reuse.keepAlive(response, ctx);
			connectionDropped = false;
		    } while (isReusable);
		} catch (IOException ex) {
		    if (! connectionDropped) {
			connectionDropped = true;
			LOG.warn("PAOS server closed the connection. Trying to connect again.");
		    } else {
			String errMsg = "Error in the link to the PAOS server.";
			LOG.error(errMsg);
			throw new PAOSException(DELIVERY_FAILED, ex);
		    }
		}
	    }
	} catch (HttpException ex) {
	    throw new PAOSException(DELIVERY_FAILED, ex);
	} catch (SOAPException ex) {
	    throw new PAOSException(SOAP_MESSAGE_FAILURE, ex);
	} catch (DocumentValidatorException ex) {
	    throw new PAOSException(SCHEMA_VALIDATION_FAILED, ex);
	} catch (MarshallingTypeException ex) {
	    throw new PAOSDispatcherException(MARSHALLING_ERROR, ex);
	} catch (InvocationTargetException ex) {
	    throw new PAOSDispatcherException(DISPATCHER_ERROR, ex);
	} catch (TransformerException ex) {
	    throw new DispatcherException(ex.getMessage(), ex);
	} catch (WSException ex) {
	    PAOSException newEx = new PAOSException(ex);
	    if (firstOecMinorError != null) {
		newEx.setAdditionalResultMinor(firstOecMinorError);
	    }
	    throw newEx;
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
	    } catch (IOException ex) {
//		throw new PAOSException(ex);
	    }
	}
    }

    private byte[] fixSlotHandle(byte[] fakeSlotHandle, byte[] currentSlotHandle, DynamicContext dynCtx) {
	if (fakeSlotHandle != null && ByteUtils.compare(currentSlotHandle, fakeSlotHandle)) {
	    ConnectionHandleType conHandle = (ConnectionHandleType) dynCtx.get(TR03112Keys.CONNECTION_HANDLE);
	    if (conHandle != null) {
		currentSlotHandle = conHandle.getSlotHandle();
	    }
	}
	return currentSlotHandle;
    }


    private StreamHttpClientConnection openHttpStream() throws PAOSConnectionException {
        StreamHttpClientConnection conn;
	try {
	    LOG.debug("Opening connection to PAOS server.");
            TlsClientProtocol handler = tlsHandler.createTlsConnection();
            conn = new StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream());
	    LOG.debug("Connection to PAOS server established.");
            return conn;
        } catch (IOException | URISyntaxException ex) {
            throw new PAOSConnectionException(ex);
        }
    }

    /**
     * Check the status code returned from the server.
     * If the status code indicates an error, a PAOSException will be thrown.
     *
     * @param statusCode The status code we received from the server
     * @throws PAOSException If the server returned a HTTP error code
     */
    private void checkHTTPStatusCode(int statusCode) throws PAOSConnectionException {
	// Check the result code. According to the PAOS Spec section 9.4 the server has to send 202
	// All tested test servers return 200 so accept both but generate a warning message in case of 200
	if (statusCode != 200 && statusCode != 202) {
	    throw new PAOSConnectionException(INVALID_HTTP_STATUS, statusCode);
	} else if (statusCode == 200) {
	    String msg2 = "The PAOS endpoint sent the http status code 200 which does not conform to the "
		    + "PAOS specification. (See section 9.4 Processing Rules of the PAOS Specification)";
	    LOG.warn(msg2);
	}
    }

    /**
     * Creates a String with all available services.
     *
     * @return A String containing all available services.
     */
    private String buildServiceString() {
	StringBuilder builder = new StringBuilder();
	for (String service : dispatcher.getServiceList()) {
	    builder.append(";");
	    builder.append('"');
	    builder.append(service);
	    builder.append('"');
	}
	return builder.toString();
    }

    @Nullable
    private Object synthesizeObj(ByteArrayInputStream content, DocumentValidatorException cause) {
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    Element body = msg.getSoapBody().getChildElements().get(0);
	    Object obj = m.unmarshal(body);

	    if (obj instanceof DIDAuthenticate) {
		DIDAuthenticate didAuth = (DIDAuthenticate) obj;
		DIDAuthenticationDataType protoIn = didAuth.getAuthenticationProtocolData();

		Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, cause.getMessage());
		DIDAuthenticateResponse res = WSHelper.makeResponse(DIDAuthenticateResponse.class, r);
		EmptyResponseDataType protoData = new EmptyResponseDataType();
		res.setAuthenticationProtocolData(protoData);
		if (protoIn != null) {
		    protoData.setProtocol(protoIn.getProtocol());
		}

		return res;
	    }

	    // no special case needed
	    return null;
	} catch (IOException | SAXException | WSMarshallerException ex) {
	    // in case of error, just quit
	    return null;
	}
    }

}
