/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.pkcs11;

import org.json.JSONObject;
import org.openecard.addon.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the PKCS11 functions in Java.
 * The functions which can be dispatched all have the {@link PKCS11Dispatchable} interface.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PKCS11Impl implements PKCS11Interface {

    private static final Logger logger = LoggerFactory.getLogger(PKCS11Impl.class);

    private final Context ctx;

    public PKCS11Impl(Context ctx) {
	this.ctx = ctx;
    }


    @PKCS11Dispatchable
    @Override
    public PKCS11Result C_Initialize(JSONObject obj) {
	// i don't see what needs to be done here
	return new PKCS11Result(PKCS11ReturnCode.CKR_OK);
    }

    @PKCS11Dispatchable
    @Override
    public PKCS11Result C_Finalize(JSONObject obj) {
	// i don't see what needs to be done here
	return new PKCS11Result(PKCS11ReturnCode.CKR_OK);
    }

    @Override
    public PKCS11Result C_GetInfo(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetFunctionList(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetSlotList(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetTokenInfo(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetMechanismList(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetMechanismInfo(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_InitToken(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SetPIN(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_OpenSession(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_CloseSession(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_CloseAllSessions(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetSessionInfo(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SetOperationState(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Login(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Logout(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_CreateObject(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_CopyObject(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DestroyObject(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetObjectSize(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetAttributeValue(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SetAttributeValue(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_FindObjectsInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_FindObjects(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_FindObjectsFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_EncryptInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Encrypt(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_EncryptFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DecryptInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Decrypt(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DecryptUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DecryptFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DigestInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DigestUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DigestKey(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DigestFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Sign(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignRecoverInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignRecover(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_VerifyInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_Verify(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_VerifyUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_VerifyFinal(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_VerifyRecoverInit(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_VerifyRecover(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DigestEncryptUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DecryptDigestUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SignEncryptUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DecryptVerifyUpdate(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GenerateKey(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GenerateKeyPair(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_WrapKey(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_UnwrapKey(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_DeriveKey(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_SeedRandom(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GenerateRandom(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_GetFunctionStatus(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_CancelFunction(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PKCS11Result C_WaitForSlotEvent(JSONObject obj) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
