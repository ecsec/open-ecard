/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

import java.util.StringTokenizer;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
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
	TLV tlv = new TLV();
	tlv.setTagNum((byte) 0x06);
	tlv.setValue(getValue(oid));

	return tlv.toBER();
    }

    /**
     * Converts a ASN1 object identifier to a byte array
     *
     * @param oid String
     * @return TLV encoded object identifier
     * @throws TLVException
     */
    public static String toString(byte[] oid) throws TLVException {
	StringBuilder sb = new StringBuilder(32);

	if (oid[0] == (byte) 0x06) {
	    oid = TLV.fromBER(oid).getValue();
	}

	switch ((oid[0] & 0x7f) / 40) {
	    case 0:
		sb.append('0');
		break;
	    case 1:
		sb.append('1');
		break;
	    default:
		sb.append('2');
	}

	sb.append('.');

	for (int i = 0; i < oid.length; i++) {
	    sb.append(oid[i]);
	    if (i < oid.length - 1) {
		sb.append(".");
	    }
	}

	return sb.toString();
    }

    /**
     * Converts a ASN1 object identifier to a byte array. Returns only the value
     * without the length and 0x06 tag.
     *
     * @param oid String
     * @return Value of the object identifier
     */
    public static byte[] getValue(String oid) {
	StringTokenizer st = new StringTokenizer(oid, ".");
	byte[] ret = new byte[st.countTokens() - 1];

	// Skip leading null
	//FIXME
	if (oid.startsWith("0")) {
	    st.nextElement();
	}

	for (int i = 0; st.hasMoreElements(); i++) {
	    int t = Integer.valueOf((String) st.nextElement());
	    ret[i] = (byte) (t & 0xFF);
	}

	return ret;
    }

}
