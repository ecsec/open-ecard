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

package org.openecard.client.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.Map;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.sal.protocol.pincompare.anytype.PinCompareDIDAuthenticateInputType;
import org.openecard.client.sal.protocol.pincompare.anytype.PinCompareDIDAuthenticateOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DIDAuthenticateStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger _logger = LoggerFactory.getLogger(DIDAuthenticateStep.class);

    private final Dispatcher dispatcher;

    public DIDAuthenticateStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
	public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
	public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	try {
	    String didName = didAuthenticate.getDIDName();
	    PinCompareDIDAuthenticateInputType pinCompareDIDAuthenticateInput = new PinCompareDIDAuthenticateInputType(didAuthenticate.getAuthenticationProtocolData());
	    ConnectionHandleType connectionHandle = didAuthenticate.getConnectionHandle();

	    CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");

	    byte[] cardApplication;
	    if (didAuthenticate.getDIDScope()!=null&&didAuthenticate.getDIDScope().equals(DIDScopeType.GLOBAL)) {
		cardApplication = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
	    } else {
		cardApplication = connectionHandle.getCardApplication();
	    }
	    if (!cardStateEntry.checkDIDSecurityCondition(cardApplication, didName, DifferentialIdentityServiceActionName.DID_AUTHENTICATE)) {
		return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplication);

	    org.openecard.client.common.sal.anytype.PinCompareMarkerType pinCompareMarker = new org.openecard.client.common.sal.anytype.PinCompareMarkerType((PinCompareMarkerType) didStructure.getDIDMarker());

	    byte keyRef = pinCompareMarker.getPinRef().getKeyRef()[0];
	    VerifyUser verify = new VerifyUser();
	    verify.setSlotHandle(connectionHandle.getSlotHandle());
	    InputUnitType inputUnit = new InputUnitType();
	    verify.setInputUnit(inputUnit);
	    PinInputType pinInput = new PinInputType();
	    inputUnit.setPinInput(pinInput);
	    pinInput.setIndex(BigInteger.ZERO);
	    pinInput.setPasswordAttributes(pinCompareMarker.getPasswordAttributes());
	    if (pinCompareDIDAuthenticateInput.getPin() == null) {
		// [TR-03112-6] The structure of the template corresponds to the
		// structure of an APDU for the VERIFY command in accordance
		// with [ISO7816-4] (Section 7.5.6).
		verify.setTemplate(new byte[] { 0x00, 0x20, 0x00, keyRef });
	    } else {
		// TODO pin is currently ignored
		verify.setTemplate(new byte[] { 0x00, 0x20, 0x00, keyRef });
	    }
	    VerifyUserResponse verifyR = (VerifyUserResponse) dispatcher.deliver(verify);
	    byte[] responseCode = verifyR.getResponse();

	    DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	    didAuthenticateResponse.setResult(WSHelper.makeResultOK());
	    PinCompareDIDAuthenticateOutputType output = pinCompareDIDAuthenticateInput.getOutputType();

	    /*
	     * If user verification failed, this contains the current value of
	     * the RetryCounter.
	     */
	    if (!ByteUtils.compare(responseCode, new byte[] { (byte) 0x90, 0x00 })) {
		output.setRetryCounter(new BigInteger(Integer.toString((responseCode[1] & 0x0F))));
	    }

	    didAuthenticateResponse.setResult(WSHelper.makeResultOK());
	    cardStateEntry.addAuthenticated(didName, cardApplication);
	    didAuthenticateResponse.setAuthenticationProtocolData(output.getAuthDataType());
	    return didAuthenticateResponse;
	} catch (Exception e) {
	    _logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResult(e));
	}
    }

}
