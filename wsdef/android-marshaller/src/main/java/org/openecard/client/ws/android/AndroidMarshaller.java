/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.ws.android;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import iso.std.iso_iec._24727.tech.schema.*;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType.And;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType.Or;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.ws.*;
import org.openecard.client.ws.soap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.*;

/**
 * This class is a provisional and simple replacement for the JAXB-Marshaller
 * used in the applet and the rich client since JAXB is not available on
 * Android.</br>
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMarshaller implements WSMarshaller {

    private static final Logger _logger = LoggerFactory.getLogger(AndroidMarshaller.class);

    private static final String iso = "iso:";
    private static final String tls = "tls:";
    private static final String dss = "dss:";
    private static final String ecapi = "ecapi:"; // xmlns:ecapi="http://www.bsi.bund.de/ecard/api/1.1"

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Transformer transformer;
    private MessageFactory soapFactory;

    public AndroidMarshaller() {
	documentBuilderFactory = null;
	documentBuilder = null;
	transformer = null;
	soapFactory = null;
	try {
	    documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilderFactory.setNamespaceAware(true);
	    documentBuilderFactory.setIgnoringComments(true);
	    documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
	    // "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	    soapFactory = MessageFactory.newInstance();
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
    public synchronized Document marshal(Object o) throws MarshallingTypeException {
	Document document = documentBuilder.newDocument();
	document.setXmlStandalone(true);

	Element rootElement = null;

	if (o instanceof DestroyChannel) {
	    DestroyChannel destroyChannel = (DestroyChannel) o;
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    Element emSlotHandle = document.createElement(iso + "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(destroyChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	} else if (o instanceof EstablishChannel) {
	    EstablishChannel establishChannel = (EstablishChannel) o;
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");

	    Element emSlotHandle = document.createElement(iso + "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(establishChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	    Element emAuthProtData = document.createElement(iso + "AuthenticationProtocolData");
	    emAuthProtData.setAttribute("Protocol", establishChannel.getAuthenticationProtocolData().getProtocol());

	    for (Element e : establishChannel.getAuthenticationProtocolData().getAny()) {
		Element eClone = document.createElement(iso + e.getLocalName());
		eClone.setTextContent(e.getTextContent());
		eClone.setAttribute("xmlns", "urn:iso:std:iso-iec:24727:tech:schema");
		emAuthProtData.appendChild(eClone);

	    }

	    rootElement.appendChild(emAuthProtData);
	} else if (o instanceof DIDAuthenticateResponse) {
	    DIDAuthenticateResponse didAuthenticateResponse = (DIDAuthenticateResponse) o;
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    rootElement.appendChild(marshalResult(didAuthenticateResponse.getResult(), document));
	    if (didAuthenticateResponse.getAuthenticationProtocolData() != null) {
		DIDAuthenticationDataType didAuthenticationDataType = didAuthenticateResponse.getAuthenticationProtocolData();

		Element elemEACOutput = document.createElement(iso + "AuthenticationProtocolData");
		elemEACOutput.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		if (didAuthenticationDataType instanceof EAC1OutputType) {
		    elemEACOutput.setAttribute("xsi:type", "iso:EAC1OutputType");
		} else if (didAuthenticationDataType instanceof EAC2OutputType) {
		    elemEACOutput.setAttribute("xsi:type", "iso:EAC2OutputType");
		} else
		    throw new MarshallingTypeException("Marshalling a DIDAuthenticationDataType of "
			    + didAuthenticationDataType.getClass().getName() + " in DIDAuthentication is not supported");

		for (Element e : didAuthenticationDataType.getAny()) {
		    Element elemCopy = document.createElement(iso + e.getLocalName());
		    elemCopy.setTextContent(e.getTextContent());
		    elemEACOutput.appendChild(elemCopy);
		}

		rootElement.appendChild(elemEACOutput);

	    } // else only the result (with error) is returned
	} else if (o instanceof InitializeFrameworkResponse) {
	    InitializeFrameworkResponse initializeFrameworkResponse = (InitializeFrameworkResponse) o;
	    rootElement = document.createElement(ecapi + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:ecapi", "http://www.bsi.bund.de/ecard/api/1.1");
	    rootElement.appendChild(marshalResult(initializeFrameworkResponse.getResult(), document));
	    Element emVersion = document.createElement(ecapi + "Version");
	    Element emMajor = document.createElement(ecapi + "Major");
	    emMajor.appendChild(document.createTextNode(initializeFrameworkResponse.getVersion().getMajor().toString()));
	    emVersion.appendChild(emMajor);
	    Element emMinor = document.createElement(ecapi + "Minor");
	    emMinor.appendChild(document.createTextNode(initializeFrameworkResponse.getVersion().getMinor().toString()));
	    emVersion.appendChild(emMinor);
	    Element emSubMinor = document.createElement(ecapi + "SubMinor");
	    emSubMinor.appendChild(document.createTextNode(initializeFrameworkResponse.getVersion().getSubMinor().toString()));
	    emVersion.appendChild(emSubMinor);
	    rootElement.appendChild(emVersion);
	} else if (o instanceof InternationalStringType) {
	    InternationalStringType internationalStringType = (InternationalStringType) o;
	    rootElement = marshalInternationStringType(internationalStringType, document, internationalStringType.getClass()
		    .getSimpleName());
	} else if (o instanceof Result) {
	    Result r = (Result) o;
	    rootElement = marshalResult(r, document);
	} else if (o instanceof iso.std.iso_iec._24727.tech.schema.StartPAOS) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    StartPAOS startPAOS = (StartPAOS) o;

	    Element em = document.createElement(iso + "SessionIdentifier");
	    em.appendChild(document.createTextNode(startPAOS.getSessionIdentifier()));
	    rootElement.appendChild(em);

	    em = document.createElement(iso + "ConnectionHandle");
	    Element em2 = document.createElement(iso + "ContextHandle");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(startPAOS.getConnectionHandle().get(0).getContextHandle())));
	    em.appendChild(em2);
	    if (startPAOS.getConnectionHandle().get(0).getSlotHandle() != null) {
		em2 = document.createElement(iso + "SlotHandle");
		em2.appendChild(document.createTextNode(ByteUtils.toHexString(startPAOS.getConnectionHandle().get(0).getSlotHandle())));
		em.appendChild(em2);
	    }
	    if (startPAOS.getConnectionHandle().get(0).getCardApplication() != null) {
		em2 = document.createElement(iso + "CardApplication");
		em2.appendChild(document.createTextNode(ByteUtils.toHexString(startPAOS.getConnectionHandle().get(0).getCardApplication())));
		em.appendChild(em2);
	    }
	    if (startPAOS.getConnectionHandle().get(0).getSlotIndex() != null) {
		em2 = document.createElement(iso + "SlotIndex");
		em2.appendChild(document.createTextNode(startPAOS.getConnectionHandle().get(0).getSlotIndex().toString()));
		em.appendChild(em2);
	    }
	    if (startPAOS.getConnectionHandle().get(0).getIFDName() != null) {
		em2 = document.createElement(iso + "IFDName");
		em2.appendChild(document.createTextNode(startPAOS.getConnectionHandle().get(0).getIFDName()));
		em.appendChild(em2);
	    }
	    if (startPAOS.getConnectionHandle().get(0).getChannelHandle() != null) {
		em2 = document.createElement(iso + "ChannelHandle");
		if (startPAOS.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier() != null) {
		    Element em3 = document.createElement(iso + "SessionIdentifier");
		    em3.appendChild(document.createTextNode(startPAOS.getConnectionHandle().get(0).getChannelHandle()
			    .getSessionIdentifier()));
		    em2.appendChild(em3);
		}
		em.appendChild(em2);
	    }
	    if (startPAOS.getConnectionHandle().get(0).getRecognitionInfo() != null) {
		em2 = document.createElement(iso + "RecognitionInfo");
		Element em3 = document.createElement(iso + "CardType");
		em3.appendChild(document.createTextNode(startPAOS.getConnectionHandle().get(0).getRecognitionInfo().getCardType()));
		em2.appendChild(em3);
		em.appendChild(em2);
	    }
	    rootElement.appendChild(em);

	} else if (o instanceof TransmitResponse) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    TransmitResponse transmitResponsePOJO = (TransmitResponse) o;

	    Element em = marshalResult(transmitResponsePOJO.getResult(), document);
	    rootElement.appendChild(em);

	    for (int i = 0; i < transmitResponsePOJO.getOutputAPDU().size(); i++) {
		em = document.createElement(iso + "OutputAPDU");
		em.appendChild(document.createTextNode(ByteUtils.toHexString(transmitResponsePOJO.getOutputAPDU().get(i))));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof EstablishContext) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	} else if (o instanceof EstablishContextResponse) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    EstablishContextResponse establishContextResponse = (EstablishContextResponse) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(establishContextResponse.getContextHandle())));
	    rootElement.appendChild(em);

	    em = marshalResult(establishContextResponse.getResult(), document);
	    rootElement.appendChild(em);

	} else if (o instanceof GetStatus) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    GetStatus getStatus = (GetStatus) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(getStatus.getContextHandle())));
	    rootElement.appendChild(em);
	    if (getStatus.getIFDName() != null) {
		em = document.createElement(iso + "IFDName");
		em.appendChild(document.createTextNode(getStatus.getIFDName()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Wait) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    Wait w = (Wait) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(w.getContextHandle())));
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
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    Connect c = (Connect) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(c.getContextHandle())));
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
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    ConnectResponse cr = (ConnectResponse) o;

	    Element em = document.createElement(iso + "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(cr.getSlotHandle())));
	    rootElement.appendChild(em);

	    em = marshalResult(cr.getResult(), document);
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDs) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    ListIFDs c = (ListIFDs) o;

	    Element em = document.createElement(iso + "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(c.getContextHandle())));
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDsResponse) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    ListIFDsResponse listIFDsResponse = (ListIFDsResponse) o;

	    for (String s : listIFDsResponse.getIFDName()) {
		Element em = document.createElement(iso + "IFDName");
		em.appendChild(document.createTextNode(s));
		rootElement.appendChild(em);
	    }

	    Element em = marshalResult(listIFDsResponse.getResult(), document);
	    rootElement.appendChild(em);

	} else if (o instanceof Transmit) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    Transmit t = (Transmit) o;

	    Element em = document.createElement(iso + "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(t.getSlotHandle())));
	    rootElement.appendChild(em);

	    for (int i = 0; i < t.getInputAPDUInfo().size(); i++) {
		em = document.createElement(iso + "InputAPDUInfo");
		rootElement.appendChild(em);
		Element em2 = document.createElement(iso + "InputAPDU");
		em2.appendChild(document.createTextNode(ByteUtils.toHexString(t.getInputAPDUInfo().get(i).getInputAPDU())));
		em.appendChild(em2);
		for (int y = 0; y < t.getInputAPDUInfo().get(i).getAcceptableStatusCode().size(); y++) {
		    em2 = document.createElement(iso + "AcceptableStatusCode");
		    em2.appendChild(document.createTextNode(ByteUtils.toHexString(t.getInputAPDUInfo().get(i).getAcceptableStatusCode()
			    .get(y))));
		    em.appendChild(em2);
		}
	    }

	} else if (o instanceof RecognitionTree) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    rootElement.setAttribute("xmlns:tls", "http://ws.openecard.org/protocols/tls/v1.0");
	    RecognitionTree recognitionTree = (RecognitionTree) o;
	    for (CardCall c : recognitionTree.getCardCall()) {
		rootElement.appendChild(marshalCardCall(c, document));
	    }

	} else if (o instanceof DisconnectResponse) {
	    rootElement = document.createElement(iso + o.getClass().getSimpleName());
	    rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
	    DisconnectResponse disconnectResponse = (DisconnectResponse) o;
	    Element em = marshalResult(disconnectResponse.getResult(), document);
	    rootElement.appendChild(em);
	} else {
	    throw new IllegalArgumentException("Cannot marshal " + o.getClass().getSimpleName());
	}
	document.appendChild(rootElement);
	return document;
    }

    private Element marshalInternationStringType(InternationalStringType internationalStringType, Document document, String name) {
	Element emInternationStringType = document.createElement(dss + name);
	emInternationStringType.setAttribute("xmlns:dss", "urn:oasis:names:tc:dss:1.0:core:schema");

	Element em = document.createElement(dss + "ResultMessage");
	em.appendChild(document.createTextNode(internationalStringType.getValue()));
	em.setAttribute("xml:lang", internationalStringType.getLang());

	emInternationStringType.appendChild(em);
	return emInternationStringType;
    }

    private synchronized Element marshalResult(Result r, Document document) {
	Element emResult = document.createElement(dss + r.getClass().getSimpleName());
	emResult.setAttribute("xmlns:dss", "urn:oasis:names:tc:dss:1.0:core:schema");
	Element em = document.createElement(dss + "ResultMajor");
	em.appendChild(document.createTextNode(r.getResultMajor()));
	emResult.appendChild(em);
	if (r.getResultMinor() != null) {
	    em = document.createElement(dss + "ResultMinor");
	    em.appendChild(document.createTextNode(r.getResultMinor()));
	    emResult.appendChild(em);
	}
	if (r.getResultMessage() != null) {
	    emResult.appendChild(marshalInternationStringType(r.getResultMessage(), document, "ResultMessage"));
	}
	return emResult;
    }

    private synchronized Node marshalCardCall(CardCall c, Document document) {
	Element emCardCall = document.createElement(iso + "CardCall");
	if (c.getCommandAPDU() != null) {
	    Element emCommandAPDU = document.createElement(iso + "CommandAPDU");
	    emCommandAPDU.appendChild(document.createTextNode(ByteUtils.toHexString(c.getCommandAPDU())));
	    emCardCall.appendChild(emCommandAPDU);
	}
	if (c.getResponseAPDU() != null && c.getResponseAPDU().size() > 0) {
	    for (ResponseAPDUType r : c.getResponseAPDU()) {
		Element emResponseAPDU = document.createElement(iso + "ResponseAPDU");

		if (r.getBody() != null) {

		    Element emBody = document.createElement(iso + "Body");
		    if (r.getBody().getTag() != null) {
			Element emTag = document.createElement(iso + "Tag");
			emTag.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getTag())));
			emBody.appendChild(emTag);
		    }
		    if (r.getBody().getMatchingData() != null) {
			Element emMatchingData = document.createElement(iso + "MatchingData");
			if (r.getBody().getMatchingData().getLength() != null) {
			    Element emLength = document.createElement(iso + "Length");
			    emLength.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getLength())));
			    emMatchingData.appendChild(emLength);
			}
			if (r.getBody().getMatchingData().getOffset() != null) {
			    Element emOffset = document.createElement(iso + "Offset");
			    emOffset.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getOffset())));
			    emMatchingData.appendChild(emOffset);
			}
			if (r.getBody().getMatchingData().getMask() != null) {
			    Element emMask = document.createElement(iso + "Mask");
			    emMask.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getMask())));
			    emMatchingData.appendChild(emMask);
			}
			if (r.getBody().getMatchingData().getMatchingValue() != null) {
			    Element emMatchingValue = document.createElement(iso + "MatchingValue");
			    emMatchingValue.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData()
				    .getMatchingValue())));
			    emMatchingData.appendChild(emMatchingValue);
			}
			emBody.appendChild(emMatchingData);
		    }
		    emResponseAPDU.appendChild(emBody);
		}
		if (r.getTrailer() != null) {
		    Element emTrailer = document.createElement(iso + "Trailer");
		    emTrailer.appendChild(document.createTextNode(ByteUtils.toHexString(r.getTrailer())));
		    emResponseAPDU.appendChild(emTrailer);
		}
		if (r.getConclusion() != null) {
		    Element emConclusion = document.createElement(iso + "Conclusion");
		    if (r.getConclusion().getCardCall() != null) {
			for (CardCall cc : r.getConclusion().getCardCall()) {
			    emConclusion.appendChild(marshalCardCall(cc, document));
			}
		    }
		    if (r.getConclusion().getRecognizedCardType() != null) {
			Element emRecognizedCardType = document.createElement(iso + "RecognizedCardType");
			emRecognizedCardType.appendChild(document.createTextNode(r.getConclusion().getRecognizedCardType()));
			emConclusion.appendChild(emRecognizedCardType);
		    }

		    emResponseAPDU.appendChild(emConclusion);
		}
		emCardCall.appendChild(emResponseAPDU);
	    }
	}
	return emCardCall;
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
	    parser.setInput(new ByteArrayInputStream(this.doc2str(newDoc).getBytes("UTF-8")), "UTF-8");
	    int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT) {
		if (eventType == XmlPullParser.START_TAG) {
		    Object obj = parse(parser);
		    return obj;
		}
		eventType = parser.next();
	    }
	    return null;
	} catch (Exception e) {
	    throw new MarshallingTypeException(e);
	}
    }

    private synchronized ResponseAPDUType parseResponseAPDUType(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException {
	ResponseAPDUType responseAPDUType = new ResponseAPDUType();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Trailer")) {
		    responseAPDUType.setTrailer(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("Body")) {
		    responseAPDUType.setBody(this.parseDataMaskType(parser));
		} else if (parser.getName().equals("Conclusion")) {
		    responseAPDUType.setConclusion(this.parseConclusion(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ResponseAPDU")));
	return responseAPDUType;
    }

    private synchronized Conclusion parseConclusion(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException {
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
		    dataMaskType.setTag(StringUtils.toByteArray(parser.nextText()));
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
		    matchingDataType.setOffset(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("Length")) {
		    matchingDataType.setLength(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingValue")) {
		    matchingDataType.setMatchingValue(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("MatchingData")));

	return matchingDataType;
    }

    private synchronized CardCall parseCardCall(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException {
	CardCall c = new CardCall();

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CommandAPDU")) {
		    c.setCommandAPDU(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("ResponseAPDU")) {
		    c.getResponseAPDU().add(this.parseResponseAPDUType(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardCall")));
	return c;
    }

    private synchronized Object parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParserConfigurationException {
	if (parser.getName().equals("DestroyChannelResponse")) {
	    DestroyChannelResponse destroyChannelResponse = new DestroyChannelResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			destroyChannelResponse.setResult(this.parseResult(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DestroyChannelResponse")));
	    return destroyChannelResponse;
	}

	else if (parser.getName().equals("EstablishChannelResponse")) {
	    EstablishChannelResponse establishChannelResponse = new EstablishChannelResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			establishChannelResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			establishChannelResponse.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishChannelResponse")));
	    return establishChannelResponse;
	}

	else if (parser.getName().equals("DIDAuthenticate")) {
	    DIDAuthenticate didAuthenticate = new DIDAuthenticate();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("DIDName")) {
			didAuthenticate.setDIDName(parser.nextText());
		    } else if (parser.getName().equals("SlotHandle")) {
			ConnectionHandleType cht = new ConnectionHandleType();
			cht.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
			didAuthenticate.setConnectionHandle(cht);
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			didAuthenticate.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }

		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthenticate")));
	    return didAuthenticate;
	}

	else if (parser.getName().equals("StartPAOSResponse")) {
	    StartPAOSResponse startPAOSResponse = new StartPAOSResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			startPAOSResponse.setResult(this.parseResult(parser));
		    }

		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("StartPAOSResponse")));
	    return startPAOSResponse;
	} else if (parser.getName().equals("InitializeFramework")) {
	    InitializeFramework initializeFramework = new InitializeFramework();
	    return initializeFramework;
	} else if (parser.getName().equals("Conclusion")) {
	    return parseConclusion(parser);
	} else if (parser.getName().equals("WaitResponse")) {
	    WaitResponse waitResponse = new WaitResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			waitResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("IFDEvent")) {
			waitResponse.getIFDEvent().add(parseIFDStatusType(parser, "IFDEvent"));
		    } else if (parser.getName().equals("SessionIdentifier")) {
			waitResponse.setSessionIdentifier(parser.nextText());
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("WaitResponse")));
	    return waitResponse;

	} else if (parser.getName().equals("GetStatusResponse")) {
	    GetStatusResponse getStatusResponse = new GetStatusResponse();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			getStatusResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("IFDStatus")) {
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
			listIFDs.setContextHandle(StringUtils.toByteArray(parser.nextText()));
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
		    if (parser.getName().equals("Result")) {
			resp.setResult(this.parseResult(parser));
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
		    if (parser.getName().equals("Result")) {
			establishContextResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			establishContextResponse.setContextHandle(StringUtils.toByteArray(parser.nextText()));
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
		    if (parser.getName().equals("Result")) {
			listIFDsResponse.setResult(this.parseResult(parser));
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
		    if (parser.getName().equals("Result")) {
			connectResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("SlotHandle")) {
			connectResponse.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
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
			c.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("Slot")) {
			c.setSlot(new BigInteger(parser.nextText()));
		    } // TODO exclusive
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Connect")));
	    return c;
	} else if (parser.getName().equals("Disconnect")) {
	    Disconnect d = new Disconnect();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			d.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("Action")) {
			d.setAction(ActionType.fromValue(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Disconnect")));
	    return d;

	} else if (parser.getName().equals("Transmit")) {
	    Transmit t = new Transmit();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("InputAPDUInfo")) {
			t.getInputAPDUInfo().add(this.parseInputAPDUInfo(parser));
		    } else if (parser.getName().equals("SlotHandle")) {
			t.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
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
		    if (parser.getName().equals("Result")) {
			transmitResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("OutputAPDU")) {
			transmitResponse.getOutputAPDU().add(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("TransmitResponse")));
	    return transmitResponse;
	} else if (parser.getName().equals("CardInfo")) {
	    CardInfo cardInfo = new CardInfo();
	    ApplicationCapabilitiesType applicationCapabilities = new ApplicationCapabilitiesType();
	    int eventType = parser.getEventType();
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ObjectIdentifier")) {
			CardTypeType cardType = new CardTypeType();
			cardType.setObjectIdentifier(parser.nextText());
			cardInfo.setCardType(cardType);
		    } else if (parser.getName().equals("ImplicitlySelectedApplication")) {
			applicationCapabilities.setImplicitlySelectedApplication(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			applicationCapabilities.getCardApplication().add(this.parseCardApplication(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardInfo")));
	    cardInfo.setApplicationCapabilities(applicationCapabilities);
	    return cardInfo;
	} else {
	    return null;
	}
    }

    private CardApplicationType parseCardApplication(XmlPullParser parser) throws XmlPullParserException, IOException {
	CardApplicationType cardApplication = new CardApplicationType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ApplicationIdentifier")) {
		    cardApplication.setApplicationIdentifier(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("ApplicationName")) {
		    cardApplication.setApplicationName(parser.nextText());
		} else if (parser.getName().equals("RequirementLevel")) {
		    cardApplication.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CardApplicationACL")) {
		    cardApplication.setCardApplicationACL(this.parseACL(parser, "CardApplicationACL"));
		} else if (parser.getName().equals("DIDInfo")) {
		    cardApplication.getDIDInfo().add(this.parseDIDInfo(parser));
		} else if (parser.getName().equals("DataSetInfo")) {
		    cardApplication.getDataSetInfo().add(this.parseDataSetInfo(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplication")));
	return cardApplication;
    }

    private DataSetInfoType parseDataSetInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DataSetInfoType dataSetInfo = new DataSetInfoType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RequirementLevel")) {
		    dataSetInfo.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DataSetACL")) {
		    dataSetInfo.setDataSetACL(this.parseACL(parser, "DataSetACL"));
		} else if (parser.getName().equals("DataSetName")) {
		    dataSetInfo.setDataSetName(parser.nextText());
		} else if (parser.getName().equals("DataSetPath")) {
		    dataSetInfo.setDataSetPath(this.parseDataSetPath(parser));
		} else if (parser.getName().equals("LocalDataSetName")) {
		    InternationalStringType internationalString = new InternationalStringType();
		    internationalString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    internationalString.setValue(parser.nextText());
		    dataSetInfo.getLocalDataSetName().add(internationalString);
		} else
		    throw new IOException(parser.getName() + " not yet implemented");

	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetInfo")));
	return dataSetInfo;
    }

    private PathType parseDataSetPath(XmlPullParser parser) throws XmlPullParserException, IOException {
	PathType path = new PathType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("efIdOrPath")) {
		    path.setEfIdOrPath(StringUtils.toByteArray(parser.nextText()));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetPath")));
	return path;
    }

    private DIDInfoType parseDIDInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDInfoType didInfo = new DIDInfoType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RequirementLevel")) {
		    didInfo.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DIDACL")) {
		    didInfo.setDIDACL(this.parseACL(parser, "DIDACL"));
		} else if (parser.getName().equals("DifferentialIdentity")) {
		    didInfo.setDifferentialIdentity(this.parseDifferentialIdentity(parser));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDInfo")));
	return didInfo;
    }

    private DifferentialIdentityType parseDifferentialIdentity(XmlPullParser parser) throws XmlPullParserException, IOException {
	DifferentialIdentityType differentialIdentity = new DifferentialIdentityType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("DIDName")) {
		    differentialIdentity.setDIDName(parser.nextText());
		} else if (parser.getName().equals("LocalDIDName")) {
		    InternationalStringType internationalString = new InternationalStringType();
		    internationalString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    internationalString.setValue(parser.nextText());
		    differentialIdentity.getLocalDIDName().add(internationalString);
		} else if (parser.getName().equals("DIDProtocol")) {
		    differentialIdentity.setDIDProtocol(parser.nextText());
		} else if (parser.getName().equals("DIDMarker")) {
		    differentialIdentity.setDIDMarker(this.parseDIDMarkerType(parser));
		} else if (parser.getName().equals("DIDScope")) {
		    differentialIdentity.setDIDScope(DIDScopeType.fromValue(parser.nextText()));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DifferentialIdentity")));
	return differentialIdentity;
    }

    private DIDMarkerType parseDIDMarkerType(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDMarkerType didMarker = new DIDMarkerType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("PACEMarker")) {
		    didMarker.setPACEMarker(this.parsePACEMarker(parser));
		} else if (parser.getName().equals("TAMarker")) {
		    didMarker.setTAMarker(this.parseTAMarker(parser));
		} else if (parser.getName().equals("CAMarker")) {
		    didMarker.setCAMarker(this.parseCAMarker(parser));
		} else if (parser.getName().equals("RIMarker")) {
		    didMarker.setRIMarker(this.parseRIMarker(parser));
		} else if (parser.getName().equals("CryptoMarker")) {
		    didMarker.setCryptoMarker(this.parseCryptoMarker(parser));
		} else if (parser.getName().equals("PinCompareMarker")) {
		    didMarker.setPinCompareMarker(this.parsePINCompareMarker(parser));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDMarker")));
	return didMarker;
    }

    private PinCompareMarkerType parsePINCompareMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	PinCompareMarkerType pinCompareMarker = new PinCompareMarkerType();
	pinCompareMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	pinCompareMarker.getAny().addAll(parseAnyTypes(parser, "PinCompareMarker", parser.getNamespace(), d, false));
	return pinCompareMarker;
    }

    private Collection<? extends Element> parseAnyTypes(XmlPullParser parser, String name, String ns, Document d, Boolean firstCall)
	    throws XmlPullParserException, IOException {
	int eventType = parser.getEventType();
	List<Element> elements = new ArrayList<Element>();
	boolean terminalNode = false;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		elements.addAll(parseAnyTypes(parser, parser.getName(), parser.getNamespace(), d, true));
	    } else if (eventType == XmlPullParser.TEXT) {
		if (parser.getText().trim().length() > 0) {
		    Element em = d.createElementNS(ns, name);
		    em.setTextContent(parser.getText());
		    elements.add(em);
		    terminalNode = true;
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));
	if (!terminalNode && firstCall) {
	    Element test = d.createElementNS(ns, name);
	    for (Element e : elements)
		test.appendChild(e);
	    List<Element> elements2 = new ArrayList<Element>();
	    elements2.add(test);
	    return elements2;
	}
	return elements;
    }

    private CryptoMarkerType parseCryptoMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	CryptoMarkerType cryptoMarker = new CryptoMarkerType();
	cryptoMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	cryptoMarker.getAny().addAll(parseAnyTypes(parser, "CryptoMarker", parser.getNamespace(), d, false));
	return cryptoMarker;
    }

    private RIMarkerType parseRIMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	RIMarkerType riMarker = new RIMarkerType();
	riMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	riMarker.getAny().addAll(parseAnyTypes(parser, "RIMarker", parser.getNamespace(), d, false));
	return riMarker;
    }

    private CAMarkerType parseCAMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	CAMarkerType caMarker = new CAMarkerType();
	caMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	caMarker.getAny().addAll(parseAnyTypes(parser, "CAMarker", parser.getNamespace(), d, false));
	return caMarker;
    }

    private TAMarkerType parseTAMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	TAMarkerType taMarker = new TAMarkerType();
	taMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	taMarker.getAny().addAll(parseAnyTypes(parser, "TAMarker", parser.getNamespace(), d, false));
	return taMarker;
    }

    private PACEMarkerType parsePACEMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
	PACEMarkerType paceMarker = new PACEMarkerType();
	paceMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	Document d = documentBuilder.newDocument();
	paceMarker.getAny().addAll(parseAnyTypes(parser, "PACEMarker", parser.getNamespace(), d, false));
	return paceMarker;
    }

    private AccessControlListType parseACL(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
	AccessControlListType accessControlList = new AccessControlListType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AccessRule")) {
		    accessControlList.getAccessRule().add(this.parseAccessRule(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(endTag)));
	return accessControlList;
    }

    private AccessRuleType parseAccessRule(XmlPullParser parser) throws XmlPullParserException, IOException {
	AccessRuleType accessRule = new AccessRuleType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CardApplicationServiceName")) {
		    accessRule.setCardApplicationServiceName(parser.nextText());
		} else if (parser.getName().equals("Action")) {
		    accessRule.setAction(this.parseAction(parser));
		} else if (parser.getName().equals("SecurityCondition")) {
		    accessRule.setSecurityCondition(this.parseSecurityCondition(parser));
		} else
		    throw new IOException("not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AccessRule")));
	return accessRule;
    }

    private SecurityConditionType parseSecurityCondition(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType securityCondition = new SecurityConditionType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("always")) {
		    securityCondition.setAlways(true);
		} else if (parser.getName().equals("never")) {
		    securityCondition.setNever(false);
		} else if (parser.getName().equals("DIDAuthentication")) {
		    securityCondition.setDIDAuthentication(this.parseDIDAuthenticationState(parser));
		} else if (parser.getName().equals("not")) {
		    securityCondition.setNot(this.parseSecurityCondition(parser));
		} else if (parser.getName().equals("and")) {
		    securityCondition.setAnd(this.parseSecurityConditionTypeAnd(parser));
		} else if (parser.getName().equals("or")) {
		    securityCondition.setOr(this.parseSecurityConditionTypeOr(parser));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("SecurityCondition")));
	return securityCondition;
    }

    private DIDAuthenticationStateType parseDIDAuthenticationState(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDAuthenticationStateType didAuthenticationState = new DIDAuthenticationStateType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("DIDName")) {
		    didAuthenticationState.setDIDName(parser.nextText());
		} else if (parser.getName().equals("DIDScope")) {
		    didAuthenticationState.setDIDScope(DIDScopeType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DIDState")) {
		    didAuthenticationState.setDIDState(Boolean.parseBoolean(parser.nextText()));
		} else if (parser.getName().equals("DIDStateQualifier")) {
		    didAuthenticationState.setDIDStateQualifier(StringUtils.toByteArray(parser.nextText()));
		} else
		    throw new IOException(parser.getName() + " not yet implemented");
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthentication")));
	return didAuthenticationState;
    }

    private Or parseSecurityConditionTypeOr(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType.Or securityConditionOr = new SecurityConditionType.Or();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("SecurityCondition")) {
		    securityConditionOr.getSecurityCondition().add(this.parseSecurityCondition(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("or")));
	return securityConditionOr;
    }

    private And parseSecurityConditionTypeAnd(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType.And securityConditionAnd = new SecurityConditionType.And();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("SecurityCondition")) {
		    securityConditionAnd.getSecurityCondition().add(this.parseSecurityCondition(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("and")));
	return securityConditionAnd;
    }

    private ActionNameType parseAction(XmlPullParser parser) throws XmlPullParserException, IOException {
	ActionNameType action = new ActionNameType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("APIAccessEntryPoint")) {
		    action.setAPIAccessEntryPoint(APIAccessEntryPointName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("ConnectionServiceAction")) {
		    action.setConnectionServiceAction(ConnectionServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CardApplicationServiceAction")) {
		    action.setCardApplicationServiceAction(CardApplicationServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("NamedDataServiceAction")) {
		    action.setNamedDataServiceAction(NamedDataServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CryptographicServiceAction")) {
		    action.setCryptographicServiceAction(CryptographicServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DifferentialIdentityServiceAction")) {
		    action.setDifferentialIdentityServiceAction(DifferentialIdentityServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("AuthorizationServiceAction")) {
		    action.setAuthorizationServiceAction(AuthorizationServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("LoadedAction")) {
		    action.setLoadedAction(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Action")));
	return action;
    }

    private InputAPDUInfoType parseInputAPDUInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	InputAPDUInfoType inputAPDUInfo = new InputAPDUInfoType();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("InputAPDU")) {
		    inputAPDUInfo.setInputAPDU(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("AcceptableStatusCode")) {
		    inputAPDUInfo.getAcceptableStatusCode().add(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("InputAPDUInfo")));
	return inputAPDUInfo;
    }

    private DIDAuthenticationDataType parseDIDAuthenticationDataType(XmlPullParser parser) throws XmlPullParserException, IOException {
	Document document = documentBuilder.newDocument();
	DIDAuthenticationDataType didAuthenticationDataType = null;
	if (parser.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type").contains("EAC1InputType")) {
	    didAuthenticationDataType = new EAC1InputType();
	} else if (parser.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type").contains("EAC2InputType")) {
	    didAuthenticationDataType = new EAC2InputType();
	} else if (parser.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type").contains("EACAdditionalInputType")) {
	    didAuthenticationDataType = new EACAdditionalInputType();
	} else {
	    didAuthenticationDataType = new DIDAuthenticationDataType();
	}

	if (parser.getAttributeValue(null, "Protocol") != null && !parser.getAttributeValue(null, "Protocol").isEmpty())
	    didAuthenticationDataType.setProtocol(parser.getAttributeValue(null, "Protocol"));

	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		Element em = document.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", parser.getName());
		em.setTextContent(parser.nextText());
		didAuthenticationDataType.getAny().add(em);
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AuthenticationProtocolData")));

	return didAuthenticationDataType;
    }

    private Result parseResult(XmlPullParser parser) throws XmlPullParserException, IOException {
	Result r = new Result();
	int eventType = parser.getEventType();
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ResultMajor")) {
		    r.setResultMajor(parser.nextText());
		} else if (parser.getName().equals("ResultMinor")) {
		    r.setResultMinor(parser.nextText());
		} else if (parser.getName().equals("ResultMessage")) {
		    InternationalStringType internationalStringType = new InternationalStringType();

		    internationalStringType.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));

		    internationalStringType.setValue(parser.nextText());

		    r.setResultMessage(internationalStringType);
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Result")));
	return r;
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
		} else if (parser.getName().equals("ActiveAntenna")) {
		    ifdStatusType.setActiveAntenna(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("SlotStatus")) {
		    ifdStatusType.getSlotStatus().add(parseSlotStatusType(parser));
		} else if (parser.getName().equals("DisplayStatus")) {
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "DisplayStatus"));
		} else if (parser.getName().equals("KeyPadStatus")) {
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "KeyPadStatus"));
		} else if (parser.getName().equals("BioSensorStatus")) {
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
		    slotStatusType.setATRorATS(StringUtils.toByteArray(parser.nextText()));
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
    public SOAPMessage doc2soap(Document envDoc) throws SOAPException {
	SOAPMessage msg = soapFactory.createMessage(envDoc);
	return msg;
    }

    @Override
    public SOAPMessage add2soap(Document content) throws SOAPException {
	SOAPMessage msg = soapFactory.createMessage();
	SOAPBody body = msg.getSOAPBody();
	body.addDocument(content);

	return msg;
    }

}
