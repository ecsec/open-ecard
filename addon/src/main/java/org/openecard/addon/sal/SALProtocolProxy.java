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
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.util.Map;
import org.openecard.addon.AbstractFactory;
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SALProtocolProxy extends AbstractFactory<SALProtocol> implements SALProtocol {

    private SALProtocol c;

    public SALProtocolProxy(String protocolClass, ClassLoader classLoader) {
	super(protocolClass, classLoader);
    }

    @Override
    public Map<String, Object> getInternalData() {
	return c.getInternalData();
    }

    @Override
    public boolean hasNextStep(FunctionType aFunction) {
	return c.hasNextStep(aFunction);
    }

    @Override
    public boolean isFinished() {
	return c.isFinished();
    }

    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession aParam) {
	return c.cardApplicationStartSession(aParam);
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession aParam) {
	return c.cardApplicationEndSession(aParam);
    }

    @Override
    public EncipherResponse encipher(Encipher aParam) {
	return c.encipher(aParam);
    }

    @Override
    public DecipherResponse decipher(Decipher aParam) {
	return c.decipher(aParam);
    }

    @Override
    public GetRandomResponse getRandom(GetRandom aParam) {
	return c.getRandom(aParam);
    }

    @Override
    public HashResponse hash(Hash aParam) {
	return c.hash(aParam);
    }

    @Override
    public SignResponse sign(Sign aParam) {
	return c.sign(aParam);
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature aParam) {
	return c.verifySignature(aParam);
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate aParam) {
	return c.verifyCertificate(aParam);
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate aParam) {
	return c.didCreate(aParam);
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate aParam) {
	return c.didUpdate(aParam);
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete aParam) {
	return c.didDelete(aParam);
    }

    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate aParam) {
	return c.didAuthenticate(aParam);
    }

    @Override
    public boolean needsSM() {
	return c.needsSM();
    }

    @Override
    public byte[] applySM(byte[] aCommandAPDU) {
	return c.applySM(aCommandAPDU);
    }

    @Override
    public byte[] removeSM(byte[] aResponseAPDU) {
	return c.removeSM(aResponseAPDU);
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	c = loadInstance(aCtx, SALProtocol.class);
    }

    @Override
    public void destroy() {
	c.destroy();
    }

}
