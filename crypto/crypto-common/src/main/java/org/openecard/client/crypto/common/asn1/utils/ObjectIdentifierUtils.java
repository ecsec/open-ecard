/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.utils;

import java.util.StringTokenizer;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;


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
	StringBuilder sb = new StringBuilder();

	if (oid[0] == (byte) 0x06) {
	    oid = TLV.fromBER(oid).getValue();
	}

	switch ((int) (oid[0] & 0x7f) / 40) {
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
