/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.ws.android;

import de.bund.bsi.ecard.api._1.ConnectionHandle;
import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.APIAccessEntryPointName;
import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ActionNameType;
import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.ApplicationCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.AuthorizationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.BasicRequirementsType;
import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.BioSensorCapabilityType;
import iso.std.iso_iec._24727.tech.schema.CAMarkerType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardCall;
import iso.std.iso_iec._24727.tech.schema.CardInfo;
import iso.std.iso_iec._24727.tech.schema.CardTypeType;
import iso.std.iso_iec._24727.tech.schema.CardTypeType.Version;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Conclusion;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.SlotInfo;
import iso.std.iso_iec._24727.tech.schema.ConnectionServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DataMaskType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.EAC1InputType;
import iso.std.iso_iec._24727.tech.schema.EAC1OutputType;
import iso.std.iso_iec._24727.tech.schema.EAC2InputType;
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType;
import iso.std.iso_iec._24727.tech.schema.EACAdditionalInputType;
import iso.std.iso_iec._24727.tech.schema.EACMarkerType;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.MatchingDataType;
import iso.std.iso_iec._24727.tech.schema.MutualAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.OutputInfoType;
import iso.std.iso_iec._24727.tech.schema.PACEMarkerType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import iso.std.iso_iec._24727.tech.schema.RIMarkerType;
import iso.std.iso_iec._24727.tech.schema.RSAAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType.And;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType.Or;
import iso.std.iso_iec._24727.tech.schema.SimpleFUStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.TAMarkerType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
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
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.Configuration;
import org.openecard.addon.manifest.ConfigurationEntry;
import org.openecard.addon.manifest.EnumEntry;
import org.openecard.addon.manifest.LocalizedString;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WhitespaceFilter;
import org.openecard.ws.soap.MessageFactory;
import org.openecard.ws.soap.SOAPBody;
import org.openecard.ws.soap.SOAPException;
import org.openecard.ws.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * This class is a provisional and simple replacement for the JAXB-Marshaller
 * used in the applet and the rich client since JAXB is not available on
 * Android.
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class AndroidMarshaller implements WSMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidMarshaller.class);

    private static final String ISO_PFX = "iso";
    private static final String ISO_NS = "urn:iso:std:iso-iec:24727:tech:schema";
    private static final String DSS_PFX = "dss";
    private static final String DSS_NS = "urn:oasis:names:tc:dss:1.0:core:schema";
    private static final String ECAPI_PFX = "ecapi";
    private static final String ECAPI_NS = "http://www.bsi.bund.de/ecard/api/1.1";

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
	    documentBuilderFactory.setExpandEntityReferences(false);
	    documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
	    // "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	    soapFactory = MessageFactory.newInstance(documentBuilder);
	} catch (Exception ex) {
	    LOG.error(ex.getMessage(), ex);
	    System.exit(1); // non recoverable
	}
    }

    private Element createElementIso(Document document, String name) {
	Element rootElement = document.createElementNS(ISO_NS, name);
	rootElement.setPrefix(ISO_PFX);
	return rootElement;
    }

    private Element createElementDss(Document document, String name) {
	Element rootElement = document.createElementNS(DSS_NS, name);
	rootElement.setPrefix(DSS_PFX);
	return rootElement;
    }

    private Element createElementEcapi(Document document, String name) {
	Element rootElement = document.createElementNS(ECAPI_NS, name);
	rootElement.setPrefix(ECAPI_PFX);
	return rootElement;
    }


    @Override
    public void addXmlTypeClass(Class xmlTypeClass) throws MarshallingTypeException {
	// not available in this implementation
    }

    @Override
    public void removeAllTypeClasses() {
	// not available in this implementation
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
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Element emSlotHandle = createElementIso(document, "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(destroyChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	} else if (o instanceof DestroyChannelResponse) {
	    DestroyChannelResponse response = (DestroyChannelResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());

	    if (response.getProfile() != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(response.getProfile()));
		rootElement.appendChild(emProfile);
	    }

	    if (response.getRequestID() != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(response.getRequestID()));
		rootElement.appendChild(emRequest);
	    }

	    if (response.getResult() != null) {
		Element emResult = marshalResult(response.getResult(), document);
		rootElement.appendChild(emResult);
	    }

	} else if (o instanceof EstablishChannel) {
	    EstablishChannel establishChannel = (EstablishChannel) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());

	    Element emSlotHandle = createElementIso(document, "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(establishChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	    Element emAuthProtData = createElementIso(document, "AuthenticationProtocolData");
	    emAuthProtData.setAttribute("Protocol", establishChannel.getAuthenticationProtocolData().getProtocol());

	    for (Element e : establishChannel.getAuthenticationProtocolData().getAny()) {
		Element eClone = createElementIso(document, e.getLocalName());
		eClone.setTextContent(e.getTextContent());
		emAuthProtData.appendChild(eClone);

	    }

	    if (establishChannel.getProfile() != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(establishChannel.getProfile()));
		rootElement.appendChild(emProfile);
	    }

	    if (establishChannel.getRequestID() != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(establishChannel.getRequestID()));
		rootElement.appendChild(emRequest);
	    }

	    rootElement.appendChild(emAuthProtData);
	} else if (o instanceof EstablishChannelResponse) {
	    EstablishChannelResponse response = (EstablishChannelResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());

	    if (response.getProfile() != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(response.getProfile()));
		rootElement.appendChild(emProfile);
	    }

	    if (response.getRequestID() != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(response.getRequestID()));
		rootElement.appendChild(emRequest);
	    }

	    if (response.getResult() != null) {
		Element emResult = marshalResult(response.getResult(), document);
		rootElement.appendChild(emResult);
	    }

	    if (response.getAuthenticationProtocolData() != null) {
		Element emAuthProtData = createElementIso(document, "AuthenticationProtocolData");
		emAuthProtData.setAttribute("Protocol", response.getAuthenticationProtocolData().getProtocol());

		for (Element e : response.getAuthenticationProtocolData().getAny()) {
		    Element eClone = createElementIso(document, e.getLocalName());
		    eClone.setTextContent(e.getTextContent());
		    emAuthProtData.appendChild(eClone);

		}
	    }
	} else if (o instanceof DIDAuthenticate) {
	    DIDAuthenticate auth = (DIDAuthenticate) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());

	    Element em;
	    if (auth.getConnectionHandle() != null) {
		em = marshalConnectionHandle(auth.getConnectionHandle(), document);
		rootElement.appendChild(em);
	    }

	    if (auth.getDIDScope() != null) {
		em = createElementIso(document, "DIDScope");
		em.appendChild(document.createTextNode(auth.getDIDScope().value()));
		rootElement.appendChild(em);
	    }

	    if (auth.getDIDName() != null) {
		em = createElementIso(document, "DIDName");
		em.appendChild(document.createTextNode(auth.getDIDName()));
		rootElement.appendChild(em);
	    }

	    if (auth.getAuthenticationProtocolData() != null) {
		DIDAuthenticationDataType d = auth.getAuthenticationProtocolData();

		em = createElementIso(document, "AuthenticationProtocolData");
		em.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		if (d instanceof EAC1OutputType) {
		    em.setAttribute("xsi:type", "iso:EAC1OutputType");
		} else if (d instanceof EAC2OutputType) {
		    em.setAttribute("xsi:type", "iso:EAC2OutputType");
		} else if (d instanceof EAC1InputType) {
		    em.setAttribute("xsi:type", "iso:EAC1InputType");
		} else if (d instanceof EAC2InputType) {
		    em.setAttribute("xsi:type", "iso:EAC2InputType");
		} else {
		    String msg = "Marshalling a DIDAuthenticationDataType of "
			    + d.getClass().getName() + " in DIDAuthentication is not supported";
		    LOG.error(msg);
		    throw new MarshallingTypeException(msg);
		}

		for (Element e : d.getAny()) {
		    Element elemCopy = createElementIso(document, e.getLocalName());
		    elemCopy.setTextContent(e.getTextContent());
		    em.appendChild(elemCopy);
		}

		for (Map.Entry<QName, String> entry : d.getOtherAttributes().entrySet()) {
		    em.setAttribute(entry.getKey().getNamespaceURI(), entry.getValue());
		}

		if (d.getProtocol() != null) {
		    em.setAttribute("Protocol", d.getProtocol());
		}
		rootElement.appendChild(em);
	    }

	    if (auth.getSAMConnectionHandle() != null) {
		em = marshalConnectionHandle(auth.getSAMConnectionHandle(), document);
		rootElement.appendChild(em);
	    }

	    if (auth.getProfile() != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(auth.getProfile()));
		rootElement.appendChild(emProfile);
	    }

	    if (auth.getRequestID() != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(auth.getRequestID()));
		rootElement.appendChild(emRequest);
	    }
	} else if (o instanceof DIDAuthenticateResponse) {
	    DIDAuthenticateResponse didAuthenticateResponse = (DIDAuthenticateResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    rootElement.appendChild(marshalResult(didAuthenticateResponse.getResult(), document));
	    if (didAuthenticateResponse.getAuthenticationProtocolData() != null) {
		DIDAuthenticationDataType didAuthenticationDataType = didAuthenticateResponse.getAuthenticationProtocolData();

		Element elemEACOutput = createElementIso(document, "AuthenticationProtocolData");
		elemEACOutput.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		if (didAuthenticationDataType instanceof EAC1OutputType) {
		    elemEACOutput.setAttribute("xsi:type", "iso:EAC1OutputType");
		} else if (didAuthenticationDataType instanceof EAC2OutputType) {
		    elemEACOutput.setAttribute("xsi:type", "iso:EAC2OutputType");
		} else {
		    String msg = "Marshalling a DIDAuthenticationDataType of " +
			didAuthenticationDataType.getClass().getName() + " in DIDAuthentication is not supported";
		    throw new MarshallingTypeException(msg);
		}
		for (Element e : didAuthenticationDataType.getAny()) {
		    Element elemCopy = createElementIso(document, e.getLocalName());
		    elemCopy.setTextContent(e.getTextContent());
		    elemEACOutput.appendChild(elemCopy);
		}

		rootElement.appendChild(elemEACOutput);

	    } // else only the result (with error) is returned
	} else if (o instanceof InitializeFrameworkResponse) {
	    InitializeFrameworkResponse initializeFrameworkResponse = (InitializeFrameworkResponse) o;
	    rootElement = createElementEcapi(document, o.getClass().getSimpleName());
	    rootElement.appendChild(marshalResult(initializeFrameworkResponse.getResult(), document));
	    Element emVersion = createElementEcapi(document, "Version");
	    Element emMajor = createElementEcapi(document, "Major");
	    emMajor.appendChild(document.createTextNode(initializeFrameworkResponse.getVersion().getMajor().toString()));
	    emVersion.appendChild(emMajor);
	    Element emMinor = createElementEcapi(document, "Minor");
	    emMinor.appendChild(document.createTextNode(initializeFrameworkResponse.getVersion().getMinor().toString()));
	    emVersion.appendChild(emMinor);
	    Element emSubMinor = createElementEcapi(document, "SubMinor");
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
	    StartPAOS startPAOS = (StartPAOS) o;

	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    if (startPAOS.getRequestID() != null) {
		rootElement.setAttribute("RequestID", startPAOS.getRequestID());
	    }
	    if (startPAOS.getProfile() != null) {
		rootElement.setAttribute("Profile", startPAOS.getProfile());
	    }

	    Element em = createElementIso(document, "SessionIdentifier");
	    em.appendChild(document.createTextNode(startPAOS.getSessionIdentifier()));
	    rootElement.appendChild(em);

	    rootElement.appendChild(marshalConnectionHandle(startPAOS.getConnectionHandle().get(0), document));

	    StartPAOS.UserAgent ua = startPAOS.getUserAgent();
	    if (ua != null) {
		Element emUa = createElementIso(document, "UserAgent");
		if (ua.getName() != null) {
		    Element emUaSub = createElementIso(document, "Name");
		    emUaSub.appendChild(document.createTextNode(ua.getName()));
		    emUa.appendChild(emUaSub);
		}
		if (ua.getVersionMajor() != null) {
		    Element emUaSub = createElementIso(document, "VersionMajor");
		    emUaSub.appendChild(document.createTextNode(ua.getVersionMajor().toString()));
		    emUa.appendChild(emUaSub);
		}
		if (ua.getVersionMinor() != null) {
		    Element emUaSub = createElementIso(document, "VersionMinor");
		    emUaSub.appendChild(document.createTextNode(ua.getVersionMinor().toString()));
		    emUa.appendChild(emUaSub);
		}
		if (ua.getVersionSubminor() != null) {
		    Element emUaSub = createElementIso(document, "VersionSubminor");
		    emUaSub.appendChild(document.createTextNode(ua.getVersionSubminor().toString()));
		    emUa.appendChild(emUaSub);
		}
		rootElement.appendChild(emUa);
	    }

	    List<StartPAOS.SupportedAPIVersions> sas = startPAOS.getSupportedAPIVersions();
	    for (StartPAOS.SupportedAPIVersions sa : sas) {
		if (sa != null) {
		    Element emSa = createElementIso(document, "SupportedAPIVersions");
		    if (sa.getMajor() != null) {
			Element emSaSub = createElementIso(document, "Major");
			emSaSub.appendChild(document.createTextNode(sa.getMajor().toString()));
			emSa.appendChild(emSaSub);
		    }
		    if (sa.getMinor() != null) {
			Element emSaSub = createElementIso(document, "Minor");
			emSaSub.appendChild(document.createTextNode(sa.getMinor().toString()));
			emSa.appendChild(emSaSub);
		    }
		    if (sa.getSubminor() != null) {
			Element emSaSub = createElementIso(document, "Subminor");
			emSaSub.appendChild(document.createTextNode(sa.getSubminor().toString()));
			emSa.appendChild(emSaSub);
		    }
		    rootElement.appendChild(emSa);
		}
	    }

	    List<String> sdids = startPAOS.getSupportedDIDProtocols();
	    for (String sdid : sdids) {
		Element emSdid = createElementIso(document, "SupportedDIDProtocols");
		emSdid.appendChild(document.createTextNode(sdid));
		rootElement.appendChild(emSdid);
	    }
	} else if (o instanceof TransmitResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    TransmitResponse transmitResponsePOJO = (TransmitResponse) o;

	    Element em = marshalResult(transmitResponsePOJO.getResult(), document);
	    rootElement.appendChild(em);

	    for (int i = 0; i < transmitResponsePOJO.getOutputAPDU().size(); i++) {
		em = createElementIso(document, "OutputAPDU");
		em.appendChild(document.createTextNode(ByteUtils.toHexString(transmitResponsePOJO.getOutputAPDU().get(i))));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof EstablishContext) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	} else if (o instanceof EstablishContextResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    EstablishContextResponse establishContextResponse = (EstablishContextResponse) o;

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(establishContextResponse.getContextHandle())));
	    rootElement.appendChild(em);

	    em = marshalResult(establishContextResponse.getResult(), document);
	    rootElement.appendChild(em);

	} else if (o instanceof GetStatus) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    GetStatus getStatus = (GetStatus) o;

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(getStatus.getContextHandle())));
	    rootElement.appendChild(em);
	    if (getStatus.getIFDName() != null) {
		em = createElementIso(document, "IFDName");
		em.appendChild(document.createTextNode(getStatus.getIFDName()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Wait) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Wait w = (Wait) o;

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(w.getContextHandle())));
	    rootElement.appendChild(em);

	    if (w.getTimeOut() != null) {
		em = createElementIso(document, "TimeOut");
		em.appendChild(document.createTextNode(w.getTimeOut().toString(16)));
		rootElement.appendChild(em);
	    }

	    if (w.getCallback() != null) {
		ChannelHandleType callback = w.getCallback();
		em = createElementIso(document, "Callback");

		if (callback.getBinding() != null) {
		    Element em2 = createElementIso(document, "Binding");
		    em2.appendChild(document.createTextNode(callback.getBinding()));
		    em.appendChild(em2);
		}
		if (callback.getSessionIdentifier() != null) {
		    Element em2 = createElementIso(document, "SessionIdentifier");
		    em2.appendChild(document.createTextNode(callback.getSessionIdentifier()));
		    em.appendChild(em2);
		}
		if (callback.getProtocolTerminationPoint() != null) {
		    Element em2 = createElementIso(document, "ProtocolTerminationPoint");
		    em2.appendChild(document.createTextNode(callback.getProtocolTerminationPoint()));
		    em.appendChild(em2);
		}
		if (callback.getPathSecurity() != null) {
		    PathSecurityType pathSecurityType = callback.getPathSecurity();
		    Element em2 = createElementIso(document, "PathSecurity");
		    Element em3 = createElementIso(document, "Protocol");

		    em3.appendChild(document.createTextNode(pathSecurityType.getProtocol()));
		    em2.appendChild(em3);
		    if (pathSecurityType.getParameters() != null) {
			em3 = createElementIso(document, "Parameters");
			em3.appendChild(document.createTextNode(pathSecurityType.getParameters().toString()));
			em2.appendChild(em3);
		    }
		    em.appendChild(em2);
		}
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Connect) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Connect c = (Connect) o;

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(c.getContextHandle())));
	    rootElement.appendChild(em);

	    em = createElementIso(document, "IFDName");
	    em.appendChild(document.createTextNode(c.getIFDName()));
	    rootElement.appendChild(em);

	    em = createElementIso(document, "Slot");
	    em.appendChild(document.createTextNode(c.getSlot().toString()));
	    rootElement.appendChild(em);
	    if (c.isExclusive() != null) {
		em = createElementIso(document, "Exclusive");
		em.appendChild(document.createTextNode(c.isExclusive().toString()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof ConnectResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    ConnectResponse cr = (ConnectResponse) o;

	    Element em = createElementIso(document, "SlotHandle");
	    if (cr.getSlotHandle() != null) {
		em.appendChild(document.createTextNode(ByteUtils.toHexString(cr.getSlotHandle())));
		rootElement.appendChild(em);
	    }

	    em = marshalResult(cr.getResult(), document);
	    rootElement.appendChild(em);
	} else if (o instanceof ListIFDs) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    ListIFDs c = (ListIFDs) o;

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(c.getContextHandle())));
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDsResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    ListIFDsResponse listIFDsResponse = (ListIFDsResponse) o;

	    for (String s : listIFDsResponse.getIFDName()) {
		Element em = createElementIso(document, "IFDName");
		em.appendChild(document.createTextNode(s));
		rootElement.appendChild(em);
	    }

	    Element em = marshalResult(listIFDsResponse.getResult(), document);
	    rootElement.appendChild(em);

	} else if (o instanceof Transmit) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Transmit t = (Transmit) o;

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(t.getSlotHandle())));
	    rootElement.appendChild(em);

	    for (int i = 0; i < t.getInputAPDUInfo().size(); i++) {
		em = createElementIso(document, "InputAPDUInfo");
		rootElement.appendChild(em);
		Element em2 = createElementIso(document, "InputAPDU");
		em2.appendChild(document.createTextNode(ByteUtils.toHexString(t.getInputAPDUInfo().get(i).getInputAPDU())));
		em.appendChild(em2);
		for (int y = 0; y < t.getInputAPDUInfo().get(i).getAcceptableStatusCode().size(); y++) {
		    em2 = createElementIso(document, "AcceptableStatusCode");
		    em2.appendChild(document.createTextNode(ByteUtils.toHexString(t.getInputAPDUInfo().get(i).getAcceptableStatusCode()
			    .get(y))));
		    em.appendChild(em2);
		}
	    }
	} else if (o instanceof RecognitionTree) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    RecognitionTree recognitionTree = (RecognitionTree) o;
	    for (CardCall c : recognitionTree.getCardCall()) {
		rootElement.appendChild(marshalCardCall(c, document));
	    }
	} else if (o instanceof CardCall) {
	    CardCall c = (CardCall) o;
	    rootElement = (Element) marshalCardCall(c, document);

	} else if (o instanceof Disconnect) {
	    Disconnect d = (Disconnect) o;
	    rootElement = createElementIso(document, d.getClass().getSimpleName());

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(d.getSlotHandle())));
	    rootElement.appendChild(em);

	    if (d.getAction() != null) {
		em = createElementIso(document, "Action");
		em.appendChild(document.createTextNode(d.getAction().value()));
	    }
	} else if (o instanceof DisconnectResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    DisconnectResponse response = (DisconnectResponse) o;
	    String profile = response.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }
	    String requestID = response.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }
	    Element emResult = marshalResult(response.getResult(), document);
	    rootElement.appendChild(emResult);

	} else if (o instanceof GetIFDCapabilities) {
	    GetIFDCapabilities getIFDCapabilities = (GetIFDCapabilities) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Element emContextHandle = createElementIso(document, "ContextHandle");
	    emContextHandle.appendChild(document.createTextNode(ByteUtils.toHexString(getIFDCapabilities.getContextHandle())));
	    rootElement.appendChild(emContextHandle);
	    Element emIFDName = createElementIso(document, "IFDName");
	    emIFDName.appendChild(document.createTextNode(getIFDCapabilities.getIFDName()));
	    rootElement.appendChild(emIFDName);
	} else if (o instanceof GetIFDCapabilitiesResponse) {
	    try {
		GetIFDCapabilitiesResponse response = (GetIFDCapabilitiesResponse) o;
		rootElement = createElementIso(document, o.getClass().getSimpleName());
		String profile = response.getProfile();
		if (profile != null) {
		    Element emProfile = createElementIso(document, "Profile");
		    emProfile.appendChild(document.createTextNode(profile));
		    rootElement.appendChild(emProfile);
		}
		String requestID = response.getRequestID();
		if (requestID != null) {
		    Element emRequest = createElementIso(document, "RequestID");
		    emRequest.appendChild(document.createElement(requestID));
		    rootElement.appendChild(emRequest);
		}
		Element emResult = marshalResult(response.getResult(), document);
		rootElement.appendChild(emResult);
		if (response.getIFDCapabilities() != null) {
		    Element emIFDCaps = marshalIFDCapabilities(response.getIFDCapabilities(), document);
		    rootElement.appendChild(emIFDCaps);
		}
	    } catch (Exception ex) {
		LOG.error(ex.getMessage(), ex);
	    }
	} else if (o instanceof TCTokenType) {
	    TCTokenType tctoken = (TCTokenType) o;
	    rootElement = document.createElement(o.getClass().getSimpleName());
	    Element em = document.createElement("ServerAddress");
	    em.appendChild(document.createTextNode(tctoken.getServerAddress()));
	    rootElement.appendChild(em);
	    em = document.createElement("SessionIdentifier");
	    em.appendChild(document.createTextNode(tctoken.getSessionIdentifier()));
	    rootElement.appendChild(em);
	    em = document.createElement("RefreshAddress");
	    em.appendChild(document.createTextNode(tctoken.getRefreshAddress()));
	    rootElement.appendChild(em);
	    // Optional element CommunicationErrorAddress
	    String communicationErrorAddress = tctoken.getCommunicationErrorAddress();
	    if (communicationErrorAddress != null) {
		em = document.createElement("CommunicationErrorAddress");
		em.appendChild(document.createTextNode(tctoken.getCommunicationErrorAddress()));
		rootElement.appendChild(em);
	    }
	    em = document.createElement("Binding");
	    em.appendChild(document.createTextNode(tctoken.getBinding()));
	    rootElement.appendChild(em);
	    // Optional element PathSecurity-Protocol
	    String pathSecurityProtocol = tctoken.getPathSecurityProtocol();
	    if (pathSecurityProtocol != null) {
		em = document.createElement("PathSecurity-Protocol");
		em.appendChild(document.createTextNode(tctoken.getPathSecurityProtocol()));
		rootElement.appendChild(em);
	    }
	    // Optional element PathSecurity-Parameters
	    TCTokenType.PathSecurityParameters pathSecurityParameters = tctoken.getPathSecurityParameters();
	    if (pathSecurityParameters != null) {
		em = document.createElement("PSK");
		em.appendChild(document.createTextNode(ByteUtils.toHexString(pathSecurityParameters.getPSK())));
		Element em1 = document.createElement("PathSecurity-Parameters");
		em1.appendChild(em);
		rootElement.appendChild(em1);
	    }
	} else if (o instanceof CardApplicationPath) {
	    CardApplicationPath p = (CardApplicationPath) o;
	    rootElement = createElementIso(document, "CardApplicationPath");

	    Element em = createElementIso(document, "CardAppPathRequest");

	    String profile = p.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }
	    String requestID = p.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }

	    // ChannelHandle
	    ChannelHandleType h = p.getCardAppPathRequest().getChannelHandle();
	    
	    if(h != null){
	    
		Element emChild = createElementIso(document, "ChannelHandle");
		em.appendChild(emChild);

		Element emChildOfCH;
		if (h.getProtocolTerminationPoint() != null) {
		    emChildOfCH = createElementIso(document, "ProtocolTerminationPoint");
		    emChildOfCH.appendChild(document.createTextNode(h.getProtocolTerminationPoint()));
		    emChild.appendChild(emChildOfCH);
		}

		if (h.getSessionIdentifier() != null) {
		    emChildOfCH = createElementIso(document, "SessionIdentifier");
		    emChildOfCH.appendChild(document.createTextNode(h.getSessionIdentifier()));
		    emChild.appendChild(emChildOfCH);
		}

		if (h.getBinding() != null) {
		    emChildOfCH = createElementIso(document, "Binding");
		    emChildOfCH.appendChild(document.createTextNode(h.getBinding()));
		    emChild.appendChild(emChildOfCH);
		}

		PathSecurityType ps = h.getPathSecurity();
		if (ps != null) {
		    emChildOfCH = createElementIso(document, "PathSecurity");
		    Element emChildOfPS = createElementIso(document, "Protocol");
		    emChildOfPS.appendChild(document.createTextNode(ps.getProtocol()));
		    emChildOfCH.appendChild(emChildOfPS);
		    // TODO here any type parsen
		    LOG.error("AnyType of CardApplicationPath: " + ps.getParameters().toString());
		    emChild.appendChild(emChildOfCH);
		}

		// context handle
		emChild = createElementIso(document, "ContextHandle");
		emChild.appendChild(document.createTextNode(ByteUtils.toHexString(p.getCardAppPathRequest().getContextHandle())));
		em.appendChild(emChild);

		// IFDName
		emChild = createElementIso(document, "IFDName");
		emChild.appendChild(document.createTextNode(p.getCardAppPathRequest().getIFDName()));
		em.appendChild(emChild);

		// SlotIndex
		emChild = createElementIso(document, "SlotIndex");
		emChild.appendChild(document.createTextNode(p.getCardAppPathRequest().getSlotIndex().toString()));
		em.appendChild(emChild);

		// Card Application
		emChild = createElementIso(document, "CardApplication");
		emChild.appendChild(document.createTextNode(ByteUtils.toHexString(p.getCardAppPathRequest().getCardApplication())));
		em.appendChild(emChild);
	    }

	    rootElement.appendChild(em);
	} else if (o instanceof CardApplicationPathResponse) {
	    CardApplicationPathResponse resp = (CardApplicationPathResponse) o;
	    rootElement = createElementIso(document, "CardApplicationPathResponse");

	    String profile = resp.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }

	    String requestID = resp.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }

	    Result result = resp.getResult();
	    if (result != null) {
		Element emResult = marshalResult(resp.getResult(), document);
		rootElement.appendChild(emResult);
	    }

	    Element em = createElementIso(document, "CardAppPathResultSet");
	    for (CardApplicationPathType path : resp.getCardAppPathResultSet().getCardApplicationPathResult()) {
		em.appendChild(marshalCardApplicationPathResult(path, document, "CardAppPathRequest"));
	    }
	    rootElement.appendChild(em);
	} else if (o instanceof BeginTransaction) {
	    BeginTransaction t = (BeginTransaction) o;
	    rootElement = createElementIso(document, "BeginTransaction");

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(t.getSlotHandle())));
	    rootElement.appendChild(em);
	} else if (o instanceof BeginTransactionResponse) {
	    BeginTransactionResponse response = (BeginTransactionResponse) o;
	    rootElement = createElementIso(document, "BeginTransactionResponse");
	    String profile = response.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }
	    String requestID = response.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }
	    Element emResult = marshalResult(response.getResult(), document);
	    rootElement.appendChild(emResult);

	} else if (o instanceof EndTransaction) {
	    EndTransaction end = (EndTransaction) o;
	    rootElement = createElementIso(document, "EndTransaction");

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(end.getSlotHandle())));
	    rootElement.appendChild(em);
	} else if (o instanceof EndTransactionResponse) {
	    EndTransactionResponse response = (EndTransactionResponse) o;
	    rootElement = createElementIso(document, "EndTransactionResponse");
	    String profile = response.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }
	    String requestID = response.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }
	    Element emResult = marshalResult(response.getResult(), document);
	    rootElement.appendChild(emResult);
	} else if (o instanceof CardApplicationConnect) {
	    CardApplicationConnect c = (CardApplicationConnect) o;
	    rootElement = createElementIso(document, "CardApplicationConnect");

	    // Card Application Path
	    rootElement.appendChild(marshalCardApplicationPathResult(c.getCardApplicationPath(), document, "CardApplicationPath"));

	    Element em;
	    if (c.getOutput() != null) {
		em = createElementIso(document, "Output");
		em.appendChild(marshalOutput(c.getOutput(), document));
		rootElement.appendChild(em);
	    }

	    // Exclusive Use
	    if (c.isExclusiveUse() != null) {
		em = createElementIso(document, "ExclusiveUse");
		em.appendChild(document.createTextNode(Boolean.toString(c.isExclusiveUse())));
		rootElement.appendChild(em);
	    }
	} else if (o instanceof CardApplicationConnectResponse) {
	    CardApplicationConnectResponse resp = (CardApplicationConnectResponse) o;
	    rootElement = createElementIso(document, "CardApplicationConnectResponse");

	    appendResponseValues(resp.getProfile(), resp.getRequestID(), resp.getResult(), rootElement, document);

	    ConnectionHandleType ch = resp.getConnectionHandle();
	    if (ch != null) {
		Element em = marshalConnectionHandle(ch, document);
		rootElement.appendChild(em);
	    }
	} else if (o instanceof CardApplicationDisconnect) {
	    CardApplicationDisconnect c = (CardApplicationDisconnect) o;
	    rootElement = createElementIso(document, "CardApplicationDisconnect");

	    if (c.getConnectionHandle() != null) {
		rootElement.appendChild(marshalConnectionHandle(c.getConnectionHandle(), document));
	    }

	    if (c.getAction() != null) {
		Element em = createElementIso(document, "Action");
		em.appendChild(document.createTextNode(c.getAction().value()));
		rootElement.appendChild(em);
	    }

	    String profile = c.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }

	    String requestID = c.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }
	} else if (o instanceof CardApplicationDisconnectResponse) {
	    CardApplicationDisconnectResponse response = (CardApplicationDisconnectResponse) o;
	    rootElement = createElementIso(document, "CardApplicationDisconnectResponse");
	    String profile = response.getProfile();
	    if (profile != null) {
		Element emProfile = createElementIso(document, "Profile");
		emProfile.appendChild(document.createTextNode(profile));
		rootElement.appendChild(emProfile);
	    }
	    String requestID = response.getRequestID();
	    if (requestID != null) {
		Element emRequest = createElementIso(document, "RequestID");
		emRequest.appendChild(document.createElement(requestID));
		rootElement.appendChild(emRequest);
	    }
	    if (response.getResult() != null) {
		Element emResult = marshalResult(response.getResult(), document);
		rootElement.appendChild(emResult);
	    }
	} else {
	    throw new IllegalArgumentException("Cannot marshal " + o.getClass().getSimpleName());
	}
	document.appendChild(rootElement);
	return document;
    }

    private Element marshalConnectionHandle(ConnectionHandleType ch, Document document) {
	Element em = createElementIso(document, "ConnectionHandle");

	Element em2;
	if (ch.getContextHandle() != null) {
	    em2 = createElementIso(document, "ContextHandle");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getContextHandle())));
	    em.appendChild(em2);
	}
	if (ch.getSlotHandle() != null) {
	    em2 = createElementIso(document, "SlotHandle");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getSlotHandle())));
	    em.appendChild(em2);
	}
	if (ch.getCardApplication() != null) {
	    em2 = createElementIso(document, "CardApplication");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getCardApplication())));
	    em.appendChild(em2);
	}
	if (ch.getSlotIndex() != null) {
	    em2 = createElementIso(document, "SlotIndex");
	    em2.appendChild(document.createTextNode(ch.getSlotIndex().toString()));
	    em.appendChild(em2);
	}
	if (ch.getIFDName() != null) {
	    em2 = createElementIso(document, "IFDName");
	    em2.appendChild(document.createTextNode(ch.getIFDName()));
	    em.appendChild(em2);
	}
	if (ch.getChannelHandle() != null) {
	    em2 = createElementIso(document, "ChannelHandle");
	    if (ch.getChannelHandle().getSessionIdentifier() != null) {
		Element em3 = createElementIso(document, "SessionIdentifier");
		em3.appendChild(document.createTextNode(ch.getChannelHandle()
			.getSessionIdentifier()));
		em2.appendChild(em3);
	    }
	    em.appendChild(em2);
	}
	if (ch.getRecognitionInfo() != null) {
	    em2 = createElementIso(document, "RecognitionInfo");
	    Element em3 = createElementIso(document, "CardType");
	    em3.appendChild(document.createTextNode(ch.getRecognitionInfo().getCardType()));
	    em2.appendChild(em3);
	    em.appendChild(em2);
	}
	return em;
    }

    private void appendResponseValues(String profile, String requestID, Result result, Element rootElement, Document document) {
	if (profile != null) {
	    Element emProfile = createElementIso(document, "Profile");
	    emProfile.appendChild(document.createTextNode(profile));
	    rootElement.appendChild(emProfile);
	}

	if (requestID != null) {
	    Element emRequest = createElementIso(document, "RequestID");
	    emRequest.appendChild(document.createElement(requestID));
	    rootElement.appendChild(emRequest);
	}

	if (result != null) {
	    Element emResult = marshalResult(result, document);
	    rootElement.appendChild(emResult);
	}
    }

    private Element marshalInternationStringType(InternationalStringType internationalStringType, Document document, String name) {
	Element em = createElementDss(document, name);
	em.appendChild(document.createTextNode(internationalStringType.getValue()));
	em.setAttribute("xml:lang", internationalStringType.getLang());
	return em;
    }

    private Element marshalOutput(OutputInfoType o, Document document) {
	Element em = createElementIso(document, "Output");

	Element e;
	if (o.getTimeout() != null) {
	    e = createElementIso(document, "Timeout");
	    e.appendChild(document.createTextNode(o.getTimeout().toString()));
	    em.appendChild(e);
	}

	if (o.getDisplayIndex() != null) {
	    e = createElementIso(document, "DisplayIndex");
	    e.appendChild(document.createTextNode(o.getDisplayIndex().toString()));
	    em.appendChild(e);
	}

	if (o.getMessage() != null) {
	    e = createElementIso(document, "Message");
	    e.appendChild(document.createTextNode(o.getMessage()));
	    em.appendChild(e);
	}

	if (o.isAcousticalSignal() != null) {
	    e = createElementIso(document, "AcousticalSignal");
	    e.appendChild(document.createTextNode(Boolean.toString(o.isAcousticalSignal())));
	    em.appendChild(e);
	}

	if (o.isOpticalSignal() != null) {
	    e = createElementIso(document, "OpticalSignal");
	    e.appendChild(document.createTextNode(Boolean.toString(o.isOpticalSignal())));
	    em.appendChild(e);
	}

	return em;
    }

    private Element marshalCardApplicationPathResult(CardApplicationPathType type, Document document, String name) {
	Element em = createElementIso(document, name);

	// ChannelHandle
	ChannelHandleType h = type.getChannelHandle();
	Element emChild = createElementIso(document, "ChannelHandle");

	Element emChildOfCH;
	if (h.getProtocolTerminationPoint() != null) {
	    emChildOfCH = createElementIso(document, "ProtocolTerminationPoint");
	    emChildOfCH.appendChild(document.createTextNode(h.getProtocolTerminationPoint()));
	    emChild.appendChild(emChildOfCH);
	}

	if (h.getSessionIdentifier() != null) {
	    emChildOfCH = createElementIso(document, "SessionIdentifier");
	    emChildOfCH.appendChild(document.createTextNode(h.getSessionIdentifier()));
	    emChild.appendChild(emChildOfCH);
	}

	if (h.getBinding() != null) {
	    emChildOfCH = createElementIso(document, "Binding");
	    emChildOfCH.appendChild(document.createTextNode(h.getBinding()));
	    emChild.appendChild(emChildOfCH);
	}

	PathSecurityType ps = h.getPathSecurity();
	if (ps != null) {
	    emChildOfCH = createElementIso(document, "PathSecurity");
	    Element emChildOfPS = createElementIso(document, "Protocol");
	    emChildOfPS.appendChild(document.createTextNode(ps.getProtocol()));
	    emChildOfCH.appendChild(emChildOfPS);
	    // TODO here any type parsen
	    LOG.error("AnyType of CardApplicationPath: " + ps.getParameters().toString());
	    emChild.appendChild(emChildOfCH);
	    em.appendChild(emChild);
	}

	// context handle
	emChild = createElementIso(document, "ContextHandle");
	emChild.appendChild(document.createTextNode(ByteUtils.toHexString(type.getContextHandle())));
	em.appendChild(emChild);

	// IFDName
	emChild = createElementIso(document, "IFDName");
	emChild.appendChild(document.createTextNode(type.getIFDName()));
	em.appendChild(emChild);

	// SlotIndex
	emChild = createElementIso(document, "SlotIndex");
	emChild.appendChild(document.createTextNode(type.getSlotIndex().toString()));
	em.appendChild(emChild);

	// Card Application
	emChild = createElementIso(document, "CardApplication");
	emChild.appendChild(document.createTextNode(ByteUtils.toHexString(type.getCardApplication())));
	em.appendChild(emChild);

	return em;
    }

    private Element marshalIFDCapabilities(IFDCapabilitiesType cap, Document document) {
	Element emIFDCaps = createElementIso(document, cap.getClass().getSimpleName());
	for (BioSensorCapabilityType bioCap : cap.getBioSensorCapability()) {
	    Element emBioCap = createElementIso(document, "BioSensorCapability");
	    Element emIndex = createElementIso(document, "Index");
	    emIndex.appendChild(document.createTextNode(bioCap.getIndex().toString()));
	    emBioCap.appendChild(emIndex);
	    Element emBiometricType = createElementIso(document, "BiometricType");
	    emBiometricType.appendChild(document.createTextNode(bioCap.getBiometricType().toString()));
	    emBioCap.appendChild(emBiometricType);
	    emIFDCaps.appendChild(emBioCap);
	}
	for (DisplayCapabilityType dispType : cap.getDisplayCapability()) {
	    Element emDisp = createElementIso(document, "DisplayCapability");
	    Element emIndex = createElementIso(document, "Index");
	    emIndex.appendChild(document.createTextNode(dispType.getIndex().toString()));
	    emDisp.appendChild(emIndex);
	    Element emLines = createElementIso(document, "Lines");
	    emLines.appendChild(document.createTextNode(dispType.getLines().toString()));
	    emDisp.appendChild(emLines);
	    Element emColumns = createElementIso(document, "Columns");
	    emColumns.appendChild(document.createTextNode(dispType.getColumns().toString()));
	    emDisp.appendChild(emColumns);
	    Element emVirLines = createElementIso(document, "VirtualLines");
	    emVirLines.appendChild(document.createTextNode(dispType.getVirtualLines().toString()));
	    emDisp.appendChild(emVirLines);
	    Element emVirColumns = createElementIso(document, "VirtualColumns");
	    emVirColumns.appendChild(document.createTextNode(dispType.getVirtualColumns().toString()));
	    emDisp.appendChild(emVirColumns);
	    emIFDCaps.appendChild(emDisp);
	}
	for (KeyPadCapabilityType keyPadType : cap.getKeyPadCapability()) {
	    Element emKP = createElementIso(document, "KeyPadCapability");
	    Element emIndex = createElementIso(document, "Index");
	    emIndex.appendChild(document.createTextNode(keyPadType.getIndex().toString()));
	    emKP.appendChild(emIndex);
	    Element emKeys = createElementIso(document, "Keys");
	    emKeys.appendChild(document.createTextNode(keyPadType.getKeys().toString()));
	    emKP.appendChild(emKeys);
	    emIFDCaps.appendChild(emKP);
	}
	for (SlotCapabilityType slotType : cap.getSlotCapability()) {
	    Element emSlot = createElementIso(document, "SlotCapability");
	    Element emIndex = createElementIso(document, "Index");
	    emIndex.appendChild(document.createTextNode(slotType.getIndex().toString()));
	    emSlot.appendChild(emIndex);
	    for (String protocol : slotType.getProtocol()) {
		Element emProtocol = createElementIso(document, "Protocol");
		emProtocol.appendChild(document.createTextNode(protocol));
		emSlot.appendChild(emProtocol);
	    }
	    emIFDCaps.appendChild(emSlot);
	}
	Element emOpticalSignalUnit = createElementIso(document, "OpticalSignalUnit");
	emOpticalSignalUnit.appendChild(document.createTextNode(Boolean.toString(cap.isOpticalSignalUnit())));
	emIFDCaps.appendChild(emOpticalSignalUnit);

	Element emAcousticSignalUnit = createElementIso(document, "AcousticSignalUnit");
	emAcousticSignalUnit.appendChild(document.createTextNode(Boolean.toString(cap.isAcousticSignalUnit())));
	emIFDCaps.appendChild(emAcousticSignalUnit);
	return emIFDCaps;
    }

    private synchronized Element marshalResult(Result r, Document document) {
	Element emResult = createElementDss(document, r.getClass().getSimpleName());
	Element em = createElementDss(document, "ResultMajor");
	em.appendChild(document.createTextNode(r.getResultMajor()));
	emResult.appendChild(em);
	if (r.getResultMinor() != null) {
	    em = createElementDss(document, "ResultMinor");
	    em.appendChild(document.createTextNode(r.getResultMinor()));
	    emResult.appendChild(em);
	}
	if (r.getResultMessage() != null && r.getResultMessage().getValue() != null) {
	    emResult.appendChild(marshalInternationStringType(r.getResultMessage(), document, "ResultMessage"));
	}
	return emResult;
    }

    private synchronized Node marshalCardCall(CardCall c, Document document) {
	Element emCardCall = createElementIso(document, "CardCall");
	if (c.getCommandAPDU() != null) {
	    Element emCommandAPDU = createElementIso(document, "CommandAPDU");
	    emCommandAPDU.appendChild(document.createTextNode(ByteUtils.toHexString(c.getCommandAPDU())));
	    emCardCall.appendChild(emCommandAPDU);
	}
	if (c.getResponseAPDU() != null && c.getResponseAPDU().size() > 0) {
	    for (ResponseAPDUType r : c.getResponseAPDU()) {
		Element emResponseAPDU = createElementIso(document, "ResponseAPDU");

		if (r.getBody() != null) {

		    Element emBody = createElementIso(document, "Body");
		    if (r.getBody().getTag() != null) {
			Element emTag = createElementIso(document, "Tag");
			emTag.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getTag())));
			emBody.appendChild(emTag);
		    }
		    if (r.getBody().getMatchingData() != null) {
			Element emMatchingData = createElementIso(document, "MatchingData");
			if (r.getBody().getMatchingData().getLength() != null) {
			    Element emLength = createElementIso(document, "Length");
			    emLength.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getLength())));
			    emMatchingData.appendChild(emLength);
			}
			if (r.getBody().getMatchingData().getOffset() != null) {
			    Element emOffset = createElementIso(document, "Offset");
			    emOffset.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getOffset())));
			    emMatchingData.appendChild(emOffset);
			}
			if (r.getBody().getMatchingData().getMask() != null) {
			    Element emMask = createElementIso(document, "Mask");
			    emMask.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData().getMask())));
			    emMatchingData.appendChild(emMask);
			}
			if (r.getBody().getMatchingData().getMatchingValue() != null) {
			    Element emMatchingValue = createElementIso(document, "MatchingValue");
			    emMatchingValue.appendChild(document.createTextNode(ByteUtils.toHexString(r.getBody().getMatchingData()
				    .getMatchingValue())));
			    emMatchingData.appendChild(emMatchingValue);
			}
			emBody.appendChild(emMatchingData);
		    }
		    emResponseAPDU.appendChild(emBody);
		}
		if (r.getTrailer() != null) {
		    Element emTrailer = createElementIso(document, "Trailer");
		    emTrailer.appendChild(document.createTextNode(ByteUtils.toHexString(r.getTrailer())));
		    emResponseAPDU.appendChild(emTrailer);
		}
		if (r.getConclusion() != null) {
		    Element emConclusion = createElementIso(document, "Conclusion");
		    if (r.getConclusion().getCardCall() != null) {
			for (CardCall cc : r.getConclusion().getCardCall()) {
			    emConclusion.appendChild(marshalCardCall(cc, document));
			}
		    }
		    if (r.getConclusion().getRecognizedCardType() != null) {
			Element emRecognizedCardType = createElementIso(document, "RecognizedCardType");
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
	Document newDoc = createDoc(n);

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
	    LOG.error("Unable to unmarshal Node element.", e);
	    throw new MarshallingTypeException(e);
	}
    }

    @Override
    public synchronized <T> JAXBElement<T> unmarshal(Node n, Class<T> c) throws MarshallingTypeException,
	    WSMarshallerException {
	Object result = unmarshal(n);
	if (result instanceof JAXBElement) {
	    JAXBElement jaxbElem = (JAXBElement) result;
	    if (jaxbElem.getDeclaredType().equals(c)) {
		return jaxbElem;
	    }
	}
	throw new MarshallingTypeException(String.format("Invalid type requested for unmarshalling: '%s'", c));
    }

    private Document createDoc(Node n) throws WSMarshallerException {
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
	return newDoc;
    }

    private synchronized ResponseAPDUType parseResponseAPDUType(XmlPullParser parser) throws XmlPullParserException,
	    IOException, ParserConfigurationException {
	ResponseAPDUType responseAPDUType = new ResponseAPDUType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Trailer")) {
		    responseAPDUType.setTrailer(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("Body")) {
		    responseAPDUType.setBody(this.parseDataMaskType(parser, "Body"));
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

	int eventType;
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

    private synchronized DataMaskType parseDataMaskType(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
	DataMaskType dataMaskType = new DataMaskType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Tag")) {
		    dataMaskType.setTag(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingData")) {
		    dataMaskType.setMatchingData(this.parseMatchingDataType(parser));
		} else if (parser.getName().equals("DataObject")) {
		    dataMaskType.setDataObject(this.parseDataMaskType(parser, "DataObject"));
		}

	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(endTag)));
	return dataMaskType;
    }

    private MatchingDataType parseMatchingDataType(XmlPullParser parser) throws XmlPullParserException, IOException {
	MatchingDataType matchingDataType = new MatchingDataType();

	int eventType;
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

	int eventType;
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

    private synchronized Object parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParserConfigurationException, DatatypeConfigurationException {
	if (parser.getName().equals("DestroyChannelResponse")) {
	    DestroyChannelResponse destroyChannelResponse = new DestroyChannelResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			destroyChannelResponse.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			destroyChannelResponse.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			destroyChannelResponse.setResult(this.parseResult(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DestroyChannelResponse")));
	    return destroyChannelResponse;
	}
	else if (parser.getName().equals("DestroyChannel")) {
	    DestroyChannel destroyChannel = new DestroyChannel();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			destroyChannel.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DestroyChannel")));
	    return destroyChannel;
	}

	else if (parser.getName().equals("EstablishChannelResponse")) {
	    EstablishChannelResponse establishChannelResponse = new EstablishChannelResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			establishChannelResponse.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			establishChannelResponse.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
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
	    int eventType;
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
	} else if (parser.getName().equals("DIDAuthenticateResponse")) {
	    DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    } if (parser.getName().equals("AuthenticationProtocolData")) {
			response.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthenticateResponse")));
	    return response;
	}

	else if (parser.getName().equals("StartPAOSResponse")) {
	    StartPAOSResponse startPAOSResponse = new StartPAOSResponse();
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	} else if (parser.getName().equals("GetIFDCapabilities")) {
	    GetIFDCapabilities getIFDCapabilities = new GetIFDCapabilities();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ContextHandle")) {
			getIFDCapabilities.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			getIFDCapabilities.setIFDName(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("GetIFDCapabilities")));
	    return getIFDCapabilities;
	} else if (parser.getName().equals("GetIFDCapabilitiesResponse")) {
	    GetIFDCapabilitiesResponse resp = new GetIFDCapabilitiesResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			resp.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			resp.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			resp.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("GetIFDCapabilitiesResponse")) {
			resp.setIFDCapabilities((IFDCapabilitiesType) this.parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("GetIFDCapabilitiesResponse")));
	    return resp;
	} else if (parser.getName().equals("IFDCapabilitiesType")) {
	    IFDCapabilitiesType cap = new IFDCapabilitiesType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("OpticalSignalUnit")) {
			cap.setOpticalSignalUnit(Boolean.getBoolean(parser.nextText()));
		    } else if (parser.getName().equals("AcousticSignalUnit")) {
			cap.setAcousticSignalUnit(Boolean.getBoolean(parser.nextText()));
		    } else if (parser.getName().equals("SlotCapability")) {
			cap.getSlotCapability().add(parseSlotCapability(parser));
		    } else if (parser.getName().equals("DisplayCapability")) {
			cap.getDisplayCapability().add(parseDisplayCapability(parser));
		    } else if (parser.getName().equals("KeyPadCapability")) {
			cap.getKeyPadCapability().add(parseKeyPadCapability(parser));
		    } else if (parser.getName().equals("BioSensorCapability")) {
			cap.getBioSensorCapability().add(parseBioSensorCapability(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("IFDCapabilitiesType")));
	    return cap;

	} else if (parser.getName().equals("BeginTransaction")) {
	    BeginTransaction trans = new BeginTransaction();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			trans.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BeginTransaction")));
	    return trans;
	} else if (parser.getName().equals("BeginTransactionResponse")) {
	    BeginTransactionResponse response = new BeginTransactionResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BeginTransactionResponse")));
	    return response;

	} else if (parser.getName().equals("EndTransaction")) {
	    EndTransaction end = new EndTransaction();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			end.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EndTransaction")));
	    return end;
	} else if (parser.getName().equals("EndTransactionResponse")) {
	    EndTransactionResponse response = new EndTransactionResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EndTransactionResponse")));
	    return response;

	} else if (parser.getName().equals("CardApplicationPath")) {
	    CardApplicationPath path = new CardApplicationPath();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardAppPathRequest")) {
			path.setCardAppPathRequest((CardApplicationPathType) parse(parser));
		    } else if (parser.getName().equals("Profile")) {
			path.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			path.setRequestID(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationPath")));
	    return path;
	} else if (parser.getName().equals("CardAppPathRequest") || parser.getName().equals("CardApplicationPathResult")) {
	    CardApplicationPathType type = new CardApplicationPathType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			type.setChannelHandle((ChannelHandleType) parse(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			type.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			type.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			type.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			type.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardAppPathRequest")));
	    return type;
	} else if (parser.getName().equals("ChannelHandle")) {
	    ChannelHandleType ch = new ChannelHandleType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ProtocolTerminationPoint")) {
			ch.setProtocolTerminationPoint(parser.nextText());
		    } else if (parser.getName().equals("SessionIdentifier")) {
			ch.setSessionIdentifier(parser.nextText());
		    } else if (parser.getName().equals("Binding")) {
			ch.setBinding(parser.nextText());
		    } else if (parser.getName().equals("PathSecurity")) {
			ch.setPathSecurity((PathSecurityType) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("ChannelHandle")));
	    return ch;
	} else if (parser.getName().equals("PathSecurity")) {
	    PathSecurityType p = new PathSecurityType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Protocol")) {
			p.setProtocol(parser.nextText());
		    } else if (parser.getName().equals("Parameters")) {
			p.setParameters((Object) parse(parser)); // TODO this object is an any type
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("PathSecurity")));
	    return p;
	} else if (parser.getName().equals("CardApplicationPathResponse")) {
	    CardApplicationPathResponse resp = new CardApplicationPathResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardAppPathResultSet")) {
			resp.setCardAppPathResultSet((CardApplicationPathResponse.CardAppPathResultSet) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationPathResponse")));
	    return resp;
	} else if (parser.getName().equals("CardAppPathResultSet")) {
	    CardApplicationPathResponse.CardAppPathResultSet result = new CardApplicationPathResponse.CardAppPathResultSet();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardApplicationPathResult")) {
			result.getCardApplicationPathResult().add((CardApplicationPathType) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardAppPathResultSet")));
	    return result;
	} else if (parser.getName().equals("CardApplicationConnect")) {
	    CardApplicationConnect result = new CardApplicationConnect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardApplicationPath")) {
			result.setCardApplicationPath(parseCardApplicationPath(parser));
		    } else if (parser.getName().equals("Output")) {
			result.setOutput((OutputInfoType) parse(parser));
		    } else if (parser.getName().equals("ExclusiveUse")) {
			result.setExclusiveUse(Boolean.getBoolean(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationConnect")));
	    return result;
	} else if (parser.getName().equals("Output")) {
	    OutputInfoType result = new OutputInfoType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Timeout")) {
			result.setTimeout(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("DisplayIndex")) {
			result.setDisplayIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("Message")) {
			result.setMessage(parser.nextText());
		    } else if (parser.getName().equals("AcousticalSignal")) {
			result.setAcousticalSignal(Boolean.getBoolean(parser.nextText()));
		    } else if (parser.getName().equals("OpticalSignal")) {
			result.setOpticalSignal(Boolean.getBoolean(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("Output")));
	    return result;
	} else if (parser.getName().equals("CardApplicationConnectResponse")) {
	    CardApplicationConnectResponse result = new CardApplicationConnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			result.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("ConnectionHandle")) {
			result.setConnectionHandle((ConnectionHandleType) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationConnectResponse")));
	    return result;
	} else if (parser.getName().equals("ConnectionHandle")) {
	    ConnectionHandleType result = new ConnectionHandleType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			result.setChannelHandle((ChannelHandleType) parse(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			result.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			result.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			result.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			result.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("SlotHandle")) {
			result.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("RecognitionInfo")) {
			result.setRecognitionInfo((RecognitionInfo) parse(parser));
		    } else if (parser.getName().equals("SlotInfo")) {
			result.setSlotInfo((SlotInfo) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("ConnectionHandle")));
	    return result;
	} else if (parser.getName().equals("RecognitionInfo")) {
	    RecognitionInfo result = new RecognitionInfo();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardType")) {
			result.setCardType(parser.nextText());
		    } else if (parser.getName().equals("CardIdentifier")) {
			result.setCardIdentifier(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("CaptureTime")) {
			// TODO
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("RecognitionInfo")));
	    return result;
	} else if (parser.getName().equals("SlotInfo")) {
	    SlotInfo result = new SlotInfo();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ProtectedAuthPath")) {
			result.setProtectedAuthPath(Boolean.getBoolean(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotInfo")));
	    return result;
	} else if (parser.getName().equals("CardApplicationDisconnect")) {
	    CardApplicationDisconnect result = new CardApplicationDisconnect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ConnectionHandle")) {
			result.setConnectionHandle(parseConnectionHandle(parser));
		    } else if (parser.getName().equals("Action")) {
			result.setAction(ActionType.fromValue(parser.nextText()));
		    } else if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationDisconnect")));
	    return result;
	} else if (parser.getName().equals("CardApplicationDisconnectResponse")) {
	    CardApplicationDisconnectResponse result = new CardApplicationDisconnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			result.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationDisconnectResponse")));
	    return result;
	} else if (parser.getName().equals("GetRecognitionTreeResponse")) {
	    GetRecognitionTreeResponse resp = new GetRecognitionTreeResponse();
	    RecognitionTree recTree = new RecognitionTree();
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	    int eventType;
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
	} else if (parser.getName().equals("DisconnectResponse")) {
	    DisconnectResponse response = new DisconnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("DisconnectResponse")));
	    return response;

	} else if (parser.getName().equals("Transmit")) {
	    Transmit t = new Transmit();
	    int eventType;
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
	    int eventType;
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
	    // TODO CardIdentification and CardCapabilities are ignored
	    CardInfo cardInfo = new CardInfo();
	    ApplicationCapabilitiesType applicationCapabilities = new ApplicationCapabilitiesType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ObjectIdentifier")) {
			CardTypeType cardType = new CardTypeType();
			cardType.setObjectIdentifier(parser.nextText());
			cardInfo.setCardType(cardType);
		    } else if (parser.getName().equals("ImplicitlySelectedApplication")) {
			try {
			    // TODO iso:Path, see CardInfo_ecard-AT_0-9-0
			    String selectedApplication = parser.nextText();
			    applicationCapabilities.setImplicitlySelectedApplication(StringUtils.toByteArray(selectedApplication));
			} catch (XmlPullParserException ex) {
			}
		    } else if (parser.getName().equals("CardApplication")) {
			applicationCapabilities.getCardApplication().add(this.parseCardApplication(parser));
		    } else if (parser.getName().equals("CardTypeName")) {
			InternationalStringType internationalString = new InternationalStringType();
			String lang = parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
			internationalString.setLang(lang);
			internationalString.setValue(parser.nextText());
			cardInfo.getCardType().getCardTypeName().add(internationalString);
		    } else if (parser.getName().equals("SpecificationBodyOrIssuer")) {
			cardInfo.getCardType().setSpecificationBodyOrIssuer(parser.nextText());
		    } else if (parser.getName().equals("Status")) {
			cardInfo.getCardType().setStatus(parser.nextText());
		    } else if (parser.getName().equals("Date")) {
			// currently not working; see http://code.google.com/p/android/issues/detail?id=14379
			/*String text = parser.nextText();
			XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(text);
			cardInfo.getCardType().setDate(date);*/
		    } else if (parser.getName().equals("Version")) {
			cardInfo.getCardType().setVersion(this.parseVersion(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardInfo")));
	    cardInfo.setApplicationCapabilities(applicationCapabilities);
	    return cardInfo;
	} else if (parser.getName().equals("AddonSpecification")) {
	    AddonSpecification addonBundleDescription = new AddonSpecification();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ID")) {
			addonBundleDescription.setId(parser.nextText());
		    } else if (parser.getName().equals("Version")) {
			addonBundleDescription.setVersion(parser.nextText());
		    } else if (parser.getName().equals("License")) {
			addonBundleDescription.setLicense(parser.nextText());
		    } else if (parser.getName().equals("LocalizedName")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getLocalizedName().add(string);
		    } else if (parser.getName().equals("LocalizedDescription")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getLocalizedDescription().add(string);
		    } else if (parser.getName().equals("About")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getAbout().add(string);
		    } else if (parser.getName().equals("Logo")) {
			addonBundleDescription.setLogo(parser.nextText());
		    } else if (parser.getName().equals("ConfigDescription")) {
			addonBundleDescription.setConfigDescription(parseConfigDescription(parser));
		    } else if (parser.getName().equals("BindingActions")) {
			addonBundleDescription.getBindingActions().addAll(parseBindingActions(parser));
		    } else if (parser.getName().equals("ApplicationActions")) {
			addonBundleDescription.getApplicationActions().addAll(parseApplicationActions(parser));
		    } else if (parser.getName().equals("IFDActions")) {
			addonBundleDescription.getIfdActions().addAll(
				parseProtocolPluginSpecification(parser, "IFDActions"));
		    } else if (parser.getName().equals("SALActions")) {
			addonBundleDescription.getSalActions().addAll(
				parseProtocolPluginSpecification(parser, "SALActions"));
		    } else {
			throw new IllegalArgumentException(parser.getName()
				+ " in AddonSpecification is not supported.");
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AddonSpecification")));
	    return addonBundleDescription;
	} else if (parser.getName().equals("EstablishChannel")) {
	    EstablishChannel result = new EstablishChannel();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			result.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			result.setAuthenticationProtocolData(parseDIDAuthenticationDataType(parser));
		    } else if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else {
			throw new IOException("Unmarshalling of " + parser.getName() + " in EstablishChannel not supported.");
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishChannel")));
	    return result;
	} else {
	    throw new IOException("Unmarshalling of " + parser.getName() + " is not yet supported.");
	}
    }

    private ConnectionHandleType parseConnectionHandle(XmlPullParser parser) throws XmlPullParserException, IOException,
		ParserConfigurationException, DatatypeConfigurationException {
	    ConnectionHandle result = new ConnectionHandle();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			result.setChannelHandle((ChannelHandleType) parse(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			result.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			result.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			result.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			result.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("SlotHandle")) {
			result.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("RecognitionInfo")) {
			result.setRecognitionInfo((RecognitionInfo) parse(parser));
		    } else if (parser.getName().equals("SlotInfo")) {
			result.setSlotInfo((SlotInfo) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("ConnectionHandle")));
	    return result;
    }

    private Collection<? extends ProtocolPluginSpecification> parseProtocolPluginSpecification(XmlPullParser parser,
	    String tagName) throws XmlPullParserException, IOException {
	ArrayList<ProtocolPluginSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ProtocolPluginSpecification")) {
		    list.add(parseProtocolPluginSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(tagName)));
	return list;
    }

    private CardApplicationPathType parseCardApplicationPath(XmlPullParser parser) throws XmlPullParserException,
		IOException, ParserConfigurationException, DatatypeConfigurationException {
	    CardApplicationPathType type = new CardApplicationPathType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			type.setChannelHandle((ChannelHandleType) parse(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			type.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			type.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			type.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			type.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationPath")));
	    return type;
    }

    private SlotCapabilityType parseSlotCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	SlotCapabilityType slotCapType = new SlotCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    slotCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Protocol")) {
		    String protocol = parser.nextText();
		    slotCapType.getProtocol().add(protocol);
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotCapabilityType")));
	return slotCapType;
    }

    private DisplayCapabilityType parseDisplayCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	DisplayCapabilityType displayCapType = new DisplayCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    displayCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Lines")) {
		    displayCapType.setLines(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Columns")) {
		    displayCapType.setColumns(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("VirtualLines")) {
		    displayCapType.setVirtualLines(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("VirtualColumns")) {
		    displayCapType.setVirtualColumns(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("DisplayCapability")));
	return displayCapType;
    }

    private KeyPadCapabilityType parseKeyPadCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	KeyPadCapabilityType keyPadCapType = new KeyPadCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    keyPadCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Keys")) {
		    keyPadCapType.setKeys(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("KeyPadCapability")));
	return keyPadCapType;
    }

    private BioSensorCapabilityType parseBioSensorCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	BioSensorCapabilityType bioCapType = new BioSensorCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    bioCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("BiometricType")) {
		    bioCapType.setBiometricType(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BioSensorCapability")));
	return bioCapType;
    }

    private ProtocolPluginSpecification parseProtocolPluginSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ProtocolPluginSpecification protocolPluginDescription = new ProtocolPluginSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    protocolPluginDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    protocolPluginDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    protocolPluginDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("URI")) {
		    protocolPluginDescription.setUri(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    protocolPluginDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ProtocolPluginSpecification")));
	return protocolPluginDescription;
    }

    private Collection<? extends AppExtensionSpecification> parseApplicationActions(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ArrayList<AppExtensionSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AppExtensionSpecification")) {
		    list.add(parseAppExtensionSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ApplicationActions")));
	return list;
    }

    private AppExtensionSpecification parseAppExtensionSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	AppExtensionSpecification appExtensionActionDescription = new AppExtensionSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    appExtensionActionDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appExtensionActionDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appExtensionActionDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("ID")) {
		    appExtensionActionDescription.setId(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    appExtensionActionDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AppExtensionSpecification")));
	return appExtensionActionDescription;
    }

    private Collection<? extends AppPluginSpecification> parseBindingActions(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ArrayList<AppPluginSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AppPluginSpecification")) {
		    list.add(parseAppPluginSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("BindingActions")));
	return list;
    }

    private AppPluginSpecification parseAppPluginSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	AppPluginSpecification appPluginActionDescription = new AppPluginSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    appPluginActionDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appPluginActionDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appPluginActionDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("ResourceName")) {
		    appPluginActionDescription.setResourceName(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    appPluginActionDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AppPluginSpecification")));
	return appPluginActionDescription;
    }

    private Configuration parseConfigDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
	Configuration c = new Configuration();
	List<ConfigurationEntry> entries = c.getEntries();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("EnumEntry")) {
		    entries.add(parseEnumEntry(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ConfigDescription")));
	return c;
    }

    private EnumEntry parseEnumEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	EnumEntry entry = new EnumEntry();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Key")) {
		    entry.setKey(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    entry.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    entry.getLocalizedDescription().add(localizedString);
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EnumEntry")));
	return entry;
    }

    private Version parseVersion(XmlPullParser parser) throws XmlPullParserException, IOException {
	Version version = new Version();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Major")) {
		    version.setMajor(parser.nextText());
		} else if (parser.getName().equals("Minor")) {
		    version.setMinor(parser.nextText());
		} else if (parser.getName().equals("SubMinor")) {
		    version.setSubMinor(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Version")));
	return version;
    }

    private CardApplicationType parseCardApplication(XmlPullParser parser) throws XmlPullParserException, IOException {
	CardApplicationType cardApplication = new CardApplicationType();
	int eventType;
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
		} else if (parser.getName().equals("InterfaceProtocol")) {
		    cardApplication.getInterfaceProtocol().add(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplication")));
	return cardApplication;
    }

    private DataSetInfoType parseDataSetInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DataSetInfoType dataSetInfo = new DataSetInfoType();
	int eventType;
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
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetInfo")));
	return dataSetInfo;
    }

    private PathType parseDataSetPath(XmlPullParser parser) throws XmlPullParserException, IOException {
	PathType path = new PathType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("efIdOrPath")) {
		    path.setEfIdOrPath(StringUtils.toByteArray(parser.nextText()));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetPath")));
	return path;
    }

    private DIDInfoType parseDIDInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDInfoType didInfo = new DIDInfoType();
	int eventType;
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
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDInfo")));
	return didInfo;
    }

    private DifferentialIdentityType parseDifferentialIdentity(XmlPullParser parser) throws XmlPullParserException, IOException {
	DifferentialIdentityType differentialIdentity = new DifferentialIdentityType();
	int eventType;
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
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DifferentialIdentity")));
	return differentialIdentity;
    }

    private DIDMarkerType parseDIDMarkerType(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDMarkerType didMarker = new DIDMarkerType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("PACEMarker")) {
		    didMarker.setPACEMarker((PACEMarkerType) this.parseMarker(parser, PACEMarkerType.class));
		} else if (parser.getName().equals("TAMarker")) {
		    didMarker.setTAMarker((TAMarkerType) this.parseMarker(parser, TAMarkerType.class));
		} else if (parser.getName().equals("CAMarker")) {
		    didMarker.setCAMarker((CAMarkerType) this.parseMarker(parser, CAMarkerType.class));
		} else if (parser.getName().equals("RIMarker")) {
		    didMarker.setRIMarker((RIMarkerType) this.parseMarker(parser, RIMarkerType.class));
		} else if (parser.getName().equals("CryptoMarker")) {
		    didMarker.setCryptoMarker((CryptoMarkerType) this.parseMarker(parser, CryptoMarkerType.class));
		} else if (parser.getName().equals("PinCompareMarker")) {
		    didMarker.setPinCompareMarker((PinCompareMarkerType) this.parseMarker(parser, PinCompareMarkerType.class));
		} else if (parser.getName().equals("RSAAuthMarker")) {
		    didMarker.setRSAAuthMarker((RSAAuthMarkerType) this.parseMarker(parser, RSAAuthMarkerType.class));
		} else if (parser.getName().equals("MutualAuthMarker")) {
		    didMarker.setMutualAuthMarker((MutualAuthMarkerType) this.parseMarker(parser, MutualAuthMarkerType.class));
		} else if (parser.getName().equals("EACMarker")) {
		    didMarker.setEACMarker((EACMarkerType) this.parseMarker(parser, EACMarkerType.class));
		} else {
		    LOG.error(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDMarker")));
	return didMarker;
    }

    private Collection<? extends Element> parseAnyTypes(XmlPullParser parser, String name, String ns, Document d, Boolean firstCall, String[] attribNames, String[] attribValues)
	    throws XmlPullParserException, IOException {
	int eventType;
	List<Element> elements = new ArrayList<>();
	boolean terminalNode = false;
	do {
	    String[] attributeNames = new String[0];
	    String[] attributeValues = new String[0];
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {
		    attributeNames = new String[attributeCount];
		    attributeValues = new String[attributeCount];
		    for (int i = 0; i < attributeCount; i++) {
			attributeNames[i] = parser.getAttributeName(i);
			attributeValues[i] = parser.getAttributeValue(i);
		    }
		}
		elements.addAll(parseAnyTypes(parser, parser.getName(), parser.getNamespace(), d, true, attributeNames, attributeValues));
	    } else if (eventType == XmlPullParser.TEXT) {
		if (parser.getText().trim().length() > 0) {
		    Element em = d.createElementNS(ns, name);
		    em.setTextContent(parser.getText());
		    elements.add(em);
		    terminalNode = true;
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));
	if (!terminalNode && firstCall) {
	    Element test = d.createElementNS(ns, name);
	    for (Element e : elements) {
		test.appendChild(e);
	    }
	    List<Element> elements2 = new ArrayList<>();

	    for (int i = 0; i < attribNames.length; i++) {
		test.setAttribute(attribNames[i], attribValues[i]);
	    }
	    elements2.add(test);
	    return elements2;
	}
	return elements;
    }

    private DIDAbstractMarkerType parseMarker(XmlPullParser parser, Class<? extends DIDAbstractMarkerType> cls) throws XmlPullParserException, IOException {
	try {
	    DIDAbstractMarkerType paceMarker = cls.newInstance();
	    paceMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	    Document d = documentBuilder.newDocument();
	    String name = cls.getSimpleName().replace("Type", "");
	    paceMarker.getAny().addAll(parseAnyTypes(parser, name, parser.getNamespace(), d, false, new String[0], new String[0]));
	    return paceMarker;
	} catch (InstantiationException | IllegalAccessException e) {
	    throw new IOException("Error while instantiating the abstract marker type.");
	}
    }

    private AccessControlListType parseACL(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
	AccessControlListType accessControlList = new AccessControlListType();
	int eventType;
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
	int eventType;
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
		} else {
		    throw new IOException("not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AccessRule")));
	return accessRule;
    }

    private SecurityConditionType parseSecurityCondition(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType securityCondition = new SecurityConditionType();
	int eventType;
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
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("SecurityCondition")));
	return securityCondition;
    }

    private DIDAuthenticationStateType parseDIDAuthenticationState(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDAuthenticationStateType didAuthenticationState = new DIDAuthenticationStateType();
	int eventType;
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
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthentication")));
	return didAuthenticationState;
    }

    private Or parseSecurityConditionTypeOr(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType.Or securityConditionOr = new SecurityConditionType.Or();
	int eventType;
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
	int eventType;
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
	int eventType;
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
	int eventType;
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
	DIDAuthenticationDataType didAuthenticationDataType;
	String attrValue = parser.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
	if (attrValue != null && attrValue.contains("EAC1InputType")) {
	    didAuthenticationDataType = new EAC1InputType();
	} else if (attrValue != null && attrValue.contains("EAC2InputType")) {
	    didAuthenticationDataType = new EAC2InputType();
	} else if (attrValue != null && attrValue.contains("EACAdditionalInputType")) {
	    didAuthenticationDataType = new EACAdditionalInputType();
	} else {
	    didAuthenticationDataType = new DIDAuthenticationDataType();
	}

	if (parser.getAttributeValue(null, "Protocol") != null && !parser.getAttributeValue(null, "Protocol").isEmpty()) {
	    didAuthenticationDataType.setProtocol(parser.getAttributeValue(null, "Protocol"));
	}

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		Element em = createElementIso(document, parser.getName());
		em.setTextContent(parser.nextText());
		didAuthenticationDataType.getAny().add(em);
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AuthenticationProtocolData")));

	return didAuthenticationDataType;
    }

    private Result parseResult(XmlPullParser parser) throws XmlPullParserException, IOException {
	Result r = new Result();
	int eventType;
	if (parser == null) {
	    return r;
	}
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
		    String lang = parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
		    internationalStringType.setLang(lang);
		    // TODO problem with parsing result message (international string)
		    try {
			String value = parser.nextText();
			internationalStringType.setValue(value);
			r.setResultMessage(internationalStringType);
		    } catch (Exception e) {}
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("Result")));
	return r;
    }

    private IFDStatusType parseIFDStatusType(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
	IFDStatusType ifdStatusType = new IFDStatusType();

	int eventType;
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

	int eventType;
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

	int eventType;
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
