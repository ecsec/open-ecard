/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.crypto.common.asn1.utils;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;


/**
 * Helper class to convert ASN1 object identifier.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class ObjectIdentifierUtils {

    /**
     * Converts a ASN1 object identifier to a byte array
     *
     * @param oid String
     * @return TLV encoded object identifier
     * @throws TLVException
     */
    public static byte[] toByteArray(String oid) throws TLVException {
	try {
	    return new ASN1ObjectIdentifier(oid).getEncoded();
	} catch (IllegalArgumentException | IOException ex) {
	    throw new TLVException("Failed to parse OID.", ex);
	}
    }

    /**
     * Converts a ASN1 object identifier in byte array form to a String.
     *
     * @param oid Object Identifier either as TLV structure or plain.
     * @return Object Identifier string.
     * @throws TLVException
     */
    public static String toString(byte[] oid) throws TLVException {
	try {
	    TLV obj;
	    try {
		obj = TLV.fromBER(oid);
	    } catch (TLVException ex) {
		// read as plain value
		obj = new TLV();
		obj.setTagNum(0x06);
		obj.setValue(oid);
	    }
	    return ASN1ObjectIdentifier.getInstance(obj.toBER()).getId();
	} catch (IllegalArgumentException ex) {
	    throw new TLVException("Failed to parse OID.", ex);
	}
    }

    /**
     * Converts a ASN1 object identifier to a byte array.
     * Returns only the value without the length and 0x06 tag.
     *
     * @param oid String
     * @return Value of the object identifier
     * @throws TLVException
     */
    public static byte[] getValue(String oid) throws TLVException {
	try {
	    ASN1ObjectIdentifier oidObj = new ASN1ObjectIdentifier(oid);
	    TLV oidTlv = TLV.fromBER(oidObj.getEncoded());
	    return oidTlv.getValue();
	} catch (IllegalArgumentException | IOException ex) {
	    throw new TLVException("Failed to parse OID.", ex);
	}
    }

}
