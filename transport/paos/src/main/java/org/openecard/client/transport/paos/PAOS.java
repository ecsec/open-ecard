package org.openecard.client.transport.paos;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.w3c.dom.Document;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>, Tobias Wich <tobias.wich@ecsec.de>
 */
public class PAOS {

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
    
    public PAOS(String endpoint, Dispatcher dispatcher) {
        this.endpoint = endpoint;
        this.dispatcher = dispatcher;
    }

    private String getRelatesTo(SOAPMessage msg) throws SOAPException {
	return getHeaderElemStr(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"));
    }

    private void setRelatesTo(SOAPMessage msg, String value) throws SOAPException {
	SOAPHeaderElement elem = getHeaderElem(msg, new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"), true);
	elem.setTextContent(value);
    }

    private String getMessageIdentifier(SOAPMessage msg) throws SOAPException {
	return getHeaderElemStr(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"));
    }

    private void setMessageIdentifier(SOAPMessage msg, String value) throws SOAPException {
	SOAPHeaderElement elem = getHeaderElem(msg, new QName(ECardConstants.WS_ADDRESSING, "MessageID"), true);
	elem.setTextContent(value);
    }

    private String getHeaderElemStr(SOAPMessage msg, QName elem) throws SOAPException {
	String result = null;
	SOAPHeader h = msg.getSOAPHeader();
	SOAPHeaderElement headerElem = getHeaderElem(msg, elem, false);
	return (headerElem == null) ? null : headerElem.getTextContent().trim();
    }

    private SOAPHeaderElement getHeaderElem(SOAPMessage msg, QName elem, boolean create) throws SOAPException {
	SOAPHeaderElement result = null;
	SOAPHeader h = msg.getSOAPHeader();
	// try to find a header
	Iterator<SOAPHeaderElement> i = h.examineAllHeaderElements();
	while (i.hasNext()) {
	    SOAPHeaderElement e = i.next();
	    if (e.getElementQName().equals(elem)) {
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
	String newId = MessageGenerator.createNewId(); // also swaps messages in MessageGenerator
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
		//throw new PAOSException("No MessageID in PAOS header.");
	    }
	    if (!MessageGenerator.setRemoteId(id)) {
		// ids don't match throw exception
		throw new PAOSException("MessageID from result doesn't match.");
	    }
	} catch (SOAPException ex) {
	    Logger.getLogger(PAOS.class.getName()).log(Level.SEVERE, null, ex);
	    throw new PAOSException(ex.getMessage(), ex);
	}
    }

    public Object processPAOSRequest(String content) throws PAOSException {
	Exception e = null;
	try {
	    Document doc = m.str2doc(content);
	    SOAPMessage msg = m.doc2soap(doc);
	    System.out.println(m.doc2str(doc));
	    updateMessageId(msg);
            return m.unmarshal(msg.getSOAPBody().getFirstChild());
	} catch (Exception ex) {
	    Logger.getLogger(PAOS.class.getName()).log(Level.SEVERE, null, ex);
	    e = ex;
	}
	throw new PAOSException(e.getMessage(), e);
    }

    public String createPAOSResponse(Object obj) throws MarshallingTypeException, org.openecard.client.ws.SOAPException, SOAPException, TransformerException {
	SOAPMessage msg = createSOAPMessage(obj);
	String result = m.doc2str(msg.getSOAPPart());
	return result;
    }

    public String createStartPAOS(String sessionIdentifier, List<ConnectionHandleType> connectionHandles) throws MarshallingTypeException, org.openecard.client.ws.SOAPException, SOAPException, TransformerException {
	StartPAOS startPAOS = new StartPAOS();
	startPAOS.setSessionIdentifier(sessionIdentifier);
	startPAOS.setProfile(ECardConstants.Profile.ECARD_1_1);
	startPAOS.getConnectionHandle().addAll(connectionHandles);

	SOAPMessage soapMsg = createSOAPMessage(startPAOS);
	String responseStr = m.doc2str(soapMsg.getSOAPPart());
	System.out.println(responseStr);

	return responseStr;
    }

    private SOAPMessage createSOAPMessage(Object content) throws MarshallingTypeException, org.openecard.client.ws.SOAPException, SOAPException {
	Document contentDoc = m.marshal(content);
	SOAPMessage msg = m.add2soap(contentDoc);
	SOAPHeader header = msg.getSOAPHeader();
	// fill header with paos stuff
	SOAPHeaderElement paos = header.addHeaderElement(new QName(ECardConstants.PAOS_VERSION_20, "PAOS"));
	paos.addAttribute(new QName(ECardConstants.SOAP_ENVELOPE, "actor"), ECardConstants.ACTOR_NEXT);
	paos.addAttribute(new QName(ECardConstants.SOAP_ENVELOPE, "mustUnderstand"), "1");
	SOAPElement version = paos.addChildElement(new QName(ECardConstants.PAOS_VERSION_20, "Version"));
	version.setTextContent(ECardConstants.PAOS_VERSION_20);
	// add message ids
	addMessageIds(msg);
	return msg;
    }

    public StartPAOSResponse sendStartPAOS(StartPAOS message) throws Exception {
        Object msg = message;

        // loop and send makes a computer happy
        do {
            String s = createPAOSResponse(msg);
            System.out.println(s);
            URL url = new URL(endpoint);
            HttpURLConnection httpPost = (HttpURLConnection) url.openConnection();
            httpPost.setRequestMethod("POST");
            httpPost.setRequestProperty(ECardConstants.HEADER_KEY_ACCEPT, ECardConstants.HEADER_VALUE_ACCEPT);
            httpPost.setRequestProperty(ECardConstants.HEADER_KEY_PAOS, ECardConstants.HEADER_VALUE_PAOS);
            httpPost.setReadTimeout(0); // timeout is configured in paos endpoint
            // write message
            httpPost.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpPost.getOutputStream());
            writer.write(s);
            writer.flush();
            // read result
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpPost.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            writer.close();
            reader.close();
            Object requestObj = processPAOSRequest(result.toString());
            // break when message is startpaosresponse
            if (requestObj instanceof StartPAOSResponse) {
                return (StartPAOSResponse) requestObj;
            }
            // send via dispatcher
            msg = dispatcher.deliver(requestObj);
        } while (true);
    }

}
