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

package org.openecard.client.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import iso.std.iso_iec._24727.tech.schema.InputUnitType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import iso.std.iso_iec._24727.tech.schema.PinInputType;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import java.math.BigInteger;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.PinCompareDIDAuthenticateInputType;
import org.openecard.client.common.sal.anytype.PinCompareDIDAuthenticateOutputType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;


/**
 *
 * @author Dirk Petrautzki <petrautzki at hs-coburg.de>
 */
public class DIDAuthenticateStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger _logger = LogManager.getLogger(DIDAuthenticateStep.class.getName());

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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)");
	} // </editor-fold>
	try {
	    String didName = didAuthenticate.getDIDName();
	    PinCompareDIDAuthenticateInputType pinCompareDIDAuthenticateInput = new PinCompareDIDAuthenticateInputType(didAuthenticate.getAuthenticationProtocolData());
	    ConnectionHandleType connectionHandle = didAuthenticate.getConnectionHandle();
	    DIDScopeType didScope = didAuthenticate.getDIDScope();
	    CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    if(!cardInfoWrapper.checkSecurityCondition(didName, didScope, DifferentialIdentityServiceActionName.DID_AUTHENTICATE)){
		return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }
	    DIDStructureType didStructure = cardInfoWrapper.getDIDStructure(didName, didScope);

	    org.openecard.client.common.sal.anytype.PinCompareMarkerType pinCompareMarker = new org.openecard.client.common.sal.anytype.PinCompareMarkerType((PinCompareMarkerType)didStructure.getDIDMarker());

	    VerifyUser verify = new VerifyUser();
	    verify.setSlotHandle(connectionHandle.getSlotHandle());
	    InputUnitType inputUnit = new InputUnitType();
	    verify.setInputUnit(inputUnit);
	    PinInputType pinInput = new PinInputType();
	    inputUnit.setPinInput(pinInput);
	    pinInput.setIndex(BigInteger.ZERO);
	    pinInput.setPasswordAttributes(pinCompareMarker.getPasswordAttributes());
	    // FIXME
	    verify.setTemplate(StringUtils.toByteArray("00 20 00 02", true));
	    VerifyUserResponse verifyR = (VerifyUserResponse) dispatcher.deliver(verify);
	    byte[] responseCode = verifyR.getResponse();

	    DIDAuthenticateResponse did = new DIDAuthenticateResponse();
	    did.setResult(WSHelper.makeResultOK());
	    PinCompareDIDAuthenticateOutputType output = pinCompareDIDAuthenticateInput.getOutputType();

	    // If user verification failed, this contains the current value of the RetryCounter.
	    if(!ByteUtils.compare(responseCode,new byte[] {(byte) 0x90, 0x00})){
	       output.setRetryCounter(new BigInteger(Integer.toString((responseCode[1]&0x0F))));
	    }

	    did.setResult(WSHelper.makeResultOK());
	    cardInfoWrapper.addAuthenticated(didName, didScope);
	    did.setAuthenticationProtocolData(output.getAuthDataType());
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)", did);
	    } // </editor-fold>
	    return did;
	} catch (Exception e) {
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "cardApplicationPath(CardApplicationPath cardApplicationPath)",
			e.getMessage(), e);
	    }
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResult(e));
	}
    }

}
