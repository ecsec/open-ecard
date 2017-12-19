/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.mdlw.sal.cryptoki;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.openecard.mdlw.sal.MiddleWareWrapper;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper which wraps a loaded library and uses the function pointer from C_GetFunctionList to make the actual call.
 *
 * @author Tobias Wich
 */
public class CryptokiLibraryWrapper implements CryptokiLibrary {

    private static final Logger LOG = LoggerFactory.getLogger(CryptokiLibraryWrapper.class);

    private final CryptokiLibrary lib;
    private final CK_FUNCTION_LIST funs;

    public CryptokiLibraryWrapper(CryptokiLibrary lib) throws CryptokiException {
	this.lib = lib;

	LOG.info("Loading PKCS#11 functions via C_GetFunctionList.");
	PointerByReference ref = new PointerByReference();
	MiddleWareWrapper.check("C_GetFunctionList", lib.C_GetFunctionList(ref));
	Pointer listPtr = ref.getValue();
	funs = new CK_FUNCTION_LIST(listPtr);
	funs.read();
	LOG.info("PKCS#11 functions loaded successfully.");
    }

    @Override
    public NativeLong C_GetFunctionList(PointerByReference ppFunctionList) {
	return lib.C_GetFunctionList(ppFunctionList);
    }

    @Override
    public NativeLong C_Initialize(Pointer pInitArgs) {
	return funs.c_Initialize.call(pInitArgs);
    }

    @Override
    public NativeLong C_Finalize(Pointer pReserved) {
	return funs.c_Finalize.call(pReserved);
    }

    @Override
    public NativeLong C_GetInfo(CK_INFO pInfo) {
	return funs.c_GetInfo.call(pInfo);
    }

    @Override
    public NativeLong C_GetSlotList(byte tokenPresent, Memory pSlotList, NativeLongByReference pulCount) {
	return funs.c_GetSlotList.call(tokenPresent, pSlotList, pulCount);
    }

    @Override
    public NativeLong C_GetSlotInfo(NativeLong slotID, CK_SLOT_INFO pInfo) {
	return funs.c_GetSlotInfo.call(slotID, pInfo);
    }

    @Override
    public NativeLong C_GetTokenInfo(NativeLong slotID, CK_TOKEN_INFO pInfo) {
	return funs.c_GetTokenInfo.call(slotID, pInfo);
    }

    @Override
    public NativeLong C_GetMechanismList(NativeLong slotID, Memory pMechanismList, NativeLongByReference pulCount) {
	return funs.c_GetMechanismList.call(slotID, pMechanismList, pulCount);
    }

    @Override
    public NativeLong C_GetMechanismInfo(NativeLong slotID, NativeLong type, CK_MECHANISM_INFO pInfo) {
	return funs.c_GetMechanismInfo.call(slotID, type, pInfo);
    }

    @Override
    public NativeLong C_InitToken(NativeLong slotID, ByteBuffer pPin, NativeLong ulPinLen, ByteBuffer pLabel) {
	return funs.c_InitToken.call(slotID, pPin, ulPinLen, pLabel);
    }

    @Override
    public NativeLong C_InitPIN(NativeLong hSession, ByteBuffer pPin, NativeLong ulPinLen) {
	return funs.c_InitPIN.call(hSession, pPin, ulPinLen);
    }

    @Override
    public NativeLong C_SetPIN(NativeLong hSession, ByteBuffer pOldPin, NativeLong ulOldLen, ByteBuffer pNewPin, NativeLong ulNewLen) {
	return funs.c_SetPIN.call(hSession, pOldPin, ulOldLen, pNewPin, ulNewLen);
    }

    @Override
    public NativeLong C_OpenSession(NativeLong slotID, NativeLong flags, Pointer pApplication, CK_NOTIFY Notify, NativeLongByReference phSession) {
	return funs.c_OpenSession.call(slotID, flags, pApplication, Notify, phSession);
    }

    @Override
    public NativeLong C_CloseSession(NativeLong hSession) {
	return funs.c_CloseSession.call(hSession);
    }

    @Override
    public NativeLong C_CloseAllSessions(NativeLong slotID) {
	return funs.c_CloseAllSessions.call(slotID);
    }

    @Override
    public NativeLong C_GetSessionInfo(NativeLong hSession, CK_SESSION_INFO pInfo) {
	return funs.c_GetSessionInfo.call(hSession, pInfo);
    }

    @Override
    public NativeLong C_GetOperationState(NativeLong hSession, ByteBuffer pOperationState, LongBuffer pulOperationStateLen) {
	return funs.c_GetOperationState.call(hSession, pOperationState, pulOperationStateLen);
    }

    @Override
    public NativeLong C_SetOperationState(NativeLong hSession, ByteBuffer pOperationState, NativeLong ulOperationStateLen, NativeLong hEncryptionKey, NativeLong hAuthenticationKey) {
	return funs.c_SetOperationState.call(hSession, pOperationState, ulOperationStateLen, hEncryptionKey, hAuthenticationKey);
    }

    @Override
    public NativeLong C_Login(NativeLong hSession, NativeLong userType, ByteBuffer pPin, NativeLong ulPinLen) {
	return funs.c_Login.call(hSession, userType, pPin, ulPinLen);
    }

    @Override
    public NativeLong C_Logout(NativeLong hSession) {
	return funs.c_Logout.call(hSession);
    }

    @Override
    public NativeLong C_CreateObject(NativeLong hSession, CK_ATTRIBUTE pTemplate, NativeLong ulCount, LongBuffer phObject) {
	return funs.c_CreateObject.call(hSession, pTemplate, ulCount, phObject);
    }

    @Override
    public NativeLong C_CopyObject(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount, LongBuffer phNewObject) {
	return funs.c_CopyObject.call(hSession, hObject, pTemplate, ulCount, phNewObject);
    }

    @Override
    public NativeLong C_DestroyObject(NativeLong hSession, NativeLong hObject) {
	return funs.c_DestroyObject.call(hSession, hObject);
    }

    @Override
    public NativeLong C_GetObjectSize(NativeLong hSession, NativeLong hObject, LongBuffer pulSize) {
	return funs.c_GetObjectSize.call(hSession, hObject, pulSize);
    }

    @Override
    public NativeLong C_GetAttributeValue(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount) {
	return funs.c_GetAttributeValue.call(hSession, hObject, pTemplate, ulCount);
    }

    @Override
    public NativeLong C_SetAttributeValue(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount) {
	return funs.c_SetAttributeValue.call(hSession, hObject, pTemplate, ulCount);
    }

    @Override
    public NativeLong C_FindObjectsInit(NativeLong hSession, CK_ATTRIBUTE pTemplate, NativeLong ulCount) {
	return funs.c_FindObjectsInit.call(hSession, pTemplate, ulCount);
    }

    @Override
    public NativeLong C_FindObjects(NativeLong hSession, Memory phObject, NativeLong ulMaxObjectCount, NativeLongByReference pulObjectCount) {
	return funs.c_FindObjects.call(hSession, phObject, ulMaxObjectCount, pulObjectCount);
    }

    @Override
    public NativeLong C_FindObjectsFinal(NativeLong hSession) {
	return funs.c_FindObjectsFinal.call(hSession);
    }

    @Override
    public NativeLong C_EncryptInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_EncryptInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_Encrypt(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pEncryptedData, LongBuffer pulEncryptedDataLen) {
	return funs.c_Encrypt.call(hSession, pData, ulDataLen, pEncryptedData, pulEncryptedDataLen);
    }

    @Override
    public NativeLong C_EncryptUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen, ByteBuffer pEncryptedPart, LongBuffer pulEncryptedPartLen) {
	return funs.c_EncryptUpdate.call(hSession, pPart, ulPartLen, pEncryptedPart, pulEncryptedPartLen);
    }

    @Override
    public NativeLong C_EncryptFinal(NativeLong hSession, ByteBuffer pLastEncryptedPart, LongBuffer pulLastEncryptedPartLen) {
	return funs.c_EncryptFinal.call(hSession, pLastEncryptedPart, pulLastEncryptedPartLen);
    }

    @Override
    public NativeLong C_DecryptInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_DecryptInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_Decrypt(NativeLong hSession, ByteBuffer pEncryptedData, NativeLong ulEncryptedDataLen, ByteBuffer pData, LongBuffer pulDataLen) {
	return funs.c_Decrypt.call(hSession, pEncryptedData, ulEncryptedDataLen, pData, pulDataLen);
    }

    @Override
    public NativeLong C_DecryptUpdate(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen, ByteBuffer pPart, LongBuffer pulPartLen) {
	return funs.c_DecryptUpdate.call(hSession, pEncryptedPart, ulEncryptedPartLen, pPart, pulPartLen);
    }

    @Override
    public NativeLong C_DecryptFinal(NativeLong hSession, ByteBuffer pLastPart, LongBuffer pulLastPartLen) {
	return funs.c_DecryptFinal.call(hSession, pLastPart, pulLastPartLen);
    }

    @Override
    public NativeLong C_DigestInit(NativeLong hSession, CK_MECHANISM pMechanism) {
	return funs.c_DigestInit.call(hSession, pMechanism);
    }

    @Override
    public NativeLong C_Digest(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pDigest, LongBuffer pulDigestLen) {
	return funs.c_Digest.call(hSession, pData, ulDataLen, pDigest, pulDigestLen);
    }

    @Override
    public NativeLong C_DigestUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen) {
	return funs.c_DigestUpdate.call(hSession, pPart, ulPartLen);
    }

    @Override
    public NativeLong C_DigestKey(NativeLong hSession, NativeLong hKey) {
	return funs.c_DigestKey.call(hSession, hKey);
    }

    @Override
    public NativeLong C_DigestFinal(NativeLong hSession, ByteBuffer pDigest, LongBuffer pulDigestLen) {
	return funs.c_DigestFinal.call(hSession, pDigest, pulDigestLen);
    }

    @Override
    public NativeLong C_SignInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_SignInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_Sign(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature, NativeLongByReference pulSignatureLen) {
	return funs.c_Sign.call(hSession, pData, ulDataLen, pSignature, pulSignatureLen);
    }

    @Override
    public NativeLong C_SignUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen) {
	return funs.c_SignUpdate.call(hSession, pPart, ulPartLen);
    }

    @Override
    public NativeLong C_SignFinal(NativeLong hSession, ByteBuffer pSignature, LongBuffer pulSignatureLen) {
	return funs.c_SignFinal.call(hSession, pSignature, pulSignatureLen);
    }

    @Override
    public NativeLong C_SignRecoverInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_SignRecoverInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_SignRecover(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature, LongBuffer pulSignatureLen) {
	return funs.c_SignRecover.call(hSession, pData, ulDataLen, pSignature, pulSignatureLen);
    }

    @Override
    public NativeLong C_VerifyInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_VerifyInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_Verify(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature, NativeLong ulSignatureLen) {
	return funs.c_Verify.call(hSession, pData, ulDataLen, pSignature, ulSignatureLen);
    }

    @Override
    public NativeLong C_VerifyUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen) {
	return funs.c_VerifyUpdate.call(hSession, pPart, ulPartLen);
    }

    @Override
    public NativeLong C_VerifyFinal(NativeLong hSession, ByteBuffer pSignature, NativeLong ulSignatureLen) {
	return funs.c_VerifyFinal.call(hSession, pSignature, ulSignatureLen);
    }

    @Override
    public NativeLong C_VerifyRecoverInit(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey) {
	return funs.c_VerifyRecoverInit.call(hSession, pMechanism, hKey);
    }

    @Override
    public NativeLong C_VerifyRecover(NativeLong hSession, ByteBuffer pSignature, NativeLong ulSignatureLen, ByteBuffer pData, LongBuffer pulDataLen) {
	return funs.c_VerifyRecover.call(hSession, pSignature, ulSignatureLen, pData, pulDataLen);
    }

    @Override
    public NativeLong C_DigestEncryptUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen, ByteBuffer pEncryptedPart, LongBuffer pulEncryptedPartLen) {
	return funs.c_DigestEncryptUpdate.call(hSession, pPart, ulPartLen, pEncryptedPart, pulEncryptedPartLen);
    }

    @Override
    public NativeLong C_DecryptDigestUpdate(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen, ByteBuffer pPart, LongBuffer pulPartLen) {
	return funs.c_DecryptDigestUpdate.call(hSession, pEncryptedPart, ulEncryptedPartLen, pPart, pulPartLen);
    }

    @Override
    public NativeLong C_SignEncryptUpdate(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen, ByteBuffer pEncryptedPart, LongBuffer pulEncryptedPartLen) {
	return funs.c_SignEncryptUpdate.call(hSession, pPart, ulPartLen, pEncryptedPart, pulEncryptedPartLen);
    }

    @Override
    public NativeLong C_DecryptVerifyUpdate(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen, ByteBuffer pPart, LongBuffer pulPartLen) {
	return funs.c_DecryptVerifyUpdate.call(hSession, pEncryptedPart, ulEncryptedPartLen, pPart, pulPartLen);
    }

    @Override
    public NativeLong C_GenerateKey(NativeLong hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE pTemplate, NativeLong ulCount, LongBuffer phKey) {
	return funs.c_GenerateKey.call(hSession, pMechanism, pTemplate, ulCount, phKey);
    }

    @Override
    public NativeLong C_GenerateKeyPair(NativeLong hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE pPublicKeyTemplate, NativeLong ulPublicKeyAttributeCount, CK_ATTRIBUTE pPrivateKeyTemplate, NativeLong ulPrivateKeyAttributeCount, LongBuffer phPublicKey, LongBuffer phPrivateKey) {
	return funs.c_GenerateKeyPair.call(hSession, pMechanism, pPublicKeyTemplate, ulPublicKeyAttributeCount, pPrivateKeyTemplate, ulPrivateKeyAttributeCount, phPublicKey, phPrivateKey);
    }

    @Override
    public NativeLong C_WrapKey(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hWrappingKey, NativeLong hKey, ByteBuffer pWrappedKey, LongBuffer pulWrappedKeyLen) {
	return funs.c_WrapKey.call(hSession, pMechanism, hWrappingKey, hKey, pWrappedKey, pulWrappedKeyLen);
    }

    @Override
    public NativeLong C_UnwrapKey(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hUnwrappingKey, ByteBuffer pWrappedKey, NativeLong ulWrappedKeyLen, CK_ATTRIBUTE pTemplate, NativeLong ulAttributeCount, LongBuffer phKey) {
	return funs.c_UnwrapKey.call(hSession, pMechanism, hUnwrappingKey, pWrappedKey, ulWrappedKeyLen, pTemplate, ulAttributeCount, phKey);
    }

    @Override
    public NativeLong C_DeriveKey(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hBaseKey, CK_ATTRIBUTE pTemplate, NativeLong ulAttributeCount, LongBuffer phKey) {
	return funs.c_DeriveKey.call(hSession, pMechanism, hBaseKey, pTemplate, ulAttributeCount, phKey);
    }

    @Override
    public NativeLong C_SeedRandom(NativeLong hSession, ByteBuffer pSeed, NativeLong ulSeedLen) {
	return funs.c_SeedRandom.call(hSession, pSeed, ulSeedLen);
    }

    @Override
    public NativeLong C_GenerateRandom(NativeLong hSession, ByteBuffer RandomData, NativeLong ulRandomLen) {
	return funs.c_GenerateRandom.call(hSession, RandomData, ulRandomLen);
    }

    @Override
    public NativeLong C_GetFunctionStatus(NativeLong hSession) {
	return funs.c_GetFunctionStatus.call(hSession);
    }

    @Override
    public NativeLong C_CancelFunction(NativeLong hSession) {
	return funs.c_CancelFunction.call(hSession);
    }

    @Override
    public NativeLong C_WaitForSlotEvent(NativeLong flags, NativeLongByReference pSlot, Pointer pRserved) {
	return funs.c_WaitForSlotEvent.call(flags, pSlot, pRserved);
    }

}
