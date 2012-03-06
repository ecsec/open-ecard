/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.transport.paos;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import org.openecard.client.ws.soap.SOAPException;
import org.openecard.client.ws.soap.SOAPHeader;
import org.openecard.client.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PAOS {

    static {
	javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
	    // FIXME
	    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
		return true;
	    }
	});
    }

    static {
	try {
	    m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static final Logger _logger = LogManager.getLogger(PAOS.class.getName());
    private static final WSMarshaller m;
    private final String endpoint;
    private final Dispatcher dispatcher;
    private SSLSocketFactory socketFactory;
    private PAOSCallback callback;

    public PAOS(String endpoint, Dispatcher dispatcher, PAOSCallback callback, SSLSocketFactory sockFac) {
	this.endpoint = endpoint;
	this.dispatcher = dispatcher;
	this.callback = callback;
	this.socketFactory = sockFac;
    }


    private String getRelatesTo(SOAPMessage msg) throws SOAPException {
	return getHeaderElemStr(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"));
    }

    private void setRelatesTo(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElem(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"), true);
	elem.setTextContent(value);
    }

    private String getMessageIdentifier(SOAPMessage msg) throws SOAPException {
	return getHeaderElemStr(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"));
    }

    private void setMessageIdentifier(SOAPMessage msg, String value) throws SOAPException {
	Element elem = getHeaderElem(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"), true);
	elem.setTextContent(value);
    }

    private String getHeaderElemStr(SOAPMessage msg, QName elem) throws SOAPException {
	Element headerElem = getHeaderElem(msg, elem, false);
	return (headerElem == null) ? null : headerElem.getTextContent().trim();
    }

    private Element getHeaderElem(SOAPMessage msg, QName elem, boolean create) throws SOAPException {
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

    private void addMessageIds(SOAPMessage msg) throws SOAPException {
	String otherId = MessageGenerator.getRemoteId();
	String newId = MessageGenerator.createNewId(); // also swaps messages in
	// MessageGenerator
	if (otherId != null) {
	    // add relatesTo element
	    setRelatesTo(msg, otherId);
	}
	// add MessageID element
	setMessageIdentifier(msg, newId);
    }

    private void updateMessageId(SOAPMessage msg) throws PAOSException {
	try {
	    String id = getMessageIdentifier(msg);
	    if (id == null) {
		throw new PAOSException("No MessageID in PAOS header.");
	    }
	    if (!MessageGenerator.setRemoteId(id)) {
		// ids don't match throw exception
		throw new PAOSException("MessageID from result doesn't match.");
	    }
	} catch (SOAPException ex) {
	    _logger.log(Level.SEVERE, null, ex);
	    throw new PAOSException(ex.getMessage(), ex);
	}
    }

    public Object processPAOSRequest(String content) throws PAOSException {
	Exception e;
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    updateMessageId(msg);
	    return m.unmarshal(msg.getSOAPBody().getChildElements().get(0));
	} catch (Exception ex) {
	    _logger.log(Level.SEVERE, null, ex);
	    e = ex;
	}
	throw new PAOSException(e.getMessage(), e);
    }

    public String createPAOSResponse(Object obj) throws MarshallingTypeException, SOAPException, TransformerException {
	SOAPMessage msg = createSOAPMessage(obj);
	String result = m.doc2str(msg.getDocument());
	return result;
    }

    public String createStartPAOS(String sessionIdentifier, List<ConnectionHandleType> connectionHandles) throws MarshallingTypeException,
    SOAPException, TransformerException {
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

	// add message ids
	addMessageIds(msg);
	return msg;
    }

    public StartPAOSResponse sendStartPAOS(StartPAOS message) throws Exception {
	Object msg = message;
	URL url = new URL(endpoint);

	// loop and send makes a computer happy
	while (true) {
	    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	    conn.setSSLSocketFactory(this.socketFactory);
	    conn.setDoOutput(true); // Triggers POST.
	    conn.setRequestProperty(ECardConstants.HEADER_KEY_PAOS, ECardConstants.HEADER_VALUE_PAOS);
	    conn.setRequestProperty(ECardConstants.HEADER_KEY_CONTENT_TYPE, ECardConstants.HEADER_VALUE_CONTENT_TYPE);
	    conn.setRequestProperty(ECardConstants.HEADER_KEY_ACCEPT, ECardConstants.HEADER_VALUE_ACCEPT);

	    OutputStream output = null;
	    try {
		output = conn.getOutputStream();
		output.write(this.createPAOSResponse(msg).getBytes("UTF-8"));
	    } finally {
		if (output != null)
		    try {
			output.close();
		    } catch (IOException e) {
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.WARNING)) {
			    _logger.logp(Level.WARNING, this.getClass().getName(), "sendStartPAOS(StartPAOS message)", e.getMessage(), e);
			} // </editor-fold>
		    }
	    }

	    InputStream response = conn.getInputStream();
	    StringBuffer sb = new StringBuffer();

	    BufferedReader reader = null;
	    try {
		reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
		for (String line; (line = reader.readLine()) != null;) {
		    sb.append(line);
		}
	    } finally {
		if (reader != null)
		    try {
			reader.close();
		    } catch (IOException e) {
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.WARNING)) {
			    _logger.logp(Level.WARNING, this.getClass().getName(), "sendStartPAOS(StartPAOS message)", e.getMessage(), e);
			} // </editor-fold>
		    }
	    }

	    Object requestObj = this.processPAOSRequest(sb.toString());
	    // break when message is startpaosresponse
	    if (requestObj instanceof StartPAOSResponse) {
		StartPAOSResponse startPAOSResponse = (StartPAOSResponse) requestObj;
		return startPAOSResponse;
	    } else if (requestObj instanceof InitializeFramework) {
		// connection seems to be successfully established, trigger
		// loading of refreshAddress (see. BSI TR-03112-7)
		new Thread(new Runnable() {
		    @Override
		    public void run() {
			callback.loadRefreshAddress();
		    }
		}).start();
	    }
	    // send via dispatcher
	    msg = dispatcher.deliver(requestObj);
	}
    }
}
