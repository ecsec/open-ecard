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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.util.Promise;
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

    private final Promise<String> keyLabel;
    private final Promise<Boolean> sensitive;
    private final Promise<Boolean> decrypt;
    private final Promise<Boolean> sign;
    private final Promise<Boolean> singRecover;
    private final Promise<Boolean> unwrap;
    private final Promise<Boolean> extractable;
    private final Promise<Boolean> alwaysSensitive;
    private final Promise<Boolean> neverExtractable;
    private final Promise<Boolean> wrapWithTrusted;
    private final Promise<Long> unwrapTemplate;
    private final Promise<Boolean> alwaysAuthenticate;

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
        this.keyLabel = new Promise<>();
        this.sensitive = new Promise<>();
        this.decrypt = new Promise<>();
        this.sign = new Promise<>();
        this.singRecover = new Promise<>();
        this.unwrap = new Promise<>();
        this.extractable = new Promise<>();
        this.alwaysSensitive = new Promise<>();
        this.neverExtractable = new Promise<>();
        this.wrapWithTrusted = new Promise<>();
        this.unwrapTemplate = new Promise<>();
        this.alwaysAuthenticate = new Promise<>();
    }

    /**
     * Loading the Attribute Value for CKA_ALWAYS_AUTHENTICATE
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueAlwaysAuthenticate() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_ALWAYS_AUTHENTICATE);
	return raw != null ? AttributeUtils.getBool(raw) : false;
    }

    /**
     * Loading the Attribute Value for CKA_UNWRAP_TEMPLATE
     * 
     * @return long
     * @throws CryptokiException
     */
    private Long loadAttrValueUnwrapTemplate() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_UNWRAP_TEMPLATE);
	return raw != null ? AttributeUtils.getLong(raw) : null;
    }

    /**
     * Loading the Attribute Value for CKA_WRAP_WITH_TRUSTED
     * 
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueWrapWithTrusted() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_WRAP_WITH_TRUSTED);
	return raw != null ? AttributeUtils.getBool(raw) : false;
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
     * Signs Data with a {@link SignatureAlgorithms}.
     * Returns the signed Data in an byte array.
     *
     * @param algo
     * @param data
     * @return
     * @throws CryptokiException
     */
    public byte[] sign(SignatureAlgorithms algo, byte[] data) throws CryptokiException {
	signInit(algo.getPkcs11Mechanism(), data);
	return sign(data);
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
	signInit(mechanism, data);
	return sign(data);
    }

    /**
     * Signs Data
     * Returns the signed Data in an byte array.
     *
     * @param data
     * @return
     * @throws CryptokiException
     */
    public byte[] sign(byte[] data) throws CryptokiException {

	try (MiddleWareWrapper.LockedMiddlewareWrapper lmw = mw.lock()) {
	    return lmw.sign(session.getSessionId(), data);
	} catch (InterruptedException ex) {
	    throw new ThreadTerminateException("Thread interrupted while waiting for Middleware lock.", ex);
	}
    }

    /**
     * Initializes the signing process
     *
     * @param mechanism
     * @param data
     * @throws CryptokiException
     */
    public void signInit(long mechanism, byte[] data) throws CryptokiException {

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
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public String getKeyLabel() throws CryptokiException {
	if (! keyLabel.isDelivered()) {
	    keyLabel.deliver(loadAttrValueLabel());
	}
        return keyLabel.derefNonblocking();
    }


    /**
     * Returns if the Private Key is Sesnsitive
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getSensitive() throws CryptokiException {
	if (! sensitive.isDelivered()) {
	    sensitive.deliver(loadAttrValueSensitive());
	}
        return sensitive.derefNonblocking();
    }

    /**
     * Returns if the Private Key is Decrypted
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getDecrypt() throws CryptokiException {
	if (! decrypt.isDelivered()) {
	    decrypt.deliver(loadAttrValueDecrypt());
	}
        return decrypt.derefNonblocking();
    }

    /**
     * Returns can Sign
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getSign() throws CryptokiException {
	if (! sign.isDelivered()) {
	    sign.deliver(loadAttrValueSign());
	}
        return sign.derefNonblocking();
    }

    /**
     * Returns if the Private Key Recover Signs
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getSignRecover() throws CryptokiException {
	if (! singRecover.isDelivered()) {
	    singRecover.deliver(loadAttrValueSignRecover());
	}
        return singRecover.derefNonblocking();
    }

    /**
     * Returns if the Private Key can Unwrap
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getUnwrap() throws CryptokiException {
	if (! unwrap.isDelivered()) {
	    unwrap.deliver(loadAttrValueUnwrap());
	}
        return unwrap.derefNonblocking();
    }

    /**
     * Returns if the Private Key is extractable
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getExtractable() throws CryptokiException {
	if (! extractable.isDelivered()) {
	    extractable.deliver(loadAttrValueExtractable());
	}
        return extractable.derefNonblocking();
    }

    /**
     * Returns if the Private Key is always Sensitive
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getAlwaysSensitive() throws CryptokiException {
	if (! alwaysSensitive.isDelivered()) {
	    alwaysSensitive.deliver(loadAttrValueAlwaysSensitive());
	}
        return alwaysSensitive.derefNonblocking();
    }

    /**
     * Returns if the Private Key is Never Extractable
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getNeverExtractable() throws CryptokiException {
	if (! neverExtractable.isDelivered()) {
	    neverExtractable.deliver(loadAttrValueNeverExtractable());
	}
        return neverExtractable.derefNonblocking();
    }

    /**
     * Returns if the Private Key wrap with trusted
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    @Nonnull
    public Boolean getWrapWithTrusted() throws CryptokiException {
	if (! wrapWithTrusted.isDelivered()) {
	    wrapWithTrusted.deliver(loadAttrValueWrapWithTrusted());
	}
        return wrapWithTrusted.derefNonblocking();
    }

    /**
     * Returns if the Private Key can unwrap template
     * 
     * @return
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    @Nullable
    public Long getUnwrapTemplate() throws CryptokiException {
	if (! unwrapTemplate.isDelivered()) {
	    unwrapTemplate.deliver(loadAttrValueUnwrapTemplate());
	}
        return unwrapTemplate.derefNonblocking();
    }

    /**
     * Returns if the Private Key is always authenticate
     * 
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    @Nonnull
    public Boolean getAlwaysAuthenticate() throws CryptokiException {
	if (! alwaysAuthenticate.isDelivered()) {
	    alwaysAuthenticate.deliver(loadAttrValueAlwaysAuthenticate());
	}
        return alwaysAuthenticate.derefNonblocking();
    }

    @Override
    public String toString() {
	try {
	    return "PKCS#11 Private Key: {label=" + getKeyLabel() + ", type=" + getKeyTypeName() + "}";
	} catch (CryptokiException ex) {
	    return "PKCS#11 Private Key: not readable";
	}
    }

}
