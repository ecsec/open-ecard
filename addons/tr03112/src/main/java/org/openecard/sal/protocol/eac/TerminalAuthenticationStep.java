/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.ObjectSchemaValidator;
import org.openecard.common.interfaces.ObjectValidatorException;
import org.openecard.common.util.Promise;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.sal.protocol.eac.anytype.EAC2InputType;
import org.openecard.sal.protocol.eac.anytype.EAC2OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements TerminalAuthentication protocol step according to BSI-TR-03112-7.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class TerminalAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(TerminalAuthenticationStep.class.getName());

    private final Dispatcher dispatcher;


    /**
     * Creates a new Terminal Authentication protocol step.
     *
     * @param dispatcher Dispatcher
     */
    public TerminalAuthenticationStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);

	try {
	    ObjectSchemaValidator valid = (ObjectSchemaValidator) dynCtx.getPromise(EACProtocol.SCHEMA_VALIDATOR).deref();

	    boolean messageValid = valid.validateObject(didAuthenticate);
	    if (! messageValid) {
		String msg = "Validation of the EAC2InputType message failed.";
		logger.error(msg);
		dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
		return response;
	    }
	} catch (ObjectValidatorException ex) {
	    String msg = "Validation of the EAC2InputType message failed due to invalid input data.";
	    logger.error(msg, ex);
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    return response;
	} catch (InterruptedException ex) {
	    String msg = "Thread interrupted while waiting for schema validator instance.";
	    logger.error(msg, ex);
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    return response;
	}

	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    EAC2InputType eac2Input = new EAC2InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC2OutputType eac2Output = eac2Input.getOutputType();

	    TerminalAuthentication ta = new TerminalAuthentication(dispatcher, slotHandle);

	    // Build certificate chain
	    CardVerifiableCertificateChain certificateChain;
	    certificateChain = (CardVerifiableCertificateChain) internalData.get(EACConstants.IDATA_CERTIFICATES);
	    certificateChain.addCertificates(eac2Input.getCertificates());

	    byte[] currentCAR = (byte[]) internalData.get(EACConstants.IDATA_CURRENT_CAR);
	    byte[] previousCAR = (byte[]) internalData.get(EACConstants.IDATA_PREVIOUS_CAR);
	    certificateChain = certificateChain.getCertificateChainFromCAR(currentCAR);
	    // try again with previous car if it didn't work
	    if (certificateChain.getCertificates().isEmpty() && previousCAR != null) {
		certificateChain = certificateChain.getCertificateChainFromCAR(previousCAR);
	    }

	    if (certificateChain.getCertificates().isEmpty()) {
		String msg = "Failed to create a valid certificate chain from the transmitted certificates.";
		logger.error(msg);
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
		return response;
	    }

	    // TA: Step 1 - Verify certificates
	    ta.verifyCertificates(certificateChain);

	    // save values for later use
	    CardVerifiableCertificate terminalCertificate = certificateChain.getTerminalCertificate();
	    byte[] key = eac2Input.getEphemeralPublicKey();
	    byte[] signature = eac2Input.getSignature();
	    internalData.put(EACConstants.IDATA_PK_PCD, key);
	    internalData.put(EACConstants.IDATA_SIGNATURE, signature);
	    internalData.put(EACConstants.IDATA_TERMINAL_CERTIFICATE, terminalCertificate);

	    if (signature != null) {
		logger.trace("Signature has been provided in EAC2InputType.");

		// perform TA and CA authentication
		ChipAuthentication ca = new ChipAuthentication(dispatcher, slotHandle);
		AuthenticationHelper auth = new AuthenticationHelper(ta, ca);
		eac2Output = auth.performAuth(eac2Output, internalData);

		// no third step needed, notify GUI
		DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
		ctx.put(EACProtocol.AUTHENTICATION_DONE, true);
	    } else {
		logger.trace("Signature has not been provided in EAC2InputType.");

		// send challenge again
		byte[] rPICC = (byte[]) internalData.get(EACConstants.IDATA_CHALLENGE);
		eac2Output.setChallenge(rPICC);
	    }

	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(eac2Output.getAuthDataType());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	}

	Promise<Object> p = (Promise<Object>) dynCtx.getPromise(TR03112Keys.PROCESSING_CANCELLATION);
        if (p.derefNonblocking() == null) {
            return response;
        } else {
            response = new DIDAuthenticateResponse();
	    String msg = "Authentication Canceled by the user.";
            response.setResult(WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, msg));
            return response;
        }
    }

}
