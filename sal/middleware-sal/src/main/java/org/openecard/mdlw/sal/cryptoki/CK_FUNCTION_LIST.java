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

import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class CK_FUNCTION_LIST extends Structure {

    public CK_FUNCTION_LIST() {
	super();
    }

    public CK_FUNCTION_LIST(Pointer peer) {
	super(peer);
    }


    public CK_VERSION version;

    public CK_VERSION getVersion() {
	return version;
    }

    public void setVersion(CK_VERSION version) {
	this.version = version;
    }


    public interface C_Initialize extends Callback {
	NativeLong call(Pointer pInitArgs);
    }
    public C_Initialize c_Initialize;

    public interface C_Finalize extends Callback {
	NativeLong call(Pointer pReserved);
    }
    public C_Finalize c_Finalize;

    public interface C_GetInfo extends Callback {
	NativeLong call(CK_INFO pInfo);
    }
    public C_GetInfo c_GetInfo;

    public interface C_GetFunctionList extends Callback {
	NativeLong call(PointerByReference ppFunctionList);
    }
    public C_GetFunctionList c_GetFunctionList;

    public interface C_GetSlotList extends Callback {
	NativeLong call(byte tokenPresent, Memory pSlotList, NativeLongByReference pulCount);
    }
    public C_GetSlotList c_GetSlotList;

    public interface C_GetSlotInfo extends Callback {
	NativeLong call(NativeLong slotID, CK_SLOT_INFO pInfo);
    }
    public C_GetSlotInfo c_GetSlotInfo;

    public interface C_GetTokenInfo extends Callback {
	NativeLong call(NativeLong slotID, CK_TOKEN_INFO pInfo);
    }
    public C_GetTokenInfo c_GetTokenInfo;

    public interface C_GetMechanismList extends Callback {
	NativeLong call(NativeLong slotID, Memory pMechanismList, NativeLongByReference pulCount);
    }
    public C_GetMechanismList c_GetMechanismList;

    public interface C_GetMechanismInfo extends Callback {
	NativeLong call(NativeLong slotID, NativeLong type, CK_MECHANISM_INFO pInfo);
    }
    public C_GetMechanismInfo c_GetMechanismInfo;

    public interface C_InitToken extends Callback {
	NativeLong call(NativeLong slotID, ByteBuffer pPin, NativeLong ulPinLen, ByteBuffer pLabel);
    }
    public C_InitToken c_InitToken;

    public interface C_InitPIN extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPin, NativeLong ulPinLen);
    }
    public C_InitPIN c_InitPIN;

    public interface C_SetPIN extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pOldPin, NativeLong ulOldLen, ByteBuffer pNewPin,
		NativeLong ulNewLen);
    }
    public C_SetPIN c_SetPIN;

    public interface C_OpenSession extends Callback {
	NativeLong call(NativeLong slotID, NativeLong flags, Pointer pApplication,
		CryptokiLibrary.CK_NOTIFY Notify, NativeLongByReference phSession);
    }
    public C_OpenSession c_OpenSession;

    public interface C_CloseSession extends Callback {
	NativeLong call(NativeLong hSession);
    }
    public C_CloseSession c_CloseSession;

    public interface C_CloseAllSessions extends Callback {
	NativeLong call(NativeLong slotID);
    }
    public C_CloseAllSessions c_CloseAllSessions;

    public interface C_GetSessionInfo extends Callback {
	NativeLong call(NativeLong hSession, CK_SESSION_INFO pInfo);
    }
    public C_GetSessionInfo c_GetSessionInfo;

    public interface C_GetOperationState extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pOperationState, LongBuffer pulOperationStateLen);
    }
    public C_GetOperationState c_GetOperationState;

    public interface C_SetOperationState extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pOperationState, NativeLong ulOperationStateLen,
		NativeLong hEncryptionKey, NativeLong hAuthenticationKey);
    }
    public C_SetOperationState c_SetOperationState;

    public interface C_Login extends Callback {
	NativeLong call(NativeLong hSession, NativeLong userType, ByteBuffer pPin, NativeLong ulPinLen);
    }
    public C_Login c_Login;

    public interface C_Logout extends Callback {
	NativeLong call(NativeLong hSession);
    }
    public C_Logout c_Logout;

    public interface C_CreateObject extends Callback {
	NativeLong call(NativeLong hSession, CK_ATTRIBUTE pTemplate, NativeLong ulCount, LongBuffer phObject);
    }
    public C_CreateObject c_CreateObject;

    public interface C_CopyObject extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount,
		LongBuffer phNewObject);
    }
    public C_CopyObject c_CopyObject;

    public interface C_DestroyObject extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hObject);
    }
    public C_DestroyObject c_DestroyObject;

    public interface C_GetObjectSize extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hObject, LongBuffer pulSize);
    }
    public C_GetObjectSize c_GetObjectSize;

    public interface C_GetAttributeValue extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount);
    }
    public C_GetAttributeValue c_GetAttributeValue;

    public interface C_SetAttributeValue extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hObject, CK_ATTRIBUTE pTemplate, NativeLong ulCount);
    }
    public C_SetAttributeValue c_SetAttributeValue;

    public interface C_FindObjectsInit extends Callback {
	NativeLong call(NativeLong hSession, CK_ATTRIBUTE pTemplate, NativeLong ulCount);
    }
    public C_FindObjectsInit c_FindObjectsInit;

    public interface C_FindObjects extends Callback {
	NativeLong call(NativeLong hSession, Memory phObject, NativeLong ulMaxObjectCount,
		NativeLongByReference pulObjectCount);
    }
    public C_FindObjects c_FindObjects;

    public interface C_FindObjectsFinal extends Callback {
	NativeLong call(NativeLong hSession);
    }
    public C_FindObjectsFinal c_FindObjectsFinal;

    public interface C_EncryptInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_EncryptInit c_EncryptInit;

    public interface C_Encrypt extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pEncryptedData,
		LongBuffer pulEncryptedDataLen);
    }
    public C_Encrypt c_Encrypt;

    public interface C_EncryptUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen, ByteBuffer pEncryptedPart,
		LongBuffer pulEncryptedPartLen);
    }
    public C_EncryptUpdate c_EncryptUpdate;

    public interface C_EncryptFinal extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pLastEncryptedPart, LongBuffer pulLastEncryptedPartLen);
    }
    public C_EncryptFinal c_EncryptFinal;

    public interface C_DecryptInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_DecryptInit c_DecryptInit;

    public interface C_Decrypt extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pEncryptedData, NativeLong ulEncryptedDataLen,
		ByteBuffer pData, LongBuffer pulDataLen);
    }
    public C_Decrypt c_Decrypt;

    public interface C_DecryptUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen,
		ByteBuffer pPart, LongBuffer pulPartLen);
    }
    public C_DecryptUpdate c_DecryptUpdate;

    public interface C_DecryptFinal extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pLastPart, LongBuffer pulLastPartLen);
    }
    public C_DecryptFinal c_DecryptFinal;

    public interface C_DigestInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism);
    }
    public C_DigestInit c_DigestInit;

    public interface C_Digest extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pDigest,
		LongBuffer pulDigestLen);
    }
    public C_Digest c_Digest;

    public interface C_DigestUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen);
    }
    public C_DigestUpdate c_DigestUpdate;

    public interface C_DigestKey extends Callback {
	NativeLong call(NativeLong hSession, NativeLong hKey);
    }
    public C_DigestKey c_DigestKey;

    public interface C_DigestFinal extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pDigest, LongBuffer pulDigestLen);
    }
    public C_DigestFinal c_DigestFinal;

    public interface C_SignInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_SignInit c_SignInit;

    public interface C_Sign extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature,
		NativeLongByReference pulSignatureLen);
    }
    public C_Sign c_Sign;

    public interface C_SignUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen);
    }
    public C_SignUpdate c_SignUpdate;

    public interface C_SignFinal extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pSignature, LongBuffer pulSignatureLen);
    }
    public C_SignFinal c_SignFinal;

    public interface C_SignRecoverInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_SignRecoverInit c_SignRecoverInit;

    public interface C_SignRecover extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature,
		LongBuffer pulSignatureLen);
    }
    public C_SignRecover c_SignRecover;

    public interface C_VerifyInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_VerifyInit c_VerifyInit;

    public interface C_Verify extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pData, NativeLong ulDataLen, ByteBuffer pSignature,
		NativeLong ulSignatureLen);
    }
    public C_Verify c_Verify;

    public interface C_VerifyUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen);
    }
    public C_VerifyUpdate c_VerifyUpdate;

    public interface C_VerifyFinal extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pSignature, NativeLong ulSignatureLen);
    }
    public C_VerifyFinal c_VerifyFinal;

    public interface C_VerifyRecoverInit extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hKey);
    }
    public C_VerifyRecoverInit c_VerifyRecoverInit;

    public interface C_VerifyRecover extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pSignature, NativeLong ulSignatureLen, ByteBuffer pData,
		LongBuffer pulDataLen);
    }
    public C_VerifyRecover c_VerifyRecover;

    public interface C_DigestEncryptUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen,
		ByteBuffer pEncryptedPart, LongBuffer pulEncryptedPartLen);
    }
    public C_DigestEncryptUpdate c_DigestEncryptUpdate;

    public interface C_DecryptDigestUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen,
		ByteBuffer pPart, LongBuffer pulPartLen);
    }
    public C_DecryptDigestUpdate c_DecryptDigestUpdate;

    public interface C_SignEncryptUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pPart, NativeLong ulPartLen,
		ByteBuffer pEncryptedPart, LongBuffer pulEncryptedPartLen);
    }
    public C_SignEncryptUpdate c_SignEncryptUpdate;

    public interface C_DecryptVerifyUpdate extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pEncryptedPart, NativeLong ulEncryptedPartLen,
		ByteBuffer pPart, LongBuffer pulPartLen);
    }
    public C_DecryptVerifyUpdate c_DecryptVerifyUpdate;

    public interface C_GenerateKey extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE pTemplate, NativeLong ulCount,
		LongBuffer phKey);
    }
    public C_GenerateKey c_GenerateKey;

    public interface C_GenerateKeyPair extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE pPublicKeyTemplate,
		NativeLong ulPublicKeyAttributeCount, CK_ATTRIBUTE pPrivateKeyTemplate,
		NativeLong ulPrivateKeyAttributeCount, LongBuffer phPublicKey, LongBuffer phPrivateKey);
    }
    public C_GenerateKeyPair c_GenerateKeyPair;

    public interface C_WrapKey extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hWrappingKey, NativeLong hKey,
		ByteBuffer pWrappedKey, LongBuffer pulWrappedKeyLen);
    }
    public C_WrapKey c_WrapKey;

    public interface C_UnwrapKey extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hUnwrappingKey,
		ByteBuffer pWrappedKey, NativeLong ulWrappedKeyLen, CK_ATTRIBUTE pTemplate, NativeLong ulAttributeCount,
		LongBuffer phKey);
    }
    public C_UnwrapKey c_UnwrapKey;

    public interface C_DeriveKey extends Callback {
	NativeLong call(NativeLong hSession, CK_MECHANISM pMechanism, NativeLong hBaseKey, CK_ATTRIBUTE pTemplate,
		NativeLong ulAttributeCount, LongBuffer phKey);
    }
    public C_DeriveKey c_DeriveKey;

    public interface C_SeedRandom extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer pSeed, NativeLong ulSeedLen);
    }
    public C_SeedRandom c_SeedRandom;

    public interface C_GenerateRandom extends Callback {
	NativeLong call(NativeLong hSession, ByteBuffer RandomData, NativeLong ulRandomLen);
    }
    public C_GenerateRandom c_GenerateRandom;

    public interface C_GetFunctionStatus extends Callback {
	NativeLong call(NativeLong hSession);
    }
    public C_GetFunctionStatus c_GetFunctionStatus;

    public interface C_CancelFunction extends Callback {
	NativeLong call(NativeLong hSession);
    }
    public C_CancelFunction c_CancelFunction;

    public interface C_WaitForSlotEvent extends Callback {
	NativeLong call(NativeLong flags, NativeLongByReference pSlot, Pointer pRserved);
    }
    public C_WaitForSlotEvent c_WaitForSlotEvent;


    @Override
    protected List<String> getFieldOrder() {
	return Arrays.asList("version",
		"c_Initialize",
		"c_Finalize",
		"c_GetInfo",
		"c_GetFunctionList",
		"c_GetSlotList",
		"c_GetSlotInfo",
		"c_GetTokenInfo",
		"c_GetMechanismList",
		"c_GetMechanismInfo",
		"c_InitToken",
		"c_InitPIN",
		"c_SetPIN",
		"c_OpenSession",
		"c_CloseSession",
		"c_CloseAllSessions",
		"c_GetSessionInfo",
		"c_GetOperationState",
		"c_SetOperationState",
		"c_Login",
		"c_Logout",
		"c_CreateObject",
		"c_CopyObject",
		"c_DestroyObject",
		"c_GetObjectSize",
		"c_GetAttributeValue",
		"c_SetAttributeValue",
		"c_FindObjectsInit",
		"c_FindObjects",
		"c_FindObjectsFinal",
		"c_EncryptInit",
		"c_Encrypt",
		"c_EncryptUpdate",
		"c_EncryptFinal",
		"c_DecryptInit",
		"c_Decrypt",
		"c_DecryptUpdate",
		"c_DecryptFinal",
		"c_DigestInit",
		"c_Digest",
		"c_DigestUpdate",
		"c_DigestKey",
		"c_DigestFinal",
		"c_SignInit",
		"c_Sign",
		"c_SignUpdate",
		"c_SignFinal",
		"c_SignRecoverInit",
		"c_SignRecover",
		"c_VerifyInit",
		"c_Verify",
		"c_VerifyUpdate",
		"c_VerifyFinal",
		"c_VerifyRecoverInit",
		"c_VerifyRecover",
		"c_DigestEncryptUpdate",
		"c_DecryptDigestUpdate",
		"c_SignEncryptUpdate",
		"c_DecryptVerifyUpdate",
		"c_GenerateKey",
		"c_GenerateKeyPair",
		"c_WrapKey",
		"c_UnwrapKey",
		"c_DeriveKey",
		"c_SeedRandom",
		"c_GenerateRandom",
		"c_GetFunctionStatus",
		"c_CancelFunction",
		"c_WaitForSlotEvent"
	);
    }

    public static class ByReference extends CK_FUNCTION_LIST implements Structure.ByReference {

    };

    public static class ByValue extends CK_FUNCTION_LIST implements Structure.ByValue {

    };

}
