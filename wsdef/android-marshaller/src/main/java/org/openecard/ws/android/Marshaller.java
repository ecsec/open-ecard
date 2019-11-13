/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import generated.TCTokenType;
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
import iso.std.iso_iec._24727.tech.schema.CardCall;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CreateSession;
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.DestroySession;
import iso.std.iso_iec._24727.tech.schema.DestroySessionResponse;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.EAC1InputType;
import iso.std.iso_iec._24727.tech.schema.EAC1OutputType;
import iso.std.iso_iec._24727.tech.schema.EAC2InputType;
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType;
import iso.std.iso_iec._24727.tech.schema.EACAdditionalInputType;
import iso.std.iso_iec._24727.tech.schema.EACMarkerType;
import iso.std.iso_iec._24727.tech.schema.EmptyResponseDataType;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.MutualAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.OutputInfoType;
import iso.std.iso_iec._24727.tech.schema.PACEMarkerType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.RIMarkerType;
import iso.std.iso_iec._24727.tech.schema.RSAAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.TAMarkerType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.RequestBaseType;
import oasis.names.tc.dss._1_0.core.schema.ResponseBaseType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.util.ByteUtils;
import static org.openecard.ws.android.AndroidMarshaller.*;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * @author Tobias Wich
 */
public class Marshaller {

    private static final Logger LOG = LoggerFactory.getLogger(Marshaller.class);

    private final DocumentBuilder documentBuilder;

    Marshaller(DocumentBuilder documentBuilder) {
	this.documentBuilder = documentBuilder;
    }


    public Document marshal(Object o) throws MarshallingTypeException {
	Document document = documentBuilder.newDocument();
	document.setXmlStandalone(true);

	Element rootElement = null;

	if (o instanceof DestroyChannel) {
	    DestroyChannel destroyChannel = (DestroyChannel) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(destroyChannel, rootElement);
	    Element emSlotHandle = createElementIso(document, "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(destroyChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	} else if (o instanceof DestroyChannelResponse) {
	    DestroyChannelResponse response = (DestroyChannelResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(response, rootElement, document);

	} else if (o instanceof EstablishChannel) {
	    EstablishChannel establishChannel = (EstablishChannel) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(establishChannel, rootElement);

	    Element emSlotHandle = createElementIso(document, "SlotHandle");
	    emSlotHandle.appendChild(document.createTextNode(ByteUtils.toHexString(establishChannel.getSlotHandle())));
	    rootElement.appendChild(emSlotHandle);

	    Element emAuthProtData = marshalAuthProtocolData(establishChannel.getAuthenticationProtocolData(), document);

	    for (Element e : establishChannel.getAuthenticationProtocolData().getAny()) {
		Element eClone = createElementIso(document, e.getLocalName());
		eClone.setTextContent(e.getTextContent());
		emAuthProtData.appendChild(eClone);
	    }

	    rootElement.appendChild(emAuthProtData);
	} else if (o instanceof EstablishChannelResponse) {
	    EstablishChannelResponse response = (EstablishChannelResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(response, rootElement, document);

	    if (response.getAuthenticationProtocolData() != null) {
		Element emAuthProtData = marshalAuthProtocolData(response.getAuthenticationProtocolData(), document);

		for (Element e : response.getAuthenticationProtocolData().getAny()) {
		    Element eClone = createElementIso(document, e.getLocalName());
		    eClone.setTextContent(e.getTextContent());
		    emAuthProtData.appendChild(eClone);
		}
	    }
	} else if (o instanceof DIDAuthenticate) {
	    DIDAuthenticate auth = (DIDAuthenticate) o;
	    rootElement = marshalDidAuthenticate(auth, document);
	} else if (o instanceof DIDAuthenticateResponse) {
	    DIDAuthenticateResponse didAuthenticateResponse = (DIDAuthenticateResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(didAuthenticateResponse, rootElement, document);

	    if (didAuthenticateResponse.getAuthenticationProtocolData() != null) {
		DIDAuthenticationDataType didAuthenticationDataType = didAuthenticateResponse.getAuthenticationProtocolData();

		Element elemEACOutput = marshalAuthProtocolData(didAuthenticationDataType, document);
		for (Element e : didAuthenticationDataType.getAny()) {
		    Element elemCopy = createElementIso(document, e.getLocalName());
		    elemCopy.setTextContent(e.getTextContent());
		    elemEACOutput.appendChild(elemCopy);
		}

		rootElement.appendChild(elemEACOutput);

	    } // else only the result (with error) is returned
	} else if (o instanceof InitializeFramework) {
	    InitializeFramework initializeFramework = (InitializeFramework) o;
	    rootElement = createElementEcapi(document, "InitializeFramework");
	    appendRequestValues(initializeFramework, rootElement);
	} else if (o instanceof InitializeFrameworkResponse) {
	    InitializeFrameworkResponse initializeFrameworkResponse = (InitializeFrameworkResponse) o;
	    rootElement = marshalInitializeFrameworkResponse(initializeFrameworkResponse, document);
	} else if (o instanceof Result) {
	    Result r = (Result) o;
	    rootElement = marshalResult(r, document);
	} else if (o instanceof iso.std.iso_iec._24727.tech.schema.StartPAOS) {
	    StartPAOS startPAOS = (StartPAOS) o;
	    rootElement = marshalStartPAOS(startPAOS, document);
	} else if (o instanceof TransmitResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    TransmitResponse transmitResponsePOJO = (TransmitResponse) o;
	    appendResponseValues(transmitResponsePOJO, rootElement, document);

	    for (int i = 0; i < transmitResponsePOJO.getOutputAPDU().size(); i++) {
		Element em = createElementIso(document, "OutputAPDU");
		em.appendChild(document.createTextNode(ByteUtils.toHexString(transmitResponsePOJO.getOutputAPDU().get(i))));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof EstablishContext) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    EstablishContext req = (EstablishContext) o;
	    appendRequestValues(req, rootElement);
	} else if (o instanceof EstablishContextResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    EstablishContextResponse establishContextResponse = (EstablishContextResponse) o;
	    appendResponseValues(establishContextResponse, rootElement, document);

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(establishContextResponse.getContextHandle())));
	    rootElement.appendChild(em);

	} else if (o instanceof GetStatus) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    GetStatus getStatus = (GetStatus) o;
	    appendRequestValues(getStatus, rootElement);

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(getStatus.getContextHandle())));
	    rootElement.appendChild(em);
	    if (getStatus.getIFDName() != null) {
		em = createElementIso(document, "IFDName");
		em.appendChild(document.createTextNode(getStatus.getIFDName()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Wait) {
	    Wait w = (Wait) o;
	    rootElement = marshalWait(w, document);

	} else if (o instanceof Connect) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    Connect c = (Connect) o;
	    appendRequestValues(c, rootElement);

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
	    appendResponseValues(cr, rootElement, document);

	    if (cr.getSlotHandle() != null) {
		Element em = createElementIso(document, "SlotHandle");
		em.appendChild(document.createTextNode(ByteUtils.toHexString(cr.getSlotHandle())));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof ListIFDs) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    ListIFDs c = (ListIFDs) o;
	    appendRequestValues(c, rootElement);

	    Element em = createElementIso(document, "ContextHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(c.getContextHandle())));
	    rootElement.appendChild(em);

	} else if (o instanceof ListIFDsResponse) {
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    ListIFDsResponse listIFDsResponse = (ListIFDsResponse) o;
	    appendResponseValues(listIFDsResponse, rootElement, document);

	    for (String s : listIFDsResponse.getIFDName()) {
		Element em = createElementIso(document, "IFDName");
		em.appendChild(document.createTextNode(s));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof Transmit) {
	    Transmit t = (Transmit) o;
	    rootElement = marshalTransmit(t, document);
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
	    appendRequestValues(d, rootElement);

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
	    appendResponseValues(response, rootElement, document);

	} else if (o instanceof GetIFDCapabilities) {
	    GetIFDCapabilities getIFDCapabilities = (GetIFDCapabilities) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(getIFDCapabilities, rootElement);

	    Element emContextHandle = createElementIso(document, "ContextHandle");
	    emContextHandle.appendChild(document.createTextNode(ByteUtils.toHexString(getIFDCapabilities.getContextHandle())));
	    rootElement.appendChild(emContextHandle);
	    Element emIFDName = createElementIso(document, "IFDName");
	    emIFDName.appendChild(document.createTextNode(getIFDCapabilities.getIFDName()));
	    rootElement.appendChild(emIFDName);
	} else if (o instanceof GetIFDCapabilitiesResponse) {
	    GetIFDCapabilitiesResponse response = (GetIFDCapabilitiesResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(response, rootElement, document);

	    if (response.getIFDCapabilities() != null) {
		Element emIFDCaps = marshalIFDCapabilities(response.getIFDCapabilities(), document);
		rootElement.appendChild(emIFDCaps);
	    }

	} else if (o instanceof TCTokenType) {
	    TCTokenType tctoken = (TCTokenType) o;
	    rootElement = marshalTCToken(tctoken, document);
	} else if (o instanceof CardApplicationPath) {
	    CardApplicationPath p = (CardApplicationPath) o;
	    rootElement = marshalCardApplicationPath(p, document);

	} else if (o instanceof CardApplicationPathResponse) {
	    CardApplicationPathResponse resp = (CardApplicationPathResponse) o;
	    rootElement = createElementIso(document, "CardApplicationPathResponse");
	    appendResponseValues(resp, rootElement, document);

	    Element em = createElementIso(document, "CardAppPathResultSet");
	    for (CardApplicationPathType path : resp.getCardAppPathResultSet().getCardApplicationPathResult()) {
		em.appendChild(marshalCardApplicationPathResult(path, document, "CardAppPathRequest"));
	    }
	    rootElement.appendChild(em);
	} else if (o instanceof BeginTransaction) {
	    BeginTransaction t = (BeginTransaction) o;
	    rootElement = createElementIso(document, "BeginTransaction");
	    appendRequestValues(t, rootElement);

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(t.getSlotHandle())));
	    rootElement.appendChild(em);
	} else if (o instanceof BeginTransactionResponse) {
	    BeginTransactionResponse response = (BeginTransactionResponse) o;
	    rootElement = createElementIso(document, "BeginTransactionResponse");
	    appendResponseValues(response, rootElement, document);

	} else if (o instanceof EndTransaction) {
	    EndTransaction end = (EndTransaction) o;
	    rootElement = createElementIso(document, "EndTransaction");
	    appendRequestValues(end, rootElement);

	    Element em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(end.getSlotHandle())));
	    rootElement.appendChild(em);
	} else if (o instanceof EndTransactionResponse) {
	    EndTransactionResponse response = (EndTransactionResponse) o;
	    rootElement = createElementIso(document, "EndTransactionResponse");
	    appendResponseValues(response, rootElement, document);
	} else if (o instanceof CreateSession) {
	    CreateSession cs = (CreateSession) o;
	    rootElement = createElementIso(document, "CreateSession");
	    appendRequestValues(cs, rootElement);

	    if (cs.getSessionIdentifier() != null) {
		Element sessChild = createElementIso(document, "SessionIdentifier");
		sessChild.appendChild(document.createTextNode(cs.getSessionIdentifier()));
		rootElement.appendChild(sessChild);
	    }
	} else if (o instanceof CreateSessionResponse) {
	    CreateSessionResponse resp = (CreateSessionResponse) o;
	    rootElement = createElementIso(document, "CreateSessionResponse");
	    appendResponseValues(resp, rootElement, document);

	    ConnectionHandleType ch = resp.getConnectionHandle();
	    if (ch != null) {
		Element em = marshalConnectionHandle(ch, document);
		rootElement.appendChild(em);
	    }
	} else if (o instanceof DestroySession) {
	    DestroySession ds = (DestroySession) o;
	    rootElement = createElementIso(document, "DestroySession");
	    appendRequestValues(ds, rootElement);

	    if (ds.getConnectionHandle() != null) {
		rootElement.appendChild(marshalConnectionHandle(ds.getConnectionHandle(), document));
	    }
	} else if (o instanceof DestroySessionResponse) {
	    DestroySessionResponse resp = (DestroySessionResponse) o;
	    rootElement = createElementIso(document, "DestroySessionResponse");
	    appendResponseValues(resp, rootElement, document);

	} else if (o instanceof CardApplicationConnect) {
	    CardApplicationConnect c = (CardApplicationConnect) o;
	    rootElement = createElementIso(document, "CardApplicationConnect");
	    appendRequestValues(c, rootElement);

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
	    appendResponseValues(resp, rootElement, document);

	    ConnectionHandleType ch = resp.getConnectionHandle();
	    if (ch != null) {
		Element em = marshalConnectionHandle(ch, document);
		rootElement.appendChild(em);
	    }
	} else if (o instanceof CardApplicationDisconnect) {
	    CardApplicationDisconnect c = (CardApplicationDisconnect) o;
	    rootElement = createElementIso(document, "CardApplicationDisconnect");
	    appendRequestValues(c, rootElement);

	    if (c.getConnectionHandle() != null) {
		rootElement.appendChild(marshalConnectionHandle(c.getConnectionHandle(), document));
	    }

	    if (c.getAction() != null) {
		Element em = createElementIso(document, "Action");
		em.appendChild(document.createTextNode(c.getAction().value()));
		rootElement.appendChild(em);
	    }

	} else if (o instanceof CardApplicationDisconnectResponse) {
	    CardApplicationDisconnectResponse response = (CardApplicationDisconnectResponse) o;
	    rootElement = createElementIso(document, "CardApplicationDisconnectResponse");
	    appendResponseValues(response, rootElement, document);
	} else if (o instanceof PrepareDevices) {
	    PrepareDevices prepareDevices = (PrepareDevices) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(prepareDevices, rootElement);

	    Element emContextHandle = createElementIso(document, "ContextHandle");
	    emContextHandle.appendChild(document.createTextNode(ByteUtils.toHexString(prepareDevices.getContextHandle())));
	    rootElement.appendChild(emContextHandle);

	} else if (o instanceof PrepareDevicesResponse) {
	    PrepareDevicesResponse resp = (PrepareDevicesResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(resp, rootElement, document);
	} else if (o instanceof PowerDownDevices) {
	    PowerDownDevices powerDownDevices = (PowerDownDevices) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(powerDownDevices, rootElement);

	    Element emContextHandle = createElementIso(document, "ContextHandle");
	    emContextHandle.appendChild(document.createTextNode(ByteUtils.toHexString(powerDownDevices.getContextHandle())));
	    rootElement.appendChild(emContextHandle);

	} else if (o instanceof PowerDownDevicesResponse) {
	    PowerDownDevicesResponse resp = (PowerDownDevicesResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(resp, rootElement, document);
	} else if (o instanceof DIDGet) {
	    DIDGet dIDGet = (DIDGet)o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendRequestValues(dIDGet, rootElement);

	    if (dIDGet.getConnectionHandle() != null) {
		rootElement.appendChild(marshalConnectionHandle(dIDGet.getConnectionHandle(), document));
	    }

	    if (dIDGet.getDIDName() != null) {
		Element e = createElementIso(document, "DIDName");
		e.appendChild(document.createTextNode(dIDGet.getDIDName()));
		rootElement.appendChild(e);
	    }
	    if (dIDGet.getDIDScope() != null) {
		Element e = createElementIso(document, "DIDScope");
		e.appendChild(document.createTextNode(dIDGet.getDIDScope().value()));
		rootElement.appendChild(e);
	    }
	} else if (o instanceof DIDGetResponse) {
	    DIDGetResponse dIDGetResponse = (DIDGetResponse) o;
	    rootElement = createElementIso(document, o.getClass().getSimpleName());
	    appendResponseValues(dIDGetResponse, rootElement, document);

	    final DIDStructureType didStructure = dIDGetResponse.getDIDStructure();
	    if (didStructure != null) {
		final Element em = marshalDIDStructure(didStructure, document);
		rootElement.appendChild(em);
	    }
	} else {
	    throw new IllegalArgumentException("Cannot marshal " + o.getClass().getSimpleName());
	}

	document.appendChild(rootElement);
	document.normalizeDocument();
	return document;
    }

    private Element marshalConnectionHandle(ConnectionHandleType ch, Document document) {
	Element em = createElementIso(document, "ConnectionHandle");

	Element em2;
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
	if (ch.getContextHandle() != null) {
	    em2 = createElementIso(document, "ContextHandle");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getContextHandle())));
	    em.appendChild(em2);
	}
	if (ch.getIFDName() != null) {
	    em2 = createElementIso(document, "IFDName");
	    em2.appendChild(document.createTextNode(ch.getIFDName()));
	    em.appendChild(em2);
	}
	if (ch.getSlotIndex() != null) {
	    em2 = createElementIso(document, "SlotIndex");
	    em2.appendChild(document.createTextNode(ch.getSlotIndex().toString()));
	    em.appendChild(em2);
	}
	if (ch.getCardApplication() != null) {
	    em2 = createElementIso(document, "CardApplication");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getCardApplication())));
	    em.appendChild(em2);
	}
	if (ch.getSlotHandle() != null) {
	    em2 = createElementIso(document, "SlotHandle");
	    em2.appendChild(document.createTextNode(ByteUtils.toHexString(ch.getSlotHandle())));
	    em.appendChild(em2);
	}
	if (ch.getRecognitionInfo() != null) {
	    em2 = createElementIso(document, "RecognitionInfo");
	    Element em3 = createElementIso(document, "CardType");
	    em3.appendChild(document.createTextNode(ch.getRecognitionInfo().getCardType()));
	    em2.appendChild(em3);
	    em.appendChild(em2);
	}
	if (ch.getSlotInfo() != null) {
	    em.appendChild(marshalSlotInfo(ch.getSlotInfo(), document));
	}

	return em;
    }

    private Element marshalSlotInfo(ConnectionHandleType.SlotInfo si, Document document) {
	Element em = createElementIso(document, "SlotInfo");

	Element em2;
	if (si.isProtectedAuthPath() != null) {
	    em2 = createElementIso(document, "ProtectedAuthPath");
	    em2.appendChild(document.createTextNode(si.isProtectedAuthPath().toString()));
	    em.appendChild(em2);
	}

	return em;
    }

    private Element marshalAuthProtocolData(DIDAuthenticationDataType authData, Document document) {
	Element elemAd = createElementIso(document, "AuthenticationProtocolData");
	elemAd.setAttribute("Protocol", authData.getProtocol());

	for (Map.Entry<QName, String> next : authData.getOtherAttributes().entrySet()) {
	    QName key = next.getKey();
	    String val = next.getValue();

	    if (! key.getNamespaceURI().isEmpty()) {
		String qn = key.getPrefix().isEmpty() ? "" : key.getPrefix() + ":";
		qn += key.getLocalPart();
		elemAd.setAttributeNS(key.getNamespaceURI(), qn, val);
	    } else {
		if (key.getPrefix().isEmpty()) {
		    elemAd.setAttribute(key.getPrefix() + ":" + key.getLocalPart(), val);
		} else {
		    elemAd.setAttribute(key.getLocalPart(), val);
		}
	    }
	}

	// check if we should synthesize the xsi:type element
	boolean synthesize = true;
	for (Map.Entry<QName, String> entries : authData.getOtherAttributes().entrySet()) {
	    QName key = entries.getKey();
	    if (XSI_NS.equals(key.getNamespaceURI()) && "type".equals(key.getLocalPart())) {
		synthesize = false;
	    }
	}
	if (synthesize) {
	    LOG.debug("Synthesizing xsi:type attribute.");
	    if (authData instanceof EAC1InputType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EAC1InputType");
	    } else if (authData instanceof EAC2InputType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EAC2InputType");
	    } else if (authData instanceof EAC1OutputType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EAC1OutputType");
	    } else if (authData instanceof EAC2OutputType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EAC2OutputType");
	    } else if (authData instanceof EACAdditionalInputType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EACAdditionalInputType");
	    } else if (authData instanceof EmptyResponseDataType) {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":EmptyResponseDataType");
	    } else {
		elemAd.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":DIDAuthenticationDataType");
	    }
	}

	return elemAd;
    }

    private void appendRequestValues(RequestBaseType req, Element e) {
	String profile = req.getProfile();
	if (profile != null) {
	    e.setAttribute("Profile", profile);
	}
	String reqId = req.getRequestID();
	if (reqId != null) {
	    e.setAttribute("RequestID", reqId);
	}
    }

    private void appendResponseValues(ResponseBaseType res, Element e, Document document) {
	String profile = res.getProfile();
	if (profile != null) {
	    e.setAttribute("Profile", profile);
	}
	String reqId = res.getRequestID();
	if (reqId != null) {
	    e.setAttribute("RequestID", reqId);
	}

	Result result = res.getResult();
	if (result != null) {
	    Element emResult = marshalResult(result, document);
	    e.appendChild(emResult);
	}
    }

    private Element marshalInternationStringType(InternationalStringType internationalStringType, Document document, String name) {
	Element em = createElementDss(document, name);
	em.appendChild(document.createTextNode(internationalStringType.getValue()));
	em.setAttributeNS(XML_NS, XML_PFX + ":lang", internationalStringType.getLang());
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

    private Element marshalStartPAOS(StartPAOS startPAOS, Document document) {
	Element rootElement = createElementIso(document, "StartPAOS");
	appendRequestValues(startPAOS, rootElement);

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

	return rootElement;
    }

    private Element marshalCardApplicationPathResult(CardApplicationPathType type, Document document, String name) {
	Element em = createElementIso(document, name);

	// ChannelHandle
	ChannelHandleType h = type.getChannelHandle();
	if (h != null) {
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
		emChild.appendChild(emChildOfCH);

		// TODO: parse parameters element
	    }
	}

	// context handle
	byte[] ctxHdl = type.getContextHandle();
	if (ctxHdl != null) {
	    Element emChild = createElementIso(document, "ContextHandle");
	    emChild.appendChild(document.createTextNode(ByteUtils.toHexString(ctxHdl)));
	    em.appendChild(emChild);
	}

	// IFDName
	String ifdName = type.getIFDName();
	if (ifdName != null) {
	    Element emChild = createElementIso(document, "IFDName");
	    emChild.appendChild(document.createTextNode(ifdName));
	    em.appendChild(emChild);
	}

	// SlotIndex
	BigInteger slotIdx = type.getSlotIndex();
	if (slotIdx != null) {
	    Element emChild = createElementIso(document, "SlotIndex");
	    emChild.appendChild(document.createTextNode(slotIdx.toString()));
	    em.appendChild(emChild);
	}

	// Card Application
	byte[] cardApp = type.getCardApplication();
	if (cardApp != null) {
	    Element emChild = createElementIso(document, "CardApplication");
	    emChild.appendChild(document.createTextNode(ByteUtils.toHexString(cardApp)));
	    em.appendChild(emChild);
	}

	return em;
    }

    private Element marshalInitializeFrameworkResponse(InitializeFrameworkResponse res, Document document) {
	Element rootElement = createElementEcapi(document, "InitializeFrameworkResponse");
	appendResponseValues(res, rootElement, document);

	InitializeFrameworkResponse.Version v = res.getVersion();
	Element emVersion = createElementEcapi(document, "Version");
	rootElement.appendChild(emVersion);

	Element emMajor = createElementEcapi(document, "Major");
	emMajor.appendChild(document.createTextNode(v.getMajor().toString()));
	emVersion.appendChild(emMajor);

	BigInteger minor = v.getMinor();
	if (minor != null) {
	    Element emMinor = createElementEcapi(document, "Minor");
	    emMinor.appendChild(document.createTextNode(minor.toString()));
	    emVersion.appendChild(emMinor);
	}

	BigInteger subminor = v.getSubMinor();
	if (subminor != null) {
	    Element emSubMinor = createElementEcapi(document, "SubMinor");
	    emSubMinor.appendChild(document.createTextNode(subminor.toString()));
	    emVersion.appendChild(emSubMinor);
	}

	return rootElement;
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

    private Element marshalResult(Result r, Document document) {
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

    private Node marshalCardCall(CardCall c, Document document) {
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

    private Element marshalCardApplicationPath(CardApplicationPath p, Document document) {
	Element rootElement = createElementIso(document, "CardApplicationPath");
	appendRequestValues(p, rootElement);

	Element em = marshalCardApplicationPathResult(p.getCardAppPathRequest(), document, "CardAppPathRequest");
	rootElement.appendChild(em);

	return rootElement;
    }

    private Element marshalWait(Wait w, Document document) {
	Element rootElement = createElementIso(document, "Wait");
	appendRequestValues(w, rootElement);

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

	return rootElement;
    }

    private Element marshalDidAuthenticate(DIDAuthenticate auth, Document document) {
	Element rootElement = createElementIso(document, "DIDAuthenticate");
	appendRequestValues(auth, rootElement);

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

	    em = marshalAuthProtocolData(d, document);

	    for (Element e : d.getAny()) {
		Element elemCopy = createElementIso(document, e.getLocalName());
		elemCopy.setTextContent(e.getTextContent());
		em.appendChild(elemCopy);
	    }
	    rootElement.appendChild(em);
	}

	if (auth.getSAMConnectionHandle() != null) {
	    em = marshalConnectionHandle(auth.getSAMConnectionHandle(), document);
	    rootElement.appendChild(em);
	}

	return rootElement;
    }

    private Element marshalTransmit(Transmit t, Document document) {
	Element rootElement = createElementIso(document, "Transmit");
	appendRequestValues(t, rootElement);

	Element em;
	if (t.getSlotHandle() != null) {
	    em = createElementIso(document, "SlotHandle");
	    em.appendChild(document.createTextNode(ByteUtils.toHexString(t.getSlotHandle())));
	    rootElement.appendChild(em);
	}

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

	return rootElement;
    }

    private Element marshalTCToken(TCTokenType tctoken, Document document) {
	Element rootElement = document.createElement("TCTokenType");
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

	return rootElement;
    }

    private Element marshalDIDStructure(DIDStructureType didStructure, Document document) throws MarshallingTypeException {
	Element root = createElementIso(document, "DIDStructure");

	{
	    Element em = createElementIso(document, "Authenticated");
	    em.appendChild(document.createTextNode(Boolean.toString(didStructure.isAuthenticated())));
	    root.appendChild(em);
	}
	if (didStructure.getDIDName() != null) {
	    Element em = createElementIso(document, "DIDName");
	    em.appendChild(document.createTextNode(didStructure.getDIDName()));
	    root.appendChild(em);
	}

	if (didStructure.getDIDScope()!= null) {
	    Element em = createElementIso(document, "DIDScope");
	    em.appendChild(document.createTextNode(didStructure.getDIDScope().value()));
	    root.appendChild(em);
	}

	final DIDAbstractMarkerType didMarker = didStructure.getDIDMarker();

	if (didMarker != null) {
	    Element em = marshalDIDMarker(didMarker, document);
	    root.appendChild(em);

	    for (Element e : didMarker.getAny()) {
		Element elemCopy = createElementIso(document, e.getLocalName());
		elemCopy.setTextContent(e.getTextContent());
		em.appendChild(elemCopy);
	    }
	}


	return root;
    }

    private Element marshalDIDMarker(DIDAbstractMarkerType didMarker, Document document) throws MarshallingTypeException {
	Element root = createElementIso(document, "DIDMarker");

	root.setAttribute("Protocol", didMarker.getProtocol());

	for (Map.Entry<QName, String> next : didMarker.getOtherAttributes().entrySet()) {
	    QName key = next.getKey();
	    String val = next.getValue();

	    if (! key.getNamespaceURI().isEmpty()) {
		String qn = key.getPrefix().isEmpty() ? "" : key.getPrefix() + ":";
		qn += key.getLocalPart();
		root.setAttributeNS(key.getNamespaceURI(), qn, val);
	    } else {
		if (key.getPrefix().isEmpty()) {
		    root.setAttribute(key.getPrefix() + ":" + key.getLocalPart(), val);
		} else {
		    root.setAttribute(key.getLocalPart(), val);
		}
	    }
	}

	// check if we should synthesize the xsi:type element
	boolean synthesize = true;
	for (Map.Entry<QName, String> entries : didMarker.getOtherAttributes().entrySet()) {
	    QName key = entries.getKey();
	    if (XSI_NS.equals(key.getNamespaceURI()) && "type".equals(key.getLocalPart())) {
		synthesize = false;
	    }
	}
	if (synthesize) {
	    String abstractType;
	    if (didMarker instanceof PinCompareMarkerType) {
		abstractType = "PinCompareMarkerType";
	    } else if (didMarker instanceof MutualAuthMarkerType) {
		abstractType = "MutualAuthMarkerType";
	    } else if (didMarker instanceof EACMarkerType) {
		abstractType = "EACMarkerType";
	    } else if (didMarker instanceof PACEMarkerType) {
		abstractType = "PACEMarkerType";
		PACEMarkerType marker = (PACEMarkerType)didMarker;

	    } else if (didMarker instanceof CAMarkerType) {
		abstractType = "CAMarkerType";
	    } else if (didMarker instanceof TAMarkerType) {
		abstractType = "TAMarkerType";
	    } else if (didMarker instanceof RIMarkerType) {
		abstractType = "RIMarkerType";
	    } else if (didMarker instanceof RSAAuthMarkerType) {
		abstractType = "RSAAuthMarkerType";
	    } else if (didMarker instanceof CryptoMarkerType) {
		abstractType = "CryptoMarkerType";
	    } else {
		throw new MarshallingTypeException(didMarker.getClass().getCanonicalName() + " is not a supported DIDAbstractMarkerType");
	    }
	    root.setAttributeNS(XSI_NS, XSI_PFX + ":type", ISO_PFX + ":" + abstractType);
	}
	return root;
    }

}
