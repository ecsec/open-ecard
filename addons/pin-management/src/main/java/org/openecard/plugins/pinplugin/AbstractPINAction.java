/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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

package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import org.openecard.addon.sal.SalStateView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.common.sal.util.InsertCardDialog;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.gui.UserConsent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Common superclass for {@code ChangePINAction} and {@code UnblockPINAction}.
 * Bundles methods needed in both actions.
 *
 * @author Dirk Petrautzki
 */
public abstract class AbstractPINAction implements AppExtensionAction {

    // translation and logger
    protected final I18n lang = I18n.getTranslation("pinplugin");
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPINAction.class);

    // constants
    protected static final String GERMAN_IDENTITY_CARD = "http://bsi.bund.de/cif/npa.xml";
    private static final byte[] RECOGNIZE_PIN_APDU = StringUtils.toByteArray("0022C1A40F800A04007F00070202040202830103");
    private static final byte[] RECOGNIZE_PUK_APDU = StringUtils.toByteArray("0022C1A40F800A04007F00070202040202830104");
    private static final byte[] RESPONSE_RC3 = new byte[] { (byte) 0x90, 0x00 };
    private static final byte[] RESPONSE_BLOCKED = new byte[] { (byte) 0x63, (byte) 0xC0 };
    private static final byte[] RESPONSE_SUSPENDED = new byte[] { (byte) 0x63, (byte) 0xC1 };
    private static final byte[] RESPONSE_RC2 = new byte[] { (byte) 0x63, (byte) 0xC2 };
    private static final byte[] RESPONSE_DEACTIVATED = new byte[] { (byte) 0x62, (byte) 0x83 };

    protected Dispatcher dispatcher;
    protected UserConsent gui;
    protected CardRecognition recognition;
    protected EventDispatcher evDispatcher;
    protected SalStateView salStateView;

    /**
     * Recognize the PIN state of the card given through the connection handle.
     *
     * @param cHandle The connection handle for the card for which the pin state should be recognized.
     * @return The recognized State (may be {@code RecognizedState.UNKNOWN}).
     */
    protected RecognizedState recognizeState(ConnectionHandleType cHandle) throws WSException {

	Transmit t = new Transmit();
	t.setSlotHandle(cHandle.getSlotHandle());
	InputAPDUInfoType inputAPDU = new InputAPDUInfoType();
	inputAPDU.setInputAPDU(RECOGNIZE_PIN_APDU);
	t.getInputAPDUInfo().add(inputAPDU);
	TransmitResponse response = (TransmitResponse) dispatcher.safeDeliver(t);

	WSHelper.checkResult(response);

	byte[] responseAPDU = response.getOutputAPDU().get(0);

	RecognizedState state;
	if (ByteUtils.compare(RESPONSE_RC3, responseAPDU)) {
	    state = RecognizedState.PIN_activated_RC3;
	} else if (ByteUtils.compare(RESPONSE_DEACTIVATED, responseAPDU)) {
	    state = RecognizedState.PIN_deactivated;
	} else if (ByteUtils.compare(RESPONSE_RC2, responseAPDU)) {
	    state = RecognizedState.PIN_activated_RC2;
	} else if (ByteUtils.compare(RESPONSE_SUSPENDED, responseAPDU)) {
	    state = RecognizedState.PIN_suspended;
	} else if (ByteUtils.compare(RESPONSE_BLOCKED, responseAPDU)) {
	    state = RecognizedState.PIN_blocked;
	} else {
	    LOG.error("Unhandled response to the PIN state recognition APDU: {}\n");
	    state = RecognizedState.UNKNOWN;
	}

	LOG.info("State of the PIN: {}.", state);
	return state;
    }

    public void getPUKStatus(ConnectionHandleType cHandle) throws WSHelper.WSException {

	Transmit t = new Transmit();
	t.setSlotHandle(cHandle.getSlotHandle());
	InputAPDUInfoType inputAPDU = new InputAPDUInfoType();
	inputAPDU.setInputAPDU(RECOGNIZE_PUK_APDU);
	t.getInputAPDUInfo().add(inputAPDU);
	TransmitResponse response = (TransmitResponse) dispatcher.safeDeliver(t);

	WSHelper.checkResult(response);
	
	byte[] responseAPDU = response.getOutputAPDU().get(0);
	LOG.debug("PUK response is {}", ByteUtils.toHexString(responseAPDU));

    }

    /**
     * Wait until a card of the specified card type was inserted.
     *
     * @param cardType The type of the card that should be inserted.
     * @return The ConnectionHandle of the inserted card or null if no card was inserted.
     */
    protected ConnectionHandleType waitForCardType(String cardType) {
	String cardName = recognition.getTranslatedCardName(cardType);
	Map<String, String> nameAndType = new HashMap<>();
	nameAndType.put(cardName, cardType);
	InsertCardDialog uc = new InsertCardDialog(gui, nameAndType, evDispatcher, salStateView);

	List<ConnectionHandleType> results = uc.show();
	if (results == null || results.isEmpty()) {
	    return null;
	}
	// get(0) should be sufficient we a looking just for one card. i think the possibility to find 2 is very low.
	return results.get(0);
    }

    /**
     * Connect to the root application of the card specified with a connection handle using a empty CardApplicationPath
     * and afterwards a CardApplicationConnect.
     *
     * @param cHandle
     *            The connection handle for the card to connect to root application.
     * @return The updated connection handle (now including a SlotHandle) or null if connecting went wrong.
     * @throws org.openecard.common.WSHelper.WSException
     */
    protected ConnectionHandleType connectToRootApplication(ConnectionHandleType cHandle) throws WSException {

	// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	cardApplicationPath.setCardAppPathRequest(cHandle);
	CardApplicationPathResponse cardApplicationPathResponse =
		(CardApplicationPathResponse) dispatcher.safeDeliver(cardApplicationPath);

	// Check CardApplicationPathResponse

	WSHelper.checkResult(cardApplicationPathResponse);


	if (cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().isEmpty()){
	    LOG.error("CardApplicationPath failed.");
	    Result result = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Card was removed.");
	    WSException ex = WSHelper.createException(result);
	    throw ex;
	}

	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(
		cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse cardApplicationConnectResponse =
		(CardApplicationConnectResponse) dispatcher.safeDeliver(cardApplicationConnect);

	// Check CardApplicationConnectResponse

	WSHelper.checkResult(cardApplicationConnectResponse);

	// Update ConnectionHandle. It now includes a SlotHandle.
	cHandle = cardApplicationConnectResponse.getConnectionHandle();

	return cHandle;
    }

    /**
     * Check if the selected card reader supports PACE.
     * In that case, the reader is a standard or comfort reader.
     *
     * @param connectionHandle Handle describing the IFD and reader.
     * @return true when card reader supports genericPACE, false otherwise.
     * @throws WSException In case request for the terminal capabilities returned an error.
     */
    protected boolean genericPACESupport(ConnectionHandleType connectionHandle) throws WSException {
	// Request terminal capabilities
	GetIFDCapabilities capabilitiesRequest = new GetIFDCapabilities();
	capabilitiesRequest.setContextHandle(connectionHandle.getContextHandle());
	capabilitiesRequest.setIFDName(connectionHandle.getIFDName());
	GetIFDCapabilitiesResponse capabilitiesResponse = (GetIFDCapabilitiesResponse) dispatcher.safeDeliver(capabilitiesRequest);
	WSHelper.checkResult(capabilitiesResponse);

	if (capabilitiesResponse.getIFDCapabilities() != null) {
	    List<SlotCapabilityType> capabilities = capabilitiesResponse.getIFDCapabilities().getSlotCapability();
	    // Check all capabilities for generic PACE
	    final String genericPACE = PACECapabilities.PACECapability.GenericPACE.getProtocol();
	    for (SlotCapabilityType capability : capabilities) {
		if (capability.getIndex().equals(connectionHandle.getSlotIndex())) {
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

}
