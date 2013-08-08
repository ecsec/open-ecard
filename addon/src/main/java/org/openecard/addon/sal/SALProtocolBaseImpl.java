/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon.sal;

import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDCreate;
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import iso.std.iso_iec._24727.tech.schema.GetRandom;
import iso.std.iso_iec._24727.tech.schema.GetRandomResponse;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.HashResponse;
import iso.std.iso_iec._24727.tech.schema.RequestType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.util.ArrayList;
import java.util.TreeMap;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;


/**
 * Basic implementation of a SAL protocol.<br/>
 * Some protocols may need to override this implementation in order to control Secure Messaging or
 * provide a customized protocol flow.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class SALProtocolBaseImpl implements SALProtocol {

    /** Object map to transport protocol specific parameters. Used when executing ProtocolStep. */
    protected final TreeMap<String, Object> internalData;
    /** List of ProtocolSteps, which are per default executed in order. */
    protected final ArrayList<ProtocolStep> steps;

    /** Index marking current step in the step list. */
    protected int curStep = 0;

    public SALProtocolBaseImpl() {
	this.internalData = new TreeMap<String, Object>();
	this.steps = new ArrayList<ProtocolStep>();
    }


    @Override
    public TreeMap<String, Object> getInternalData() {
	return internalData;
    }

    private boolean hasNextStep() {
	return steps.size() > curStep;
    }

    @Override
    public boolean hasNextStep(FunctionType functionName) {
	if (hasNextStep()) {
	    if (steps.get(curStep).getFunctionType() == functionName) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    @Override
    public boolean isFinished() {
	return ! hasNextStep();
    }


    /**
     * Get next step and advance counter.
     * @return next step or null if none exists.
     */
    private ProtocolStep next() {
	if (steps.size() > curStep) {
	    ProtocolStep step = steps.get(curStep);
	    curStep++;
	    return step;
	} else {
	    return null;
	}
    }

    private static <Req extends RequestType> ResponseType perform(Class<? extends ResponseType> responseClass,
	    ProtocolStep step, Req request, TreeMap<String, Object> internalData) {
	// return not implemented result first
	if (step == null) {
	    String msg = "There is no applicable protocol step at this point in the protocol flow.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, msg);
	    return WSHelper.makeResponse(responseClass, r);
	} else {
	    return step.perform(request, internalData);
	}
    }

    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession param) {
	return (CardApplicationStartSessionResponse) perform(CardApplicationEndSessionResponse.class, next(), param, internalData);
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession param) {
	return (CardApplicationEndSessionResponse) perform(CardApplicationEndSessionResponse.class, next(), param, internalData);
    }

    @Override
    public EncipherResponse encipher(Encipher param) {
	return (EncipherResponse) perform(EncipherResponse.class, next(), param, internalData);
    }

    @Override
    public DecipherResponse decipher(Decipher param) {
	return (DecipherResponse) perform(DecipherResponse.class, next(), param, internalData);
    }

    @Override
    public GetRandomResponse getRandom(GetRandom param) {
	return (GetRandomResponse) perform(GetRandomResponse.class, next(), param, internalData);
    }

    @Override
    public HashResponse hash(Hash param) {
	return (HashResponse) perform(HashResponse.class, next(), param, internalData);
    }

    @Override
    public SignResponse sign(Sign param) {
	return (SignResponse) perform(SignResponse.class, next(), param, internalData);
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature param) {
	return (VerifySignatureResponse) perform(VerifySignatureResponse.class, next(), param, internalData);
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate param) {
	return (VerifyCertificateResponse) perform(VerifyCertificateResponse.class, next(), param, internalData);
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate param) {
	return (DIDCreateResponse) perform(DIDCreateResponse.class, next(), param, internalData);
    }

    @Override
    public DIDGetResponse didGet(DIDGet param) {
	return (DIDGetResponse) perform(DIDGetResponse.class, next(), param, internalData);
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate param) {
	return (DIDUpdateResponse) perform(DIDUpdateResponse.class, next(), param, internalData);
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete param) {
	return (DIDDeleteResponse) perform(DIDDeleteResponse.class, next(), param, internalData);
    }

    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate param) {
	return (DIDAuthenticateResponse) perform(DIDAuthenticateResponse.class, next(), param, internalData);
    }


    ///
    /// Secure Messaging functions
    /// Overwrite in subclass when needed
    ///

    @Override
    public boolean needsSM() {
	return false;
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
	return commandAPDU;
    }
    @Override
    public byte[] removeSM(byte[] responseAPDU) {
	return responseAPDU;
    }

}
