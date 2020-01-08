/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal.protocol.eac.gui;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openecard.addon.Context;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.sal.util.CardConnectorUtil;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.util.SysUtils;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;


/**
 *
 * @author Tobias Wich
 */
public class PaceCardHelper {

    private static final String NPA_TYPE = "http://bsi.bund.de/cif/npa.xml";

    private final Context ctx;
    private ConnectionHandleType conHandle;

    public PaceCardHelper(Context ctx, ConnectionHandleType conHandle) {
	this.ctx = ctx;
	this.conHandle = conHandle;
    }

    public boolean isConnected() {
	return conHandle.getSlotHandle() != null;
    }

    public PACEMarkerType getPaceMarker(String pinType) throws WSHelper.WSException {
	if (isConnected()) {
	    return getPaceMarkerFromSal(pinType);
	} else {
	    return getPaceMarkerFromCif(pinType);
	}
    }

    private PACEMarkerType getPaceMarkerFromSal(String pinType) throws WSHelper.WSException {
	DIDGet dg = new DIDGet();
	dg.setConnectionHandle(conHandle);
	dg.setDIDName(pinType);
	DIDGetResponse dgr = (DIDGetResponse) ctx.getDispatcher().safeDeliver(dg);
	WSHelper.checkResult(dgr);

	DIDStructureType didStructure = dgr.getDIDStructure();
	iso.std.iso_iec._24727.tech.schema.PACEMarkerType didMarker;
	didMarker = (iso.std.iso_iec._24727.tech.schema.PACEMarkerType) didStructure.getDIDMarker();
	return new PACEMarkerType(didMarker);
    }

    private PACEMarkerType getPaceMarkerFromCif(String pinType) {
	CardInfoType cif = ctx.getRecognition().getCardInfo(NPA_TYPE);
	for (CardApplicationType app : cif.getApplicationCapabilities().getCardApplication()) {
	    for (DIDInfoType did : app.getDIDInfo()) {
		if (pinType.equals(did.getDifferentialIdentity().getDIDName())) {
		    // convert marker
		    iso.std.iso_iec._24727.tech.schema.PACEMarkerType marker;
		    marker = did.getDifferentialIdentity().getDIDMarker().getPACEMarker();
		    PACEMarkerType wrappedMarker = new PACEMarkerType(marker);
		    return wrappedMarker;
		}
	    }
	}

	// nothing found, this means the code is just wrong
	String msg = String.format("The requested DID=%s is not available in the nPA CIF.", pinType);
	throw new IllegalArgumentException(msg);
    }


    /**
     * Check if the selected card reader supports PACE.
     * In that case, the reader is a standard or comfort reader.
     *
     * @return true when card reader supports genericPACE, false otherwise.
     * @throws WSHelper.WSException
     */
    public boolean isNativePinEntry() throws WSHelper.WSException {
	// Request terminal capabilities
	GetIFDCapabilities capabilitiesRequest = new GetIFDCapabilities();
	capabilitiesRequest.setContextHandle(conHandle.getContextHandle());
	capabilitiesRequest.setIFDName(conHandle.getIFDName());
	GetIFDCapabilitiesResponse capabilitiesResponse = (GetIFDCapabilitiesResponse) ctx.getDispatcher().safeDeliver(capabilitiesRequest);
	WSHelper.checkResult(capabilitiesResponse);

	if (capabilitiesResponse.getIFDCapabilities() != null) {
	    List<SlotCapabilityType> capabilities = capabilitiesResponse.getIFDCapabilities().getSlotCapability();
	    // Check all capabilities for generic PACE
	    final String genericPACE = PACECapabilities.PACECapability.GenericPACE.getProtocol();
	    for (SlotCapabilityType capability : capabilities) {
		if (capability.getIndex().equals(conHandle.getSlotIndex())) {
		    for (String protocol : capability.getProtocol()) {
			if (protocol.equals(genericPACE)) {
			    return true;
			}
		    }
		}
	    }
	}

	// No PACE capability found
	return false;
    }

    public EacPinStatus getPinStatus() throws WSHelper.WSException {
	InputAPDUInfoType input = new InputAPDUInfoType();
	input.setInputAPDU(new byte[]{(byte) 0x00, (byte) 0x22, (byte) 0xC1, (byte) 0xA4, (byte) 0x0F, (byte) 0x80,
	    (byte) 0x0A, (byte) 0x04, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x07, (byte) 0x02, (byte) 0x02,
	    (byte) 0x04, (byte) 0x02, (byte) 0x02, (byte) 0x83, (byte) 0x01, (byte) 0x03});
	input.getAcceptableStatusCode().addAll(EacPinStatus.getCodes());

	Transmit transmit = new Transmit();
	transmit.setSlotHandle(conHandle.getSlotHandle());
	transmit.getInputAPDUInfo().add(input);

	TransmitResponse pinCheckResponse = (TransmitResponse) ctx.getDispatcher().safeDeliver(transmit);
	WSHelper.checkResult(pinCheckResponse);
	byte[] output = pinCheckResponse.getOutputAPDU().get(0);
	CardResponseAPDU outputApdu = new CardResponseAPDU(output);
	byte[] status = outputApdu.getStatusBytes();
	return EacPinStatus.fromCode(status);
    }

    public ConnectionHandleType connectCardIfNeeded() throws WSHelper.WSException, InterruptedException {
	// connect card and update handle of this instance
	if (isConnected()) {
	    return conHandle;
	} else {
	    // signal cards to be activated
	    PrepareDevices pdreq = new PrepareDevices();
	    pdreq.setContextHandle(conHandle.getContextHandle());
	    PrepareDevicesResponse response = (PrepareDevicesResponse)ctx.getDispatcher().safeDeliver(pdreq);
	    WSHelper.checkResult(response);
	    final String sessionIdentifier = conHandle.getChannelHandle().getSessionIdentifier();

	    Set<String> npaType = new HashSet<>();
	    npaType.add(NPA_TYPE);
	    // wait for eid card
	    CardConnectorUtil connectorUtil = new CardConnectorUtil(ctx.getDispatcher(), ctx.getEventDispatcher(), npaType,
		    sessionIdentifier, conHandle.getContextHandle(), conHandle.getIFDName());
	    CardApplicationPathType path = connectorUtil.waitForCard();
	    ChannelHandleType channelHandle = path.getChannelHandle();
	    if (channelHandle == null) {
		channelHandle = new ChannelHandleType();
		path.setChannelHandle(channelHandle);
	    }
	    if (channelHandle.getSessionIdentifier() == null) {
		channelHandle.setSessionIdentifier(sessionIdentifier);
	    }

	    // connect eid card
	    CardApplicationConnect conReq = new CardApplicationConnect();
	    conReq.setCardApplicationPath(path);
	    conReq.setExclusiveUse(Boolean.TRUE);
	    CardApplicationConnectResponse conRes = (CardApplicationConnectResponse) ctx.getDispatcher().safeDeliver(conReq);
	    WSHelper.checkResult(conRes);
	    this.conHandle = conRes.getConnectionHandle();
	    return conHandle;
	}
    }

    public void disconnectIfMobile() {
	if (SysUtils.isMobileDevice()) {
	    CardApplicationDisconnect disc = new CardApplicationDisconnect();
	    disc.setConnectionHandle(conHandle);
	    CardApplicationDisconnectResponse discr = (CardApplicationDisconnectResponse) ctx.getDispatcher().safeDeliver(disc);
	}
    }

    public ConnectionHandleType getMobileReader() throws WSHelper.WSException {
	// LstIFD shound be fine for that
	ListIFDs li = new ListIFDs();
	byte[] ctxHandle = ctx.getIfdCtx().get(0);
	li.setContextHandle(ctxHandle);
	ListIFDsResponse lir = (ListIFDsResponse) ctx.getDispatcher().safeDeliver(li);
	WSHelper.checkResult(lir);
	String ifd = lir.getIFDName().get(0);

	ConnectionHandleType newHandle = HandlerUtils.copyHandle(conHandle);
	newHandle.setContextHandle(ctxHandle);
	newHandle.setIFDName(ifd);
	return newHandle;
    }

}
