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
package org.openecard.client.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import java.util.Map;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.TASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.sal.protocol.eac.anytype.EAC2InputType;
import org.openecard.client.sal.protocol.eac.anytype.EAC2OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TerminalAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(TerminalAuthenticationStep.class.getName());
    private Dispatcher dispatcher;

    public TerminalAuthenticationStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.ENTER, "perform");
	// </editor-fold>

	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    EAC2InputType eac2Input = new EAC2InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC2OutputType eac2Output = eac2Input.getOutputType();

	    TerminalAuthentication ta = new TerminalAuthentication(dispatcher, slotHandle);

	    // TA: Step 1 - Verify certificates
	    CardVerifiableCertificateChain certificateChain = (CardVerifiableCertificateChain) internalData.get(EACConstants.INTERNAL_DATA_CERTIFICATES);
	    byte[] currentCAR = (byte[]) internalData.get(EACConstants.CURRENT_CAR);
	    ta.verifyCertificates(certificateChain, currentCAR);

	    // TA: Step 2 - MSE:SET AT
	    SecurityInfos securityInfos = (SecurityInfos) internalData.get(EACConstants.INTERNAL_DATA_SECURITY_INFOS);
	    EFCardAccess efca = new EFCardAccess(securityInfos);
	    TASecurityInfos tas = efca.getTASecurityInfos();

	    byte[] oid = ObjectIdentifierUtils.getValue(tas.getTAInfo().getProtocol());
	    byte[] chr = certificateChain.getTerminalCertificate().getCHR().toByteArray();
	    byte[] key = eac2Input.getEphemeralPublicKey();
	    byte[] aad = (byte[]) internalData.get(EACConstants.INTERNAL_DATA_AUTHENTICATED_AUXILIARY_DATA);
	    ta.mseSetAT(oid, chr, key, aad);

	    // TA: Step 3 - Get challenge
	    byte[] rPICC = ta.getChallenge();

	    // Store public key for Chip Authentication
	    internalData.put(EACConstants.INTERNAL_DATA_PK_PCD, ByteUtils.toHexString(eac2Input.getEphemeralPublicKey()));

	    // Create response
	    eac2Output.setChallenge(rPICC);

	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(eac2Output.getAuthDataType());

	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    logger.trace(LoggingConstants.EXIT, "perform");
	    // </editor-fold>

	    return response;
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }
}
