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


/**
 * Interface with all PKCS11 functions as defined in {@code pkcs11_f.h}.
 * The functions don't need to be implemented if there is no {@link PKCS11Dispatchable} annotation.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface PKCS11Interface {

    PKCS11Result C_Initialize(JSONObject obj);
    PKCS11Result C_Finalize(JSONObject obj);
    PKCS11Result C_GetInfo(JSONObject obj);
    PKCS11Result C_GetFunctionList(JSONObject obj);
    PKCS11Result C_GetSlotList(JSONObject obj);
    PKCS11Result C_GetTokenInfo(JSONObject obj);
    PKCS11Result C_GetMechanismList(JSONObject obj);
    PKCS11Result C_GetMechanismInfo(JSONObject obj);
    PKCS11Result C_InitToken(JSONObject obj);
    PKCS11Result C_SetPIN(JSONObject obj);
    PKCS11Result C_OpenSession(JSONObject obj);
    PKCS11Result C_CloseSession(JSONObject obj);
    PKCS11Result C_CloseAllSessions(JSONObject obj);
    PKCS11Result C_GetSessionInfo(JSONObject obj);
    PKCS11Result C_SetOperationState(JSONObject obj);
    PKCS11Result C_Login(JSONObject obj);
    PKCS11Result C_Logout(JSONObject obj);
    PKCS11Result C_CreateObject(JSONObject obj);
    PKCS11Result C_CopyObject(JSONObject obj);
    PKCS11Result C_DestroyObject(JSONObject obj);
    PKCS11Result C_GetObjectSize(JSONObject obj);
    PKCS11Result C_GetAttributeValue(JSONObject obj);
    PKCS11Result C_SetAttributeValue(JSONObject obj);
    PKCS11Result C_FindObjectsInit(JSONObject obj);
    PKCS11Result C_FindObjects(JSONObject obj);
    PKCS11Result C_FindObjectsFinal(JSONObject obj);
    PKCS11Result C_EncryptInit(JSONObject obj);
    PKCS11Result C_Encrypt(JSONObject obj);
    PKCS11Result C_EncryptFinal(JSONObject obj);
    PKCS11Result C_DecryptInit(JSONObject obj);
    PKCS11Result C_Decrypt(JSONObject obj);
    PKCS11Result C_DecryptUpdate(JSONObject obj);
    PKCS11Result C_DecryptFinal(JSONObject obj);
    PKCS11Result C_DigestInit(JSONObject obj);
    PKCS11Result C_DigestUpdate(JSONObject obj);
    PKCS11Result C_DigestKey(JSONObject obj);
    PKCS11Result C_DigestFinal(JSONObject obj);
    PKCS11Result C_SignInit(JSONObject obj);
    PKCS11Result C_Sign(JSONObject obj);
    PKCS11Result C_SignUpdate(JSONObject obj);
    PKCS11Result C_SignFinal(JSONObject obj);
    PKCS11Result C_SignRecoverInit(JSONObject obj);
    PKCS11Result C_SignRecover(JSONObject obj);
    PKCS11Result C_VerifyInit(JSONObject obj);
    PKCS11Result C_Verify(JSONObject obj);
    PKCS11Result C_VerifyUpdate(JSONObject obj);
    PKCS11Result C_VerifyFinal(JSONObject obj);
    PKCS11Result C_VerifyRecoverInit(JSONObject obj);
    PKCS11Result C_VerifyRecover(JSONObject obj);
    PKCS11Result C_DigestEncryptUpdate(JSONObject obj);
    PKCS11Result C_DecryptDigestUpdate(JSONObject obj);
    PKCS11Result C_SignEncryptUpdate(JSONObject obj);
    PKCS11Result C_DecryptVerifyUpdate(JSONObject obj);
    PKCS11Result C_GenerateKey(JSONObject obj);
    PKCS11Result C_GenerateKeyPair(JSONObject obj);
    PKCS11Result C_WrapKey(JSONObject obj);
    PKCS11Result C_UnwrapKey(JSONObject obj);
    PKCS11Result C_DeriveKey(JSONObject obj);
    PKCS11Result C_SeedRandom(JSONObject obj);
    PKCS11Result C_GenerateRandom(JSONObject obj);
    
    // legacy
    PKCS11Result C_GetFunctionStatus(JSONObject obj);
    PKCS11Result C_CancelFunction(JSONObject obj);
    
    // cryptoki 2.01+
    PKCS11Result C_WaitForSlotEvent(JSONObject obj);

}
