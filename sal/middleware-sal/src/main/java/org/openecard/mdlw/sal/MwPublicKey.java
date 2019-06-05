/****************************************************************************
 * Copyright (C) 2016-2017 ecsec GmbH.
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

import java.io.IOException;
import javax.annotation.Nullable;
import org.bouncycastle.util.Arrays;
import org.openecard.common.util.Promise;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.struct.CkAttribute;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 *
 * @author Jan Mannsbart
 * @author Tobias Wich
 */
public class MwPublicKey extends MwAbstractKey {

    private final Promise<Boolean> encrypt;
    private final Promise<Boolean> verify;
    private final Promise<Boolean> verifyRecover;
    private final Promise<Boolean> wrap;
    private final Promise<Boolean> trusted;
    private final Promise<String> keyLabel;
    private final Promise<byte[]> subject;

    /**
     * Creates new Public Key from given Object Handle
     *
     * @param objectHandle
     * @param mw
     * @param mwSession
     * @throws CryptokiException
     */
    public MwPublicKey(long objectHandle, MiddleWareWrapper mw, MwSession mwSession) throws CryptokiException {
	super(objectHandle, mw, mwSession);
        this.encrypt = new Promise<>();
        this.verify = new Promise<>();
        this.verifyRecover = new Promise<>();
        this.wrap = new Promise<>();
        this.trusted = new Promise<>();
        this.keyLabel = new Promise<>();
        this.subject = new Promise<>();
    }

    /**
     * Loading the Attribute Value for CKA_ENCRYPT
     *
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueEncrypt() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_ENCRYPT);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_VERIFY
     *
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueVerify() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_VERIFY);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_VERIFY_RECOVER
     *
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueVerifyRecover() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_VERIFY_RECOVER);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_WRAP
     *
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueWrap() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_WRAP);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_TRUSTED
     *
     * @return boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValueTrusted() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_TRUSTED);
	return raw != null ? AttributeUtils.getBool(raw) : false;
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
     * Loading the Attribute Value for CKA_SUBJECT
     *
     * @return byte []
     * @throws CryptokiException
     * @throws IOException
     */
    private byte[] loadAttrValueSubject() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_SUBJECT);
	return raw != null ? AttributeUtils.getBytes(raw) : null;
    }

    /**
     * Returns the Private Key Object Handle
     *
     * @return long
     */
    public long getObjectHandle() {
        return objectHandle;
    }

    /**
     * Returns the MiddlewareWrapper
     *
     * @return {@link MiddleWareWrapper}
     */
    public MiddleWareWrapper getmw() {
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
     * Returns if the Public Key is Enrypted
     *
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getEncrypt() throws CryptokiException {
	if (! encrypt.isDelivered()) {
	    encrypt.deliver(loadAttrValueEncrypt());
	}
        return encrypt.derefNonblocking();
    }

    /**
     * Returns if the Public Key is Verifyed
     *
     * @return
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getVerify() throws CryptokiException {
	if (! verify.isDelivered()) {
	    verify.deliver(loadAttrValueVerify());
	}
        return verify.derefNonblocking();
    }

    /**
     * Returns if the Public Key Verifys Recover
     *
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getVerifyRecover() throws CryptokiException {
	if (! verifyRecover.isDelivered()) {
	    verifyRecover.deliver(loadAttrValueVerifyRecover());
	}
        return verifyRecover.derefNonblocking();
    }

    /**
     * Returns if the Public Key is Wrapped
     *
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getWrap() throws CryptokiException {
	if (! wrap.isDelivered()) {
	    wrap.deliver(loadAttrValueWrap());
	}
        return wrap.derefNonblocking();
    }

    /**
     * Returns if the Public Key is Trusted
     *
     * @return boolean
     * @throws CryptokiException Thrown in case the attribute could not be loaded from the middleware.
     */
    public Boolean getTrusted() throws CryptokiException {
	if (! trusted.isDelivered()) {
	    trusted.deliver(loadAttrValueTrusted());
	}
        return trusted.derefNonblocking();
    }

    /**
     * Returns the Key Label
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

    @Nullable
    public byte[] getSubject() throws CryptokiException {
	if (! subject.isDelivered()) {
	    subject.deliver(loadAttrValueSubject());
	}
	return Arrays.clone(subject.derefNonblocking());
    }

    @Override
    public String toString() {
	try {
	    return "PKCS#11 Public Key: {label=" + getKeyLabel() + ", type=" + getKeyTypeName() + "}";
	} catch (CryptokiException ex) {
	    return "PKCS#11 Public Key: not readable";
	}
    }

}
