package org.openecard.client.ws.android;

import iso.std.iso_iec._24727.tech.schema.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.util.Helper;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WhitespaceFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMarshaller implements WSMarshaller {

    private static final String iso = "iso:";
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Transformer transformer;

    public AndroidMarshaller() {
	documentBuilderFactory = null;
	documentBuilder = null;
	transformer = null;
	try {
	    documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	} catch (Exception ex) {
	    ex.printStackTrace(System.err);
	    System.exit(1); // non recoverable
	}
    }

    @Override
    public synchronized String doc2str(Node doc) throws TransformerException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	transformer.transform(new DOMSource(doc), new StreamResult(out));
	String result;
	try {
	    result = out.toString("UTF-8");
	} catch (UnsupportedEncodingException ex) {
	    throw new TransformerException(ex);
	}
	return result;
    }

    @Override
    public synchronized Document marshal(Object o)  throws MarshallingTypeException {
	Document document = documentBuilder.newDocument();
	document.setXmlStandalone(true);
	Element rootElement = document.createElement(iso + o.getClass().getSimpleName());
	rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	document.appendChild(rootElement);

	if (o instanceof iso.std.iso_iec._24727.tech.schema.StartPAOS) {
	    StartPAOS startPAOSPOJO = (StartPAOS) o;

	    Element em = document.createElement(iso + "SessionIdentifier");
	    em.appendChild(document.createTextNode(startPAOSPOJO.getSessionIdentifier()));
	    rootElement.appendChild(em);

	    em = document.createElement(iso + "ConnectionHandle");
	    em.setAttribute("xsi:type", "iso:ConnectionHandleType");
	    Element em2 = document.createElement(iso + "ContextHandle");
	    em2.appendChild(document.createTextNode(Helper.convByteArrayToString(startPAOSPOJO.getConnectionHandle().get(0).getContextHandle())));
	    em.appendChild(em2);
	    em2 = document.createElement(iso + "SlotHandle");
	    em2.appendChild(document.createTextNode(Helper.convByteArrayToString(startPAOSPOJO.getConnectionHandle().get(0).getSlotHandle())));
	    em.appendChild(em2);
	    rootElement.appendChild(em);

	} else if (o instanceof TransmitResponse) {
	    TransmitResponse transmitResponsePOJO = (TransmitResponse) o;

	    Element em = document.createElement("Result");
	    Element em2 = document.createElement("ResultMajor");
	    em2.appendChild(document.createTextNode(transmitResponsePOJO.getResult().getResultMajor()));
	    // TODO result minor
	    em.appendChild(em2);
	    rootElement.appendChild(em);

	    for (int i = 0; i < transmitResponsePOJO.getOutputAPDU().size(); i++) {
		em = document.createElement(iso + "OutputAPDU");
		em.appendChild(document.createTextNode(Helper.convByteArrayToString(transmitResponsePOJO.getOutputAPDU().get(i))));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof EstablishContext) {
	    /* nothing more to do */

	} else if (o instanceof EstablishContextResponse) {
	    EstablishContextResponse establishContextResponse = (EstablishContextResponse) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(establishContextResponse.getContextHandle())));
	    rootElement.appendChild(em);

	    em = document.createElement("Result");
	    Element em2 = document.createElement("ResultMajor");
	    em2.appendChild(document.createTextNode(establishContextResponse.getResult().getResultMajor()));
	    // TODO result minor
	    em.appendChild(em2);
	    rootElement.appendChild(em);

	} else if (o instanceof GetStatus) {
	    GetStatus getStatus = (GetStatus) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(getStatus.getContextHandle())));
	    rootElement.appendChild(em);
	    if (getStatus.getIFDName()!=null) {
		em = document.createElement(iso + "IFDName");
		em.appendChild(document.createTextNode(getStatus.getIFDName()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Wait) {
	    Wait w = (Wait) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(w.getContextHandle())));
	    rootElement.appendChild(em);

	    if (w.getTimeOut() != null) {
		em = document.createElement(iso + "TimeOut");
		em.appendChild(document.createTextNode(w.getTimeOut().toString(16)));
		rootElement.appendChild(em);
	    }

	    if (w.getCallback() != null) {
		ChannelHandleType callback = w.getCallback();
		em = document.createElement(iso + "Callback");

		if (callback.getBinding() != null) {
		    Element em2 = document.createElement(iso + "Binding");
		    em2.appendChild(document.createTextNode(callback.getBinding()));
		    em.appendChild(em2);
		}
		if (callback.getSessionIdentifier() != null) {
		    Element em2 = document.createElement(iso + "SessionIdentifier");
		    em2.appendChild(document.createTextNode(callback.getSessionIdentifier()));
		    em.appendChild(em2);
		}
		if (callback.getProtocolTerminationPoint() != null) {
		    Element em2 = document.createElement(iso + "ProtocolTerminationPoint");
		    em2.appendChild(document.createTextNode(callback.getProtocolTerminationPoint()));
		    em.appendChild(em2);
		}
		if (callback.getPathSecurity() != null) {
		    PathSecurityType pathSecurityType = callback.getPathSecurity();
		    Element em2 = document.createElement(iso + "PathSecurity");
		    Element em3 = document.createElement(iso + "Protocol");

		    em3.appendChild(document.createTextNode(pathSecurityType.getProtocol()));
		    em2.appendChild(em3);
		    if (pathSecurityType.getParameters() != null) {
			em3 = document.createElement(iso + "Parameters");
			em3.appendChild(document.createTextNode(pathSecurityType.getParameters().toString()));
			em2.appendChild(em3);
		    }
		    em.appendChild(em2);
		}
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Connect) {
	    Connect c = (Connect) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(c.getContextHandle())));
	    rootElement.appendChild(em);

	    em = document.createElement(iso + "IFDName");
	    em.appendChild(document.createTextNode(c.getIFDName()));
	    rootElement.appendChild(em);

	    em = document.createElement(iso + "Slot");
	    em.appendChild(document.createTextNode(c.getSlot().toString()));
	    rootElement.appendChild(em);
	    if (c.isExclusive() != null) {
		em = document.createElement(iso + "Exclusive");
		em.appendChild(document.createTextNode(c.isExclusive().toString()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof ConnectResponse) {
	    ConnectResponse cr = (ConnectResponse) o;

	    Element em = document.createElement(iso + "SlotHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(cr.getSlotHandle())));
	    rootElement.appendChild(em);

	    em = document.createElement("Result");
	    Element em2 = document.createElement("ResultMajor");
	    em2.appendChild(document.createTextNode(cr.getResult().getResultMajor()));
	    // TODO result minor
	    em.appendChild(em2);
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDs) {
	    ListIFDs c = (ListIFDs) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(c.getContextHandle())));
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDsResponse) {
	    ListIFDsResponse listIFDsResponse = (ListIFDsResponse) o;

	    for (String s : listIFDsResponse.getIFDName()) {
		Element em = document.createElement(iso + "IFDName");
		em.appendChild(document.createTextNode(s));
		rootElement.appendChild(em);
	    }

	    Element em = document.createElement("Result");
	    Element em2 = document.createElement("ResultMajor");
	    em2.appendChild(document.createTextNode(listIFDsResponse.getResult().getResultMajor()));
	    // TODO result minor
	    em.appendChild(em2);
	    rootElement.appendChild(em);

	} else if (o instanceof Transmit) {
	    Transmit t = (Transmit) o;

	    Element em = document.createElement(iso + "SlotHandle");
	    em.appendChild(document.createTextNode(Helper.convByteArrayToString(t.getSlotHandle())));
	    rootElement.appendChild(em);

	    for (int i = 0; i < t.getInputAPDUInfo().size(); i++) {
		em = document.createElement(iso + "InputAPDUInfo");
		rootElement.appendChild(em);
		Element em2 = document.createElement(iso + "InputAPDU");
		em2.appendChild(document.createTextNode(Helper.convByteArrayToString(t.getInputAPDUInfo().get(i).getInputAPDU())));
		em.appendChild(em2);
		for (int y = 0; y < t.getInputAPDUInfo().get(i).getAcceptableStatusCode().size(); y++) {
		    em2 = document.createElement(iso + "AcceptableStatusCode");
		    em2.appendChild(document.createTextNode(Helper.convByteArrayToString(t.getInputAPDUInfo().get(i).getAcceptableStatusCode().get(y))));
		    em.appendChild(em2);
		}
	    }

	} else {
	    throw new IllegalArgumentException("Cannot marshal " + o.getClass().getSimpleName());
	}

	return document;
    }

    @Override
    public synchronized Document str2doc(String docStr) throws SAXException {
	try {
	    // read dom as w3
	    StringReader strReader = new StringReader(docStr);
	    InputSource inSrc = new InputSource(strReader);
	    Document doc = documentBuilder.parse(inSrc);

	    WhitespaceFilter.filter(doc);
	    return doc;
	} catch (IOException ex) {
	    throw new SAXException(ex);
	}
    }

    @Override
    public synchronized Document str2doc(InputStream docStr) throws SAXException, IOException {
	// read dom as w3
	Document doc;
	try {
	    doc = documentBuilder.parse(docStr);
	    WhitespaceFilter.filter(doc);
	    return doc;
	} catch (IOException e) {
	    throw new SAXException(e);
	}
    }

    @Override
    public synchronized Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException {
	Document newDoc = null;
	if (n instanceof Document) {
	    newDoc = (Document) n;
	} else if (n instanceof Element) {
	    newDoc = documentBuilder.newDocument();
	    Node root = newDoc.importNode(n, true);
	    newDoc.appendChild(root);
	} else {
	    throw new WSMarshallerException("Only w3c Document and Element are accepted.");
	}
	try {
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    XmlPullParser parser = factory.newPullParser();
	    parser.setInput(new InputStreamReader(new ByteArrayInputStream(this.doc2str(newDoc).getBytes("UTF-8"))));
	    int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT) {
		if (eventType == XmlPullParser.START_TAG) {
		    return parse(parser);
		}
		eventType = parser.next();
	    }
	    return null;
	} catch (Exception e) {
	    throw new MarshallingTypeException(e);
	}
    }

    private synchronized ResponseAPDUType parseResponseAPDUType(XmlPullParser parser) throws XmlPullParserException, IOException {
	ResponseAPDUType responseAPDUType = new ResponseAPDUType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Trailer")) {
		    responseAPDUType.setTrailer(Helper.convStringToByteArray(parser.nextText()));
		} else if (parser.getName().equals("Body")) {
		    responseAPDUType.setBody(this.parseDataMaskType(parser));
		} else if (parser.getName().equals("Conclusion")) {
		    responseAPDUType.setConclusion(this.parseConclusion(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ResponseAPDU")));
	return responseAPDUType;
    }

    private synchronized Conclusion parseConclusion(XmlPullParser parser) throws XmlPullParserException, IOException {
	Conclusion conc = new Conclusion();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RecognizedCardType")) {
		    conc.setRecognizedCardType(parser.nextText());
		} else if (parser.getName().equals("CardCall")) {
		    conc.getCardCall().add(this.parseCardCall(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Conclusion")));
	return conc;
    }

    private synchronized DataMaskType parseDataMaskType(XmlPullParser parser) throws XmlPullParserException, IOException {
	DataMaskType dataMaskType = new DataMaskType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Tag")) {
		    dataMaskType.setTag(Helper.convStringToByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingData")) {
		    dataMaskType.setMatchingData(this.parseMatchingDataType(parser));
		}

	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Body")));
	return dataMaskType;
    }

    private MatchingDataType parseMatchingDataType(XmlPullParser parser) throws XmlPullParserException, IOException {
	MatchingDataType matchingDataType = new MatchingDataType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Offset")) {
		    matchingDataType.setOffset(Helper.convStringToByteArray(parser.nextText()));
		} else if (parser.getName().equals("Length")) {
		    matchingDataType.setLength(Helper.convStringToByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingValue")) {
		    matchingDataType.setMatchingValue(Helper.convStringToByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("MatchingData")));

	return matchingDataType;
    }

    private synchronized CardCall parseCardCall(XmlPullParser parser) throws XmlPullParserException, IOException {
	CardCall c = new CardCall();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CommandAPDU")) {
		    c.setCommandAPDU(Helper.convStringToByteArray(parser.nextText()));
		} else if (parser.getName().equals("ResponseAPDU")) {
		    c.getResponseAPDU().add(this.parseResponseAPDUType(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardCall")));
	return c;
    }

    private synchronized Object parse(XmlPullParser parser) throws XmlPullParserException, IOException {
	if(parser.getName().equals("WaitResponse")){
	    WaitResponse waitResponse = new WaitResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			waitResponse.setResult(r);
		    } else if (parser.getName().equals("IFDEvent")){
			waitResponse.getIFDEvent().add(parseIFDStatusType(parser, "IFDEvent"));
		    } else if (parser.getName().equals("SessionIdentifier")){
			waitResponse.setSessionIdentifier(parser.nextText());
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("WaitResponse")));
	    return waitResponse;

	} else if(parser.getName().equals("GetStatusResponse")){
	    GetStatusResponse getStatusResponse = new GetStatusResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			getStatusResponse.setResult(r);
		    } else if (parser.getName().equals("IFDStatus")){
			getStatusResponse.getIFDStatus().add(parseIFDStatusType(parser, "IFDStatus"));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("GetStatusResponse")));
	    return getStatusResponse;

	} else if (parser.getName().equals("ListIFDs")) {
	    ListIFDs listIFDs = new ListIFDs();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ContextHandle")) {
			listIFDs.setContextHandle(Helper.convStringToByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ListIFDs")));
	    return listIFDs;

	} else if (parser.getName().equals("GetRecognitionTreeResponse")) {
	    GetRecognitionTreeResponse resp = new GetRecognitionTreeResponse();
	    RecognitionTree recTree = new RecognitionTree();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			resp.setResult(r);
		    } else if (parser.getName().equals("CardCall")) {
			recTree.getCardCall().add(this.parseCardCall(parser));
		    }
		} else if (eventType == XmlPullParser.END_TAG) {
		    if (parser.getName().equals("CardCall")) {

		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("GetRecognitionTreeResponse")));
	    resp.setRecognitionTree(recTree);
	    return resp;

	} else if (parser.getName().equals("EstablishContext")) {
	    EstablishContext establishContext = new EstablishContext();
	    return establishContext;

	} else if (parser.getName().equals("EstablishContextResponse")) {
	    EstablishContextResponse establishContextResponse = new EstablishContextResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			establishContextResponse.setResult(r);
		    } else if (parser.getName().equals("ContextHandle")) {
			establishContextResponse.setContextHandle(Helper.convStringToByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishContextResponse")));
	    return establishContextResponse;

	} else if (parser.getName().equals("ListIFDsResponse")) {
	    ListIFDsResponse listIFDsResponse = new ListIFDsResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			listIFDsResponse.setResult(r);
		    } else if (parser.getName().equals("IFDName")) {
			listIFDsResponse.getIFDName().add(parser.nextText());
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ListIFDsResponse")));
	    return listIFDsResponse;

	} else if (parser.getName().equals("ConnectResponse")) {
	    ConnectResponse connectResponse = new ConnectResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			connectResponse.setResult(r);
		    } else if (parser.getName().equals("SlotHandle")) {
			connectResponse.setSlotHandle(Helper.convStringToByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ConnectResponse")));
	    return connectResponse;

	} else if (parser.getName().equals("Connect")) {
	    Connect c = new Connect();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("IFDName")) {
			c.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("ContextHandle")) {
			c.setContextHandle(Helper.convStringToByteArray(parser.nextText()));
		    } else if (parser.getName().equals("Slot")) {
			c.setSlot(new BigInteger(parser.nextText()));
		    } // TODO exclusive
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Connect")));
	    return c;

	} else if (parser.getName().equals("Transmit")) {
	    Transmit t = new Transmit();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("InputAPDU")) {
			InputAPDUInfoType iait = new InputAPDUInfoType();
			iait.setInputAPDU(Helper.convStringToByteArray(parser.nextText()));
			t.getInputAPDUInfo().add(iait);
		    } else if (parser.getName().equals("SlotHandle")) {
			t.setSlotHandle(Helper.convStringToByteArray(parser.nextText()));
		    } // TODO acceptablestatuscode
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Transmit")));
	    return t;

	} else if (parser.getName().equals("TransmitResponse")) {
	    TransmitResponse transmitResponse = new TransmitResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ResultMajor")) {
			Result r = new Result();
			r.setResultMajor(parser.nextText());
			transmitResponse.setResult(r);
		    } else if (parser.getName().equals("OutputAPDU")) {
			transmitResponse.getOutputAPDU().add(Helper.convStringToByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("TransmitResponse")));
	    return transmitResponse;
	} else {
	    return null;
	}
    }

    private IFDStatusType parseIFDStatusType(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
	IFDStatusType ifdStatusType = new IFDStatusType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("IFDName")) {
		    ifdStatusType.setIFDName(parser.nextText());
		} else if (parser.getName().equals("Connected")) {
		    ifdStatusType.setConnected(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("ActiveAntenna")){
		    ifdStatusType.setActiveAntenna(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("SlotStatus")){
		    ifdStatusType.getSlotStatus().add(parseSlotStatusType(parser));
		} else if (parser.getName().equals("DisplayStatus")){
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "DisplayStatus"));
		} else if (parser.getName().equals("KeyPadStatus")){
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "KeyPadStatus"));
		} else if (parser.getName().equals("BioSensorStatus")){
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "BioSensorStatus"));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));

	return ifdStatusType;
    }

    private SlotStatusType parseSlotStatusType(XmlPullParser parser) throws XmlPullParserException, IOException {
	SlotStatusType slotStatusType = new SlotStatusType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    slotStatusType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("CardAvailable")) {
		    slotStatusType.setCardAvailable(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("ATRorATS")) {
		    slotStatusType.setATRorATS(Helper.convStringToByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotStatus")));

	return slotStatusType;
    }

    private SimpleFUStatusType parseSimpleFUStatusType(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
	SimpleFUStatusType simpleFUStatusType = new SimpleFUStatusType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    simpleFUStatusType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Available")) {
		    simpleFUStatusType.setAvailable(Boolean.valueOf(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));

	return simpleFUStatusType;
    }

    @Override
    public SOAPMessage doc2soap(Document envDoc) {
	// TODO
	return null;
    }

    @Override
    public SOAPMessage add2soap(Document content) {
	// TODO
	return null;
    }

}
