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

import java.io.IOException;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.struct.CkAttribute;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 *
 * @author Jan Mannsbart
 * @author Tobias Wich
 */
public class MwPublicKey extends MwAbstractKey {

    private final Boolean encrypt;
    private final Boolean verify;
    private final Boolean verifyRecover;
    private final Boolean wrap;
    private final Boolean trusted;
    private final String keyLabel;
    private final byte[] subject;

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
        this.encrypt = loadAttrValueEncrypt();
        this.verify = loadAttrValueVerify();
        this.verifyRecover = loadAttrValueVerifyRecover();
        this.wrap = loadAttrValueWrap();
        this.trusted = loadAttrValueTrusted();
        this.keyLabel = loadAttrValueLabel();
        this.subject = loadAttrValueSubject();
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
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_TRUSTED);
	return AttributeUtils.getBool(raw);
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
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_SUBJECT);
	return AttributeUtils.getBytes(raw);
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
     */
    public Boolean getEncrypt() {
        return encrypt;
    }

    /**
     * Returns if the Public Key is Verifyed
     * 
     * @return
     */
    public Boolean getVerify() {
        return verify;
    }

    /**
     * Returns if the Public Key Verifys Recover
     * 
     * @return boolean
     */
    public Boolean getVerify_Recover() {
        return verifyRecover;
    }

    /**
     * Returns if the Public Key is Wrapped
     * 
     * @return boolean
     */
    public Boolean getWrap() {
        return wrap;
    }

    /**
     * Returns if the Public Key is Trusted
     * 
     * @return boolean
     */
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * Returns the Key Label
     * 
     * @return String
     */
    public String getKeyLabel() {
        return keyLabel;
    }

    @Override
    public String toString() {
	return "PKCS#11 Public Key: {label=" + getKeyLabel() + ", type=" + getKeyTypeName() + "}";
    }

}
