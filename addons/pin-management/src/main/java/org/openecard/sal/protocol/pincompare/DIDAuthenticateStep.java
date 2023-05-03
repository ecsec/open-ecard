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

package org.openecard.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.*;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.bouncycastle.util.Arrays;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType;
import org.openecard.common.anytype.pin.PINCompareMarkerType;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.state.ConnectedCardEntry;
import org.openecard.common.sal.state.StateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.util.PINUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;


/**
 * Implements the DIDAuthenticate step of the PIN Compare protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class DIDAuthenticateStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(DIDAuthenticateStep.class);
    private final Dispatcher dispatcher;

    /**
     * Creates a new DIDAuthenticateStep.
     *
     * @param dispatcher Dispatcher
     */
    public DIDAuthenticateStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate request, Map<String, Object> internalData) {
	DIDAuthenticateResponse response = WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultOK());

	char[] rawPIN = null;
	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    String didName = SALUtils.getDIDName(request);
	    StateEntry stateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);
	    ConnectedCardEntry cardStateEntry = stateEntry.getCardEntry();
	    PINCompareDIDAuthenticateInputType pinCompareInput = new PINCompareDIDAuthenticateInputType(request.getAuthenticationProtocolData());
	    PINCompareDIDAuthenticateOutputType pinCompareOutput = pinCompareInput.getOutputType();

	    byte[] cardApplication;
	    if (request.getDIDScope() != null && request.getDIDScope().equals(DIDScopeType.GLOBAL)) {
		cardApplication = cardStateEntry.getCif().getApplicationIdByDidName(request.getDIDName(),
			request.getDIDScope());
	    } else {
		cardApplication = connectionHandle.getCardApplication();
	    }
	    Assert.securityConditionDID(cardStateEntry, cardApplication, didName, DifferentialIdentityServiceActionName.DID_AUTHENTICATE);

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplication);
	    PINCompareMarkerType pinCompareMarker = new PINCompareMarkerType(didStructure.getDIDMarker());
	    byte keyRef = pinCompareMarker.getPINRef().getKeyRef()[0];
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    PasswordAttributesType attributes = pinCompareMarker.getPasswordAttributes();
	    rawPIN = pinCompareInput.getPIN();
	    pinCompareInput.setPIN(null); // delete pin from memory of the structure
	    byte[] template = new byte[] { 0x00, 0x20, 0x00, keyRef };
	    byte[] responseCode;

	    // [TR-03112-6] The structure of the template corresponds to the
	    // structure of an APDU for the VERIFY command in accordance
	    // with [ISO7816-4] (Section 7.5.6).
	    if (rawPIN == null || rawPIN.length == 0) {
		VerifyUser verify = new VerifyUser();
		verify.setSlotHandle(slotHandle);

		InputUnitType inputUnit = new InputUnitType();
		verify.setInputUnit(inputUnit);

		PinInputType pinInput = new PinInputType();
		inputUnit.setPinInput(pinInput);
		pinInput.setIndex(BigInteger.ZERO);
		pinInput.setPasswordAttributes(attributes);

		verify.setTemplate(template);
		VerifyUserResponse verifyR = (VerifyUserResponse) dispatcher.safeDeliver(verify);
		WSHelper.checkResult(verifyR);
		responseCode = verifyR.getResponse();
	    } else {
		Transmit verifyTransmit = PINUtils.buildVerifyTransmit(rawPIN, attributes, template, slotHandle);
		try {
		    TransmitResponse transResp = (TransmitResponse) dispatcher.safeDeliver(verifyTransmit);
		    WSHelper.checkResult(transResp);
		    responseCode = transResp.getOutputAPDU().get(0);
		} finally {
		    // blank PIN APDU
		    for (InputAPDUInfoType apdu : verifyTransmit.getInputAPDUInfo()) {
			byte[] rawApdu = apdu.getInputAPDU();
			if (rawApdu != null) {
			    java.util.Arrays.fill(rawApdu, (byte) 0);
			}
		    }
		}
	    }

	    CardResponseAPDU verifyResponseAPDU = new CardResponseAPDU(responseCode);
	    if (verifyResponseAPDU.isWarningProcessed()) {
		pinCompareOutput.setRetryCounter(new BigInteger(Integer.toString((verifyResponseAPDU.getSW2() & 0x0F))));
	    }

	    cardStateEntry.addAuthenticated(didName, cardApplication);
	    response.setAuthenticationProtocolData(pinCompareOutput.getAuthDataType());
	} catch (ECardException e) {
	    LOG.error(e.getMessage(), e);
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    if (e instanceof RuntimeException) {
		throw (RuntimeException) e;
	    }
	    LOG.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	} finally {
	    if (rawPIN != null) {
		Arrays.fill(rawPIN, ' ');
	    }
	}

	return response;
    }

}
