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

import javax.annotation.Nullable;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.struct.CkAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class MwAbstractKey {

    private static final Logger LOG = LoggerFactory.getLogger(MwAbstractKey.class);

    protected final long objectHandle;
    protected final MiddleWareWrapper mw;
    protected final MwSession session;

    private final long keyType;
    private final byte[] keyId;
    private final long[] allowedMechanisms;

    public MwAbstractKey(long objectHandle, MiddleWareWrapper mw, MwSession session) throws CryptokiException {
	this.objectHandle = objectHandle;
	this.mw = mw;
	this.session = session;

        this.keyType = loadAttrValueKeyType();
	this.keyId = loadAttrValueKeyID();
	this.allowedMechanisms = loadAttrValAllowedMechanisms();
    }

    @Nullable
    protected CkAttribute getAttributeChecked(int type) throws CryptokiException {
	try {
	    return mw.getAttributeValue(session.getSessionId(), objectHandle, type);
	} catch (CryptokiException ex) {
	    switch ((int) ex.getErrorCode()) {
		case CryptokiLibrary.CKR_ATTRIBUTE_TYPE_INVALID:
		    String ts = String.format("%#08X", type);
		    LOG.debug("Error retrieving attribute value (type={}), but ignoring it: {}", ts, ex.getMessage());
		    return null;
	    }

	    throw ex;
	}
    }

    /**
     * Loading the Attribute Value for CKA_KEY_TYPE
     *
     * @return NativeLong
     * @throws CryptokiException
     */
    private long loadAttrValueKeyType() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_KEY_TYPE);
	return AttributeUtils.getLong(raw);
    }

    /**
     * Loading the Attribute Value for CKA_ID
     *
     * @return byte[]
     * @throws CryptokiException
     */
    private byte[] loadAttrValueKeyID() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_ID);
	return AttributeUtils.getBytes(raw);
    }

    private long[] loadAttrValAllowedMechanisms() throws CryptokiException {
	CkAttribute raw = getAttributeChecked(CryptokiLibrary.CKA_ALLOWED_MECHANISMS);
	if (raw != null) {
	    return AttributeUtils.getLongs(raw);
	} else {
	    LOG.warn("Failed to read allowed mechanisms from key object.");
	    return new long[0];
	}
    }

    public long getKeyType() {
	return keyType;
    }

    /**
     * Return the Key Type, for example CKK_RSA
     *
     * @return String
     */
    public String getKeyTypeName() {
        switch ((int) keyType) {
        case (int) 0x00000000L:
            return "CKK_RSA";
        case (int) 0x00000001L:
            return "CKK_DSA";
        case (int) 0x00000002L:
            return "CKK_ECDSA";
        case (int) 0x00000003L:
            return "CKK_EC";
        case (int) 0x00000004L:
            return "CKK_X9_42_DH";
        case (int) 0x00000005L:
            return "CKK_KEA";
        case (int) 0x00000010L:
            return "CKK_GENERIC_SECRET";
        case (int) 0x00000011L:
            return "CKK_RC2";
        case (int) 0x00000012L:
            return "CKK_RC4";
        case (int) 0x00000013L:
            return "CKK_DES";
        case (int) 0x00000014L:
            return "CKK_DES2";
        case (int) 0x00000015L:
            return "CKK_DES3";
        case (int) 0x00000016L:
            return "CKK_CAST";
        case (int) 0x00000017L:
            return "CKK_CAST3";
        case (int) 0x00000018L:
            return "CKK_CAST128";
        case (int) 0x00000019L:
            return "CKK_RC5";
        case (int) 0x0000001AL:
            return "CKK_IDEA";
        case (int) 0x0000001BL:
            return "CKK_SKIPJACK";
        case (int) 0x0000001CL:
            return "CKK_BATON";
        case (int) 0x0000001DL:
            return "CKK_JUNIPER";
        case (int) 0x0000001EL:
            return "CKK_CDMF";
        case (int) 0x0000001FL:
            return "CKK_AES";
        case (int) 0x00000020L:
            return "CKK_BLOWFISH";
        case (int) 0x00000021L:
            return "CKK_TWOFISH";
        case (int) 0x00000022L:
            return "CKK_SECURID";
        case (int) 0x00000023L:
            return "CKK_HOTP";
        case (int) 0x00000024L:
            return "CKK_ACTI";
        case (int) 0x00000025L:
            return "CKK_CAMELLIA";
        case (int) 0x00000026L:
            return "CKK_ARIA";
        case (int) 0x00000027L:
            return "CKK_MD5_HMAC";
        case (int) 0x00000028L:
            return "CKK_SHA_1_HMAC";
        case (int) 0x00000029L:
            return "CKK_RIPEMD128_HMAC";
        case (int) 0x0000002AL:
            return "CKK_RIPEMD160_HMAC";
        case (int) 0x0000002BL:
            return "CKK_SHA256_HMAC";
        case (int) 0x0000002CL:
            return "CKK_SHA384_HMAC";
        case (int) 0x0000002DL:
            return "CKK_SHA512_HMAC";
        case (int) 0x0000002EL:
            return "CKK_SHA224_HMAC";
        case (int) 0x0000002FL:
            return "CKK_SEED";
        case (int) 0x00000030L:
            return "CKK_GOSTR3410";
        case (int) 0x00000031L:
            return "CKK_GOSTR3411";
        case (int) 0x00000032L:
            return "CKK_GOST28147";
        default:
            return "Unknown key mechanism";
        }
    }

    /**
     * Returns the Private Key Key Identifier
     *
     * @return
     */
    public byte[] getKeyID() {
        return keyId;
    }

    /**
     * Returns the allowed mechanisms for this key object.
     *
     * @return Array containing the mechanism IDs.
     */
    public long[] getAllowedMechanisms() {
	return allowedMechanisms;
    }

}
