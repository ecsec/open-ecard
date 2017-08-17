/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.cryptoki.CK_MECHANISM;
import org.openecard.mdlw.sal.struct.CkAttribute;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.openecard.common.ThreadTerminateException;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.mdlw.sal.cryptoki.CK_RSA_PKCS_PSS_PARAMS;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.exceptions.InvalidArgumentsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Jan Mannsbart
 * @author Tobias Wich
 */
public class MwPrivateKey extends MwAbstractKey {

    private static final Logger LOG = LoggerFactory.getLogger(MwPrivateKey.class);

    private final String keyLabel;
    private final Boolean sensitive;
    private final Boolean decrypt;
    private final Boolean sign;
    private final Boolean singRecover;
    private final Boolean unwrap;
    private final Boolean extractable;
    private final Boolean alwaysSensitive;
    private final Boolean neverExtractable;
    private final Boolean wrapWithTrusted;
    private final long unwrapTemplate;
    private final Boolean alwaysAuthenticate;

    /**
     * Created a new Private Key from a given Object Handle
     * 
     * @param objectHandle
     * @param mw
     * @param mwSession
     * @throws CryptokiException
     */
    public MwPrivateKey(long objectHandle, MiddleWareWrapper mw, MwSession mwSession) throws CryptokiException {
        super(objectHandle, mw, mwSession);
        this.keyLabel = loadAttrValueLabel();
        this.sensitive = loadAttrValueSensitive();
        this.decrypt = loadAttrValueDecrypt();
        this.sign = loadAttrValueSign();
        this.singRecover = loadAttrValueSignRecover();
        this.unwrap = loadAttrValueUnwrap();
        this.extractable = loadAttrValueExtractable();
        this.alwaysSensitive = loadAttrValueAlwaysSensitive();
        this.neverExtractable = loadAttrValueNeverExtractable();
        this.wrapWithTrusted = loadAttrValueWrapWithTrusted();
        this.unwrapTemplate = loadAttrValueUnwrapTemplate();
        this.alwaysAuthenticate = loadAttrValueAlwaysAuthenticate();
    }

    /**
     * Loading the Attribute Value for CKA_ALWAYS_AUTHENTICATE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueAlwaysAuthenticate() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_ALWAYS_AUTHENTICATE);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_UNWRAP_TEMPLATE
     * 
     * @return long
     * @throws CryptokiException
     */
    private long loadAttrValueUnwrapTemplate() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_UNWRAP_TEMPLATE);
	return AttributeUtils.getLong(raw);
    }

    /**
     * Loading the Attribute Value for CKA_WRAP_WITH_TRUSTED
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueWrapWithTrusted() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_WRAP_WITH_TRUSTED);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_NEVER_EXTRACTABLE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueNeverExtractable() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_NEVER_EXTRACTABLE);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_ALWAYS_SENSITIVE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueAlwaysSensitive() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_ALWAYS_SENSITIVE);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_EXTRACTABLE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueExtractable() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_EXTRACTABLE);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_UNWRAP
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueUnwrap() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_UNWRAP);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_SIGN_RECOVER
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueSignRecover() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_SIGN_RECOVER);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_SIGN
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueSign() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_SIGN);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_DECRYPT
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueDecrypt() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_DECRYPT);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_SENSITIVE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueSensitive() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_SENSITIVE);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Signs Data with a {@link Mechanism}.
     * Returns the signed Data in an byte array.
     *
     * @param mechanism
     * @param data
     * @return
     * @throws CryptokiException
     */
    public byte[] sign(SignatureAlgorithms mechanism, byte[] data) throws CryptokiException {
	return sign(mechanism.getPkcs11Mechanism(), data);
    }

    /**
     * Signs Data with a {@link Mechanism}.
     * Returns the signed Data in an byte array.
     *
     * @param mechanism
     * @param data
     * @return
     * @throws CryptokiException
     */
    public byte[] sign(long mechanism, byte[] data) throws CryptokiException {
	Pointer paramsPtr;
	NativeLong paramsPtrSize;

	if (isPSSAlg((int) mechanism)) { // only execute with PSS algorithm
	    // determine parameters for PKCS#11 PSS
	    LOG.debug("Preparing PSS Parameters.");
	    NativeLong hashAlg = new NativeLong(getHashAlg((int) mechanism, data), true);
	    NativeLong mgfAlg = new NativeLong(getMgf1Alg(hashAlg.intValue()), true);
	    NativeLong sLen = new NativeLong(getHashLen(hashAlg.intValue()), true);
	    CK_RSA_PKCS_PSS_PARAMS pssParams = new CK_RSA_PKCS_PSS_PARAMS(hashAlg, mgfAlg, sLen);
	    pssParams.write();
	    paramsPtr = pssParams.getPointer();
	    paramsPtrSize = new NativeLong(pssParams.size(), true);
	} else {
	    paramsPtr = Pointer.NULL;
	    paramsPtrSize = new NativeLong(0, true);
	}

        CK_MECHANISM pMechanism = new CK_MECHANISM(new NativeLong(mechanism, true), paramsPtr, paramsPtrSize);

	try (MiddleWareWrapper.LockedMiddlewareWrapper lmw = mw.lock()) {
	    lmw.signInit(session.getSessionId(), pMechanism, objectHandle);
	    return lmw.sign(session.getSessionId(), data);
	} catch (InterruptedException ex) {
	    throw new ThreadTerminateException("Thread interrupted while waiting for Middleware lock.", ex);
	}
    }

    private boolean isPSSAlg(int mechanism) {
	switch (mechanism) {
	    case CryptokiLibrary.CKM_RSA_PKCS_PSS:
	    case CryptokiLibrary.CKM_SHA1_RSA_PKCS_PSS:
	    case CryptokiLibrary.CKM_SHA224_RSA_PKCS_PSS:
	    case CryptokiLibrary.CKM_SHA256_RSA_PKCS_PSS:
	    case CryptokiLibrary.CKM_SHA384_RSA_PKCS_PSS:
	    case CryptokiLibrary.CKM_SHA512_RSA_PKCS_PSS:
		return true;
	    default:return false;
	}
    }

    private int getHashAlg(int pssMechanism, byte[] message) throws CryptokiException {
	switch (pssMechanism) {
	    case CryptokiLibrary.CKM_RSA_PKCS_PSS: return getHashAlg(message);
	    case CryptokiLibrary.CKM_SHA1_RSA_PKCS_PSS: return CryptokiLibrary.CKM_SHA_1;
	    case CryptokiLibrary.CKM_SHA224_RSA_PKCS_PSS: return CryptokiLibrary.CKM_SHA224;
	    case CryptokiLibrary.CKM_SHA256_RSA_PKCS_PSS: return CryptokiLibrary.CKM_SHA256;
	    case CryptokiLibrary.CKM_SHA384_RSA_PKCS_PSS: return CryptokiLibrary.CKM_SHA384;
	    case CryptokiLibrary.CKM_SHA512_RSA_PKCS_PSS: return CryptokiLibrary.CKM_SHA512;
	    default:
		throw new IllegalStateException("Hash determination triggered for non PSS mechanism.");
	}
    }

    private int getHashAlg(byte[] hash) throws CryptokiException {
	switch (hash.length) {
	    case 20: return CryptokiLibrary.CKM_SHA_1;
	    case 28: return CryptokiLibrary.CKM_SHA224;
	    case 32: return CryptokiLibrary.CKM_SHA256;
	    case 48: return CryptokiLibrary.CKM_SHA384;
	    case 64: return CryptokiLibrary.CKM_SHA512;
	    default:
		String msg = "Size of the Hash does not match any supported algorithm.";
		throw new InvalidArgumentsException(msg, CryptokiLibrary.CKR_MECHANISM_PARAM_INVALID);
	}
    }

    private int getHashLen(int hashAlg) {
	switch (hashAlg) {
	    case CryptokiLibrary.CKM_SHA_1: return 20;
	    case CryptokiLibrary.CKM_SHA224: return 28;
	    case CryptokiLibrary.CKM_SHA256: return 32;
	    case CryptokiLibrary.CKM_SHA384: return 48;
	    case CryptokiLibrary.CKM_SHA512: return 64;
	    default: return 20; // as per RFC 3447
	}
    }

    private int getMgf1Alg(int hashAlg) throws CryptokiException {
	switch (hashAlg) {
	    case CryptokiLibrary.CKM_SHA_1: return CryptokiLibrary.CKG_MGF1_SHA1;
	    case CryptokiLibrary.CKM_SHA224: return CryptokiLibrary.CKG_MGF1_SHA224;
	    case CryptokiLibrary.CKM_SHA256: return CryptokiLibrary.CKG_MGF1_SHA256;
	    case CryptokiLibrary.CKM_SHA384: return CryptokiLibrary.CKG_MGF1_SHA384;
	    case CryptokiLibrary.CKM_SHA512: return CryptokiLibrary.CKG_MGF1_SHA512;
	    default:
		String msg = "Hash algorithm is not supported.";
		throw new InvalidArgumentsException(msg, CryptokiLibrary.CKR_MECHANISM_PARAM_INVALID);
	}
    }

    /**
     * Loading the Attribute Value for CKA_LABEL
     * 
     * @return String
     * @throws CryptokiException
     */
    private String loadAttrValueLabel() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_LABEL);
	return AttributeUtils.getString(raw);
    }

    /**
     * Return the Object Handle
     * 
     * @return long
     */
    public long getObjectHandle() {
        return objectHandle;
    }

    /**
     * Returns the MiddlewareWrapper
     * 
     * @return MiddleWareWrapper
     */
    MiddleWareWrapper getMW() {
        return mw;
    }

    /**
     * Returns the Session
     * 
     * @return {@link MwSession}
     */
    public MwSession getSession() {
        return session;
    }

    /**
     * Returns the Private Key Key Label
     * 
     * @return String
     */
    public String getKeyLabel() {
        return keyLabel;
    }


    /**
     * Returns if the Private Key is Sesnsitive
     * 
     * @return boolean
     */
    public Boolean getSensitive() {
        return sensitive;
    }

    /**
     * Returns if the Private Key is Decrypted
     * 
     * @return boolean
     */
    public Boolean getDecrypt() {
        return decrypt;
    }

    /**
     * Returns can Sign
     * 
     * @return boolean
     */
    public Boolean getSign() {
        return sign;
    }

    /**
     * Returns if the Private Key Recover Signs
     * 
     * @return boolean
     */
    public Boolean getSignRecover() {
        return singRecover;
    }

    /**
     * Returns if the Private Key can Unwrap
     * 
     * @return boolean
     */
    public Boolean getUnwrap() {
        return unwrap;
    }

    /**
     * Returns if the Private Key is extractable
     * 
     * @return boolean
     */
    public Boolean getExtractable() {
        return extractable;
    }

    /**
     * Returns if the Private Key is always Sensitive
     * 
     * @return boolean
     */
    public Boolean getAlwaysSensitive() {
        return alwaysSensitive;
    }

    /**
     * Returns if the Private Key is Never Extractable
     * 
     * @return boolean
     */
    public Boolean getNeverExtractable() {
        return neverExtractable;
    }

    /**
     * Returns if the Private Key wrap with trusted
     * 
     * @return boolean
     */
    public Boolean getWrapWithTrusted() {
        return wrapWithTrusted;
    }

    /**
     * Returns if the Private Key can unwrap template
     * 
     * @return
     */
    public long getUnwrapTemplate() {
        return unwrapTemplate;
    }

    /**
     * Returns if the Private Key is always authenticate
     * 
     * @return boolean
     */
    public Boolean getAlwaysAuthenticate() {
        return alwaysAuthenticate;
    }

    @Override
    public String toString() {
	return "PKCS#11 Private Key: {label=" + getKeyLabel() + ", type=" + getKeyTypeName() + "}";
    }

}
