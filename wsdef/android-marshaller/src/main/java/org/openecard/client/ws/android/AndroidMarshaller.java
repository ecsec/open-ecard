/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
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

package org.openecard.client.ws.android;

import iso.std.iso_iec._24727.tech.schema.CardCall;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Conclusion;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.DataMaskType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.MatchingDataType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType;
import iso.std.iso_iec._24727.tech.schema.SimpleFUStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;

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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;

import org.openecard.client.common.util.Helper;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WhitespaceFilter;
import org.openecard.client.ws.soap.MessageFactory;
import org.openecard.client.ws.soap.SOAPBody;
import org.openecard.client.ws.soap.SOAPException;
import org.openecard.client.ws.soap.SOAPMessage;
import org.openecard.ws.protocols.tls.v1.TLSMarkerType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMarshaller implements WSMarshaller {

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
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setIgnoringComments(true);
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

		if (o instanceof InitializeFrameworkResponse) {
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
			StartPAOS startPAOSPOJO = (StartPAOS) o;

			Element em = document.createElement(iso + "SessionIdentifier");
			em.appendChild(document.createTextNode(startPAOSPOJO.getSessionIdentifier()));
			rootElement.appendChild(em);

			em = document.createElement(iso + "ConnectionHandle");
			Element em2 = document.createElement(iso + "ContextHandle");
			em2.appendChild(document.createTextNode(Helper.convByteArrayToString(startPAOSPOJO.getConnectionHandle().get(0)
					.getContextHandle())));
			em.appendChild(em2);
			em2 = document.createElement(iso + "SlotHandle");
			em2.appendChild(document.createTextNode(Helper
					.convByteArrayToString(startPAOSPOJO.getConnectionHandle().get(0).getSlotHandle())));
			em.appendChild(em2);
			rootElement.appendChild(em);

		} else if (o instanceof TransmitResponse) {
			rootElement = document.createElement(iso + o.getClass().getSimpleName());
			rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
			TransmitResponse transmitResponsePOJO = (TransmitResponse) o;

			Element em = marshalResult(transmitResponsePOJO.getResult(), document);
			rootElement.appendChild(em);

			for (int i = 0; i < transmitResponsePOJO.getOutputAPDU().size(); i++) {
				em = document.createElement(iso + "OutputAPDU");
				em.appendChild(document.createTextNode(Helper.convByteArrayToString(transmitResponsePOJO.getOutputAPDU().get(i))));
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
			em.appendChild(document.createTextNode(Helper.convByteArrayToString(establishContextResponse.getContextHandle())));
			rootElement.appendChild(em);

			em = marshalResult(establishContextResponse.getResult(), document);
			rootElement.appendChild(em);

		} else if (o instanceof GetStatus) {
			rootElement = document.createElement(iso + o.getClass().getSimpleName());
			rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
			GetStatus getStatus = (GetStatus) o;

			Element em = document.createElement(iso + "ContextHandle");
			em.appendChild(document.createTextNode(Helper.convByteArrayToString(getStatus.getContextHandle())));
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
			rootElement = document.createElement(iso + o.getClass().getSimpleName());
			rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
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
			rootElement = document.createElement(iso + o.getClass().getSimpleName());
			rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
			ConnectResponse cr = (ConnectResponse) o;

			Element em = document.createElement(iso + "SlotHandle");
			em.appendChild(document.createTextNode(Helper.convByteArrayToString(cr.getSlotHandle())));
			rootElement.appendChild(em);

			em = marshalResult(cr.getResult(), document);
			rootElement.appendChild(em);

		} else if (o instanceof ListIFDs) {
			rootElement = document.createElement(iso + o.getClass().getSimpleName());
			rootElement.setAttribute("xmlns:iso", "urn:iso:std:iso-iec:24727:tech:schema");
			ListIFDs c = (ListIFDs) o;

			Element em = document.createElement(iso + "ContextHandle");
			em.appendChild(document.createTextNode(Helper.convByteArrayToString(c.getContextHandle())));
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
					em2.appendChild(document.createTextNode(Helper.convByteArrayToString(t.getInputAPDUInfo().get(i)
							.getAcceptableStatusCode().get(y))));
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
		em = document.createElement(dss + "ResultMinor");
		em.appendChild(document.createTextNode(r.getResultMinor()));
		emResult.appendChild(em);
		emResult.appendChild(marshalInternationStringType(r.getResultMessage(), document, "ResultMessage"));
		return emResult;
	}

	private synchronized Node marshalCardCall(CardCall c, Document document) {
		Element emCardCall = document.createElement(iso + "CardCall");
		if (c.getCommandAPDU() != null) {
			Element emCommandAPDU = document.createElement(iso + "CommandAPDU");
			emCommandAPDU.appendChild(document.createTextNode(Helper.convByteArrayToString(c.getCommandAPDU())));
			emCardCall.appendChild(emCommandAPDU);
		}
		if (c.getResponseAPDU() != null && c.getResponseAPDU().size() > 0) {
			for (ResponseAPDUType r : c.getResponseAPDU()) {
				Element emResponseAPDU = document.createElement(iso + "ResponseAPDU");

				if (r.getBody() != null) {

					Element emBody = document.createElement(iso + "Body");
					if (r.getBody().getTag() != null) {
						Element emTag = document.createElement(iso + "Tag");
						emTag.appendChild(document.createTextNode(Helper.convByteArrayToString(r.getBody().getTag())));
						emBody.appendChild(emTag);
					}
					if (r.getBody().getMatchingData() != null) {
						Element emMatchingData = document.createElement(iso + "MatchingData");
						if (r.getBody().getMatchingData().getLength() != null) {
							Element emLength = document.createElement(iso + "Length");
							emLength.appendChild(document.createTextNode(Helper.convByteArrayToString(r.getBody().getMatchingData()
									.getLength())));
							emMatchingData.appendChild(emLength);
						}
						if (r.getBody().getMatchingData().getOffset() != null) {
							Element emOffset = document.createElement(iso + "Offset");
							emOffset.appendChild(document.createTextNode(Helper.convByteArrayToString(r.getBody().getMatchingData()
									.getOffset())));
							emMatchingData.appendChild(emOffset);
						}
						if (r.getBody().getMatchingData().getMask() != null) {
							Element emMask = document.createElement(iso + "Mask");
							emMask.appendChild(document.createTextNode(Helper
									.convByteArrayToString(r.getBody().getMatchingData().getMask())));
							emMatchingData.appendChild(emMask);
						}
						if (r.getBody().getMatchingData().getMatchingValue() != null) {
							Element emMatchingValue = document.createElement(iso + "MatchingValue");
							emMatchingValue.appendChild(document.createTextNode(Helper.convByteArrayToString(r.getBody().getMatchingData()
									.getMatchingValue())));
							emMatchingData.appendChild(emMatchingValue);
						}
						emBody.appendChild(emMatchingData);
					}
					emResponseAPDU.appendChild(emBody);
				}
				if (r.getTrailer() != null) {
					Element emTrailer = document.createElement(iso + "Trailer");
					emTrailer.appendChild(document.createTextNode(Helper.convByteArrayToString(r.getTrailer())));
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
					if (r.getConclusion().getTLSMarker() != null) {
						Element emTLSMarker = document.createElement(iso + "TLSMarker");
						emTLSMarker.setAttribute("Protocol", "urn:ietf:rfc:5246");
						Element emKey = document.createElement(tls + "Key");
						emTLSMarker.appendChild(emKey);

						for (int i = 0; i < r.getConclusion().getTLSMarker().getAny().get(0).getChildNodes().getLength(); i++) {
							Node n = r.getConclusion().getTLSMarker().getAny().get(0).getChildNodes().item(i);
							if (n.getNodeName().equals("Certificate")) {
								Element elem = document.createElement(tls + n.getNodeName());
								Element elem2 = document.createElement(iso + "efIdOrPath");
								elem2.appendChild(document.createTextNode(n.getTextContent()));
								elem.appendChild(elem2);
								emKey.appendChild(elem);
								System.out.println(n.getChildNodes().item(0).getNodeName());
							} else {
								Element elem = document.createElement(tls + n.getNodeName());
								elem.appendChild(document.createTextNode(n.getTextContent()));
								emKey.appendChild(elem);
							}
						}

						emConclusion.appendChild(emTLSMarker);
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

	private synchronized ResponseAPDUType parseResponseAPDUType(XmlPullParser parser) throws XmlPullParserException, IOException,
			ParserConfigurationException {
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
				} else if (parser.getName().equals("TLSMarker")) {
					conc.setTLSMarker(this.parseTLSMarker(parser));
				}
			}

		} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Conclusion")));
		return conc;
	}

	private TLSMarkerType parseTLSMarker(XmlPullParser parser) throws XmlPullParserException, IOException, ParserConfigurationException {
		TLSMarkerType tlsMarkerType = new TLSMarkerType();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document d = builder.newDocument();
		Element emKey = d.createElement("Key");
		Element emCertificate = d.createElement("Certificate");
		;
		int eventType = parser.getEventType();
		do {
			parser.next();
			eventType = parser.getEventType();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("Applicationidentifier") || parser.getName().equals("CardAlgRef")
						|| parser.getName().equals("KeyRef") || parser.getName().equals("SignatureGenerationInfo")) {
					Element emApplicationidentifier = d.createElement(parser.getName());
					emApplicationidentifier.setTextContent(parser.nextText());
					emKey.appendChild(emApplicationidentifier);
				} else if (parser.getName().equals("efIdOrPath")) {
					Element emEfIdOrPath = d.createElement("efIdOrPath");
					emEfIdOrPath.appendChild(d.createTextNode(parser.nextText()));
					emCertificate.appendChild(emEfIdOrPath);
				}
			}

		} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("TLSMarker")));
		emKey.appendChild(emCertificate);
		tlsMarkerType.getAny().add(emKey);
		return tlsMarkerType;

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

	private synchronized CardCall parseCardCall(XmlPullParser parser) throws XmlPullParserException, IOException,
			ParserConfigurationException {
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

	private synchronized Object parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParserConfigurationException {
		if (parser.getName().equals("StartPAOSResponse")) {
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
					if (parser.getName().equals("ResultMajor")) {
						Result r = new Result();
						r.setResultMajor(parser.nextText());
						waitResponse.setResult(r);
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
					if (parser.getName().equals("ResultMajor")) {
						Result r = new Result();
						r.setResultMajor(parser.nextText());
						getStatusResponse.setResult(r);
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

	private Result parseResult(XmlPullParser parser) throws XmlPullParserException, IOException {
		Result r = new Result();
		int eventType = parser.getEventType();
		do {
			parser.next();
			eventType = parser.getEventType();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("ResultMajor")) {
					r.setResultMajor(parser.nextText());
				}
				if (parser.getName().equals("ResultMinor")) {
					r.setResultMinor(parser.nextText());
				}
				if (parser.getName().equals("ResultMessage")) {
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
