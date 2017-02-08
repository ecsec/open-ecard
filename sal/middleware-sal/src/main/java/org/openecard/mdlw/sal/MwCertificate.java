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
import org.openecard.mdlw.sal.cryptoki.CK_DATE;
import org.openecard.mdlw.sal.struct.CkAttribute;
import javax.annotation.Nullable;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 * 
 * @author Jan Mannsbart
 * @author Tobias Wich
 */
public class MwCertificate {

    private final long objectHandle;
    private final MiddleWareWrapper mw;
    private final MwSession session;

    private final byte[] value;
    private final String label;
    private final byte[] id;
    private final byte[] subject;
    private final byte[] issuer;
    private final long certType;
    private final Boolean trusted;
    private final long certCategory;
    private final byte[] checkValue;
    private final CK_DATE startDate;
    private final CK_DATE endDate;

    /**
     * Creates a MwCertificate Object from the given Objecthandle.
     * 
     * @param objectHandle
     * @param mw
     * @param mwSession
     * @throws CryptokiException
     */
    public MwCertificate(long objectHandle, MiddleWareWrapper mw, MwSession mwSession) throws CryptokiException {
        this.objectHandle = objectHandle;
        this.mw = mw;
        this.session = mwSession;
        this.value = loadAttrValValue();
        this.label = loadAttrValLabel();
        this.id = loadAttrValID();
	this.subject = loadByteArray(CryptokiLibrary.CKA_SUBJECT);
	this.issuer = loadByteArray(CryptokiLibrary.CKA_ISSUER);
        this.certType = loadAttrValCertificateType();
        this.trusted = loadAttrValTrusted();
        this.certCategory = loadAttrValCertificateCategory();
        this.checkValue = loadAttrValCheckValue();
        this.startDate = loadAttrValStartDate();
        this.endDate = loadAttrValEndDate();
    }

    private byte[] loadByteArray(int type) throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, type);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Loading the Attribute Value for CKA_VALUE
     * 
     * @return byte []
     * @throws CryptokiException
     */
    private byte[] loadAttrValValue() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_VALUE);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Loading the Attribute Value for CKA_LABEL
     * 
     * @return String
     * @throws CryptokiException
     */
    private String loadAttrValLabel() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_LABEL);
	return AttributeUtils.getString(raw);
    }

    /**
     * Loading the Attribute Value for CKA_ID
     * 
     * @return byte []
     * @throws CryptokiException
     */
    private byte[] loadAttrValID() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_ID);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Loading the Attribute Value for CKA_CERTIFICATE_TYPE
     * 
     * @return NativeLong
     * @throws CryptokiException
     */
    private long loadAttrValCertificateType() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_CERTIFICATE_TYPE);
	return AttributeUtils.getLong(raw);
    }

    /**
     * Loading the Attribute Value for CKA_TRUSTED
     * 
     * @return Boolean
     * @throws CryptokiException
     */
    private Boolean loadAttrValTrusted() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_TRUSTED);
	return AttributeUtils.getBool(raw);
    }

    /**
     * Loading the Attribute Value for CKA_CERTIFICATE_CATEGORY
     * 
     * @return NativeLong
     * @throws CryptokiException
     */
    private long loadAttrValCertificateCategory() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_CERTIFICATE_CATEGORY);
	return AttributeUtils.getLong(raw);
    }

    /**
     * Loading the Attribute Value for CKA_CHECK_VALUE
     * 
     * @return byte []
     * @throws CryptokiException
     */
    private byte[] loadAttrValCheckValue() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_CHECK_VALUE);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Loading the Attribute Value for CKA_START_DATE
     * 
     * @return CK_DATE
     * @throws CryptokiException
     */
    private CK_DATE loadAttrValStartDate() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_START_DATE);
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    return new CK_DATE(raw.getData());
	} else {
	    return null;
	}
    }

    /**
     * Loading the Attribute Value for CKA_END_DATE
     * 
     * @return CK_DATE
     * @throws CryptokiException
     */
    private CK_DATE loadAttrValEndDate() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_END_DATE);
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    return new CK_DATE(raw.getData().getPointer(0));
	} else {
	    return null;
	}
    }

    /**
     * Returns the Object Handle.
     * 
     * @return long
     */
    public long getObjectHandle() {
        return objectHandle;
    }

    /**
     * Returns the MiddlewareWrapper.
     * 
     * @return MiddlewareWrapper
     */
    public MiddleWareWrapper getMW() {
        return mw;
    }

    /**
     * Returns the Session Identifier
     * 
     * @return long
     */
    public MwSession getSession() {
        return session;
    }

    /**
     * Returns the Certificate Label
     * 
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the Certificate Identifier
     * 
     * @return byte[]
     */
    public byte[] getID() {
        return id;
    }

    public byte[] getSubject() {
	return subject;
    }

    public byte[] getIssuer() {
	return issuer;
    }

    /**
     * Returns the Type of Certificate
     * 
     * @return String
     * 
     * 
     *         Return Options: CKC_X509, CKC_X_509_ATTR_CERT, CKC_WTLS,
     *         CKC_VENDOR_DEFINED or UNKNOWN
     * 
     */
    public String getCertificateType() {
	switch ((int) certType) {
	    case (int) 0x00000000L:
		return "CKC_X_509";
	    case (int) 0x00000001L:
		return "CKC_X_509_ATTR_CERT";
	    case (int) 0x00000002L:
		return "CKC_WTLS";
	    case (int) 0x80000000L:
		return "CKC_VENDOR_DEFINED";
	    default:
		return "UNKNOWN";
	}
    }

    /**
     * Returns if the Certificate is trusted
     * 
     * @return Boolean
     */
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * Return the Category of Certificate
     * 
     * @return String
     * 
     *         Return Options: CK_CERTIFICATE_CATEGORY_UNSPECIFIED,
     *         CK_CERTIFICATE_CATEGORY_TOKEN_USER,
     *         CK_CERTIFICATE_CATEGORY_AUTHORITY,
     *         CK_CERTIFICATE_CATEGORY_OTHER_ENTITY or UNKNOWN
     */
    public String getCertificateCategory() {
	switch ((int) certCategory) {
	    case (int) 0x00000000L:
		return "CK_CERTIFICATE_CATEGORY_UNSPECIFIED";
	    case (int) 0x00000001L:
		return "CK_CERTIFICATE_CATEGORY_TOKEN_USER";
	    case (int) 0x00000002L:
		return "CK_CERTIFICATE_CATEGORY_AUTHORITY";
	    case (int) 0x80000000L:
		return "CK_CERTIFICATE_CATEGORY_OTHER_ENTITY";
	    default:
		return "UNKNOWN";
	}
    }

    /**
     * Return the Certificate Checkvalue
     * 
     * @return byte[]
     */
    @Nullable
    public byte[] getCheckValue() {
        return checkValue;
    }

    /**
     * Returns the Certificate Start Date
     * 
     * @return CK_DATE
     */
    public CK_DATE getStartDate() {
        return startDate;
    }

    /**
     * Returns the Certificate End Date
     * 
     * @return CK_DATE
     */
    public CK_DATE getEndDate() {
        return endDate;
    }

    /**
     * Return the Certificate Value
     * 
     * @return byte[]
     */
    public byte[] getValue() {
        return value;
    }

}
