package org.openecard.client.ws.jaxb;

import javax.xml.namespace.QName;
import static junit.framework.Assert.assertNotNull;
import org.junit.Test;
import org.openecard.client.ws.soap.MessageFactory;
import org.openecard.client.ws.soap.SOAPBody;
import org.openecard.client.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MarshalTest {

    String xmlStr
	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	+ "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:addr=\"http://www.w3.org/2005/03/addressing\" xmlns:paos20=\"urn:liberty:paos:2006-08\">\n"
	+ "  <soap11:Header>\n"
	+ "    <paos20:PAOS soap11:actor=\"http://schemas.xmlsoap.org/soap/actor/next\" soap11:mustUnderstand=\"1\">\n"
	+ "      <paos20:Version>urn:liberty:paos:2006-08</paos20:Version>\n"
	+ "    </paos20:PAOS>\n"
	+ "    <MessageID xmlns=\"http://www.w3.org/2005/03/addressing\">urn:uuid:00dcda36-bc0b-11df-b497-0a0027000000</MessageID>\n"
	+ "  </soap11:Header>\n"
	+ "  <soap11:Body>\n"
	+ "    <ns3:StartPAOS xmlns:ns3=\"urn:iso:std:iso-iec:24727:tech:schema\"\n"
	+ "                   xmlns=\"urn:oasis:names:tc:dss:1.0:core:schema\"\n"
	+ "                   xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"\n"
	+ "                   xmlns:ns4=\"http://ws.gematik.de/fa/vods/eVerordnungXML/v6.0\"\n"
	+ "                   xmlns:ns5=\"http://www.w3.org/2001/04/xmlenc#\"\n"
	+ "                   xmlns:ns6=\"http://ws.gematik.de/fa/vods/TransportPayload/v6.0\"\n"
	+ "                   xmlns:ns7=\"http://ws.gematik.de/fa/vods/eDispensierungXML/v6.0\"\n"
	+ "                   xmlns:ns8=\"http://ws.epotheke.com/bs/v1.0\" Profile=\"http://ws.ecsec.de\">\n"
	+ "      <ns3:SessionIdentifier>0123456789</ns3:SessionIdentifier>\n"
	+ "      <ns3:ConnectionHandle>\n"
	+ "        <ns3:ContextHandle>A5E007BE249B4A747C6BD0CFCAD584D9</ns3:ContextHandle>\n"
	+ "        <ns3:IFDName>SCM SCR 3311 [CCID Interface] (21120940200534) 00 00</ns3:IFDName>\n"
	+ "        <ns3:SlotHandle>3C78847D2A6BCD56B7CB320FB92960F1D68C04A1</ns3:SlotHandle>\n"
	+ "        <ns3:RecognitionInfo>\n"
	+ "          <ns3:CardType>http://ws.gematik.de/egk/1.0.0</ns3:CardType>\n"
	+ "          <ns3:CardIdentifier>80276883111211400934</ns3:CardIdentifier>\n"
	+ "          <ns3:CaptureTime>2010-09-09T14:08:16.720+02:00</ns3:CaptureTime>\n"
	+ "        </ns3:RecognitionInfo>\n"
	+ "      </ns3:ConnectionHandle>\n"
	+ "      <ns3:ConnectionHandle>\n"
	+ "        <ns3:ContextHandle>A5E007BE249B4A747C6BD0CFCAD584D9</ns3:ContextHandle>\n"
	+ "        <ns3:IFDName>SCM SCR 3311 [CCID Interface] (21120940200534) 00 00</ns3:IFDName>\n"
	+ "        <ns3:SlotHandle>CF7A39B71D9D85D211B231B7A082485BDAAF9F96</ns3:SlotHandle>\n"
	+ "        <ns3:RecognitionInfo>\n"
	+ "          <ns3:CardType>http://ws.gematik.de/egk/1.0.0</ns3:CardType>\n"
	+ "          <ns3:CardIdentifier>80276883111211400934</ns3:CardIdentifier>\n"
	+ "          <ns3:CaptureTime>2010-09-09T14:08:47.942+02:00</ns3:CaptureTime>\n"
	+ "        </ns3:RecognitionInfo>\n"
	+ "      </ns3:ConnectionHandle>\n"
	+ "    </ns3:StartPAOS>\n"
	+ "  </soap11:Body>\n"
	+ "</soap11:Envelope>";

    @Test
    public void testConversion() throws Exception {
	JAXBMarshaller m = new JAXBMarshaller();
	Document doc = m.str2doc(xmlStr);

	org.openecard.client.ws.soap.SOAPMessage msg = m.doc2soap(doc);
	SOAPBody body = msg.getSOAPBody();
	Node result = body.getChildElements().get(0);

	//System.out.println(m.doc2str(result));
	Object o = m.unmarshal(result);
	doc = m.marshal(o);
	assertNotNull(doc);
	//System.out.println(m.doc2str(doc));
    }

    @Test
    public void testSOAPMarshal() throws Exception {
	JAXBMarshaller m = new JAXBMarshaller();
    	Document doc = m.str2doc(xmlStr);
	SOAPMessage msg = m.doc2soap(doc);

    	Object o = m.unmarshal(msg.getSOAPBody().getChildElements().get(0));
	doc = m.marshal(o);

	MessageFactory factory = MessageFactory.newInstance();
	SOAPMessage soapMsg = factory.createMessage();
	soapMsg.getSOAPBody().addDocument(doc);
	//soapMsg.writeTo(System.out);
    }

    @Test
    public void testSoapHeaderAdd() throws Exception {
	JAXBMarshaller m = new JAXBMarshaller();
    	Document doc = m.str2doc(xmlStr);
	SOAPMessage msg = m.doc2soap(doc);

	Element msgId=null;
	// check if messageid is present
	for (Element next : msg.getSOAPHeader().getChildElements()) {
	    if (next.getNodeName().equals("MessageID") && next.getNamespaceURI().equals("http://www.w3.org/2005/03/addressing")) {
		msgId = next;
	    }
	}
	assertNotNull(msgId);

	// add relates to
	Element relates = msg.getSOAPHeader().addHeaderElement(new QName("http://www.w3.org/2005/03/addressing", "RelatesTo"));
	relates.setTextContent("relates to fancy id");

	System.out.println(m.doc2str(msg.getDocument()));
    }

}
