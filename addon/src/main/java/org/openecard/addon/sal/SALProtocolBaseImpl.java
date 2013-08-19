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
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    protected final ArrayList<ProtocolStep<?, ?>> steps;
    protected final Map<FunctionType, ProtocolStep<?, ?>> statelessSteps;

    /** Index marking current step in the step list. */
    protected int curStep = 0;

    protected SALProtocolBaseImpl() {
	this.internalData = new TreeMap<String, Object>();
	this.steps = new ArrayList<ProtocolStep<?, ?>>();
	this.statelessSteps = new EnumMap<FunctionType, ProtocolStep<?, ?>>(FunctionType.class);
    }


    @Override
    public TreeMap<String, Object> getInternalData() {
	return internalData;
    }

    private boolean hasNextStep() {
	return steps.size() > curStep;
    }

    private boolean hasNextProcessStep(FunctionType functionName) {
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

    private boolean hasStatelessStep(FunctionType functionName) {
	return statelessSteps.containsKey(functionName);
    }

    @Override
    public boolean hasNextStep(FunctionType functionName) {
	if (hasStatelessStep(functionName)) {
	    return true;
	}
	// check for a step in the process order
	return hasNextProcessStep(functionName);
    }

    @Override
    public boolean isFinished() {
	return ! hasNextStep();
    }

    protected @Nonnull ProtocolStep<?, ?> addOrderStep(@Nonnull ProtocolStep<?, ?> step) {
	steps.add(step);
	return step;
    }

    /**
     * Adds the given step to the stateless steps of this protocol.
     *
     * @param step The protocol step to add to the map.
     * @return The previously associated step, or null if there was no previous association.
     */
    protected @Nullable ProtocolStep<?, ?> addStatelessStep(@Nonnull ProtocolStep<?, ?> step) {
	return statelessSteps.put(step.getFunctionType(), step);
    }


    /**
     * Get next step and advance counter.
     * @return next step or null if none exists.
     */
    private ProtocolStep<? extends RequestType, ? extends ResponseType> next(FunctionType functionName) {
	// process order step takes precedence over stateless steps
	if (hasNextProcessStep(functionName)) {
	    ProtocolStep<?, ?> step = steps.get(curStep);
	    curStep++;
	    return step;
	} else {
	    return statelessSteps.get(functionName); // returns null if nothing found
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
	ProtocolStep<?, ?> s = next(FunctionType.CardApplicationStartSession);
	Class<? extends ResponseType> c = CardApplicationStartSessionResponse.class;
	return (CardApplicationStartSessionResponse) perform(c, s, param, internalData);
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession param) {
	ProtocolStep<?, ?> s = next(FunctionType.CardApplicationEndSession);
	Class<? extends ResponseType> c = CardApplicationEndSessionResponse.class;
	return (CardApplicationEndSessionResponse) perform(c, s, param, internalData);
    }

    @Override
    public EncipherResponse encipher(Encipher param) {
	ProtocolStep<?, ?> s = next(FunctionType.Encipher);
	return (EncipherResponse) perform(EncipherResponse.class, s, param, internalData);
    }

    @Override
    public DecipherResponse decipher(Decipher param) {
	ProtocolStep<?, ?> s = next(FunctionType.Decipher);
	return (DecipherResponse) perform(DecipherResponse.class, s, param, internalData);
    }

    @Override
    public GetRandomResponse getRandom(GetRandom param) {
	ProtocolStep<?, ?> s = next(FunctionType.GetRandom);
	return (GetRandomResponse) perform(GetRandomResponse.class, s, param, internalData);
    }

    @Override
    public HashResponse hash(Hash param) {
	ProtocolStep<?, ?> s = next(FunctionType.Hash);
	return (HashResponse) perform(HashResponse.class, s, param, internalData);
    }

    @Override
    public SignResponse sign(Sign param) {
	ProtocolStep<?, ?> s = next(FunctionType.Sign);
	return (SignResponse) perform(SignResponse.class, s, param, internalData);
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature param) {
	ProtocolStep<?, ?> s = next(FunctionType.VerifySignature);
	return (VerifySignatureResponse) perform(VerifySignatureResponse.class, s, param, internalData);
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate param) {
	ProtocolStep<?, ?> s = next(FunctionType.VerifyCertificate);
	return (VerifyCertificateResponse) perform(VerifyCertificateResponse.class, s, param, internalData);
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate param) {
	ProtocolStep<?, ?> s = next(FunctionType.DIDCreate);
	return (DIDCreateResponse) perform(DIDCreateResponse.class, s, param, internalData);
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate param) {
	ProtocolStep<?, ?> s = next(FunctionType.DIDUpdate);
	return (DIDUpdateResponse) perform(DIDUpdateResponse.class, s, param, internalData);
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete param) {
	ProtocolStep<?, ?> s = next(FunctionType.DIDDelete);
	return (DIDDeleteResponse) perform(DIDDeleteResponse.class, s, param, internalData);
    }

    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate param) {
	ProtocolStep<?, ?> s = next(FunctionType.DIDAuthenticate);
	return (DIDAuthenticateResponse) perform(DIDAuthenticateResponse.class, s, param, internalData);
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
