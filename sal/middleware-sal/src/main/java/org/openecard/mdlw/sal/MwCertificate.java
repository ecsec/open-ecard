/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.cryptoki.CK_DATE;
import org.openecard.mdlw.sal.struct.CkAttribute;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import org.openecard.bouncycastle.util.Arrays;
import org.openecard.common.util.ByteUtils;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Jan Mannsbart
 * @author Tobias Wich
 */
public class MwCertificate {
    
    private static final Logger LOG = LoggerFactory.getLogger(MwCertificate.class);

    private final long objectHandle;
    private final MiddleWareWrapper mw;
    private final MwSession session;

    private final byte[] value;
    private final byte[] id;
    private final byte[] subject;
    private final byte[] issuer;
    private final long certType;
    private final Boolean trusted;
    private final long certCategory;
    private final byte[] checkValue;
    private final Calendar startDate;
    private final Calendar endDate;
    private final String label;

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
        this.id = loadAttrValID();
	this.subject = loadByteArray(CryptokiLibrary.CKA_SUBJECT);
	this.issuer = loadByteArray(CryptokiLibrary.CKA_ISSUER);
        this.certType = loadAttrValCertificateType();
        this.trusted = loadAttrValTrusted();
        this.certCategory = loadAttrValCertificateCategory();
        this.checkValue = loadAttrValCheckValue();
        this.startDate = loadAttrValStartDate();
        this.endDate = loadAttrValEndDate();
        this.label = loadAttrValLabel();
    }

    @Nullable
    private CkAttribute getAttributeChecked(int type) throws CryptokiException {
	try {
	    return mw.getAttributeValue(session.getSessionId(), objectHandle, type);
	} catch (CryptokiException ex) {
	    switch ((int) ex.getErrorCode()) {
		case CryptokiLibrary.CKR_ATTRIBUTE_TYPE_INVALID:
		    String ts = String.format("%#010x", type);
		    LOG.debug("Error retrieving attribute value (type={}), but ignoring it: {}", ts, ex.getMessage());
		    return null;
	    }

	    throw ex;
	}
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
	String labelStr = AttributeUtils.getString(raw);

	// find replacement if needed
	if (labelStr == null && subject != null) {
	    labelStr = new X500Principal(subject).getName(X500Principal.RFC2253);
	}
	if (labelStr == null && value != null) {
	    try {
		byte[] hash = MessageDigest.getInstance("SHA-1").digest(value);
		labelStr = ByteUtils.toHexString(hash);
	    } catch (NoSuchAlgorithmException ex) {
		// SHA-1 is a standard name and must be present
	    }
	}

	return labelStr;
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
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_TRUSTED);
	return raw != null ? AttributeUtils.getBool(raw) : null;
    }

    /**
     * Loading the Attribute Value for CKA_CERTIFICATE_CATEGORY
     *
     * @return NativeLong
     * @throws CryptokiException
     */
    private long loadAttrValCertificateCategory() throws CryptokiException {
	CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_CERTIFICATE_CATEGORY);
	// default is 0 meaning undefined type
	return raw != null ? AttributeUtils.getLong(raw) : 0;
    }

    /**
     * Loading the Attribute Value for CKA_CHECK_VALUE
     * 
     * @return byte []
     * @throws CryptokiException
     */
    private byte[] loadAttrValCheckValue() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_CHECK_VALUE);
	return raw != null ? AttributeUtils.getBytes(raw) : null;
    }

    /**
     * Loading the Attribute Value for CKA_START_DATE
     * 
     * @return Calendar object with UTC based timezone.
     * @throws CryptokiException
     */
    private Calendar loadAttrValStartDate() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_START_DATE);
	return convertCalendar(raw);
    }

    /**
     * Loading the Attribute Value for CKA_END_DATE
     * 
     * @return Calendar object with UTC based timezone.
     * @throws CryptokiException
     */
    private Calendar loadAttrValEndDate() throws CryptokiException {
        CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_END_DATE);
	return convertCalendar(raw);
    }

    private Calendar convertCalendar(CkAttribute raw) {
	int dataLen = raw != null ? raw.getLength().intValue() : 0;
	if (dataLen > 0) {
	    assert(raw != null);
	    CK_DATE d = new CK_DATE(raw.getData());
	    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	    cal.set(Integer.parseInt(new String(d.getYear())),
		    Integer.parseInt(new String(d.getMonth())) - 1, // 0 based
		    Integer.parseInt(new String(d.getDay())));
	    return cal;
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
    @Nullable
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * Return the Category of Certificate
     *
     * @return CertCategory
     */
    public CertCategory getCertificateCategory() {
	return CertCategory.forCategoryType(certCategory);
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
    @Nullable
    public Calendar getStartDate() {
        return startDate;
    }

    /**
     * Returns the Certificate End Date
     * 
     * @return CK_DATE
     */
    @Nullable
    public Calendar getEndDate() {
        return endDate;
    }

    /**
     * Return the Certificate Value
     * 
     * @return byte[]
     */
    @Nonnull
    public byte[] getValue() {
        return Arrays.clone(value);
    }

}
