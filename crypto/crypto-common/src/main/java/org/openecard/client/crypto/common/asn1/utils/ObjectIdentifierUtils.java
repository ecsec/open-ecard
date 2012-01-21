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


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public final class ObjectIdentifierUtils {

    /**
     * Converts a ASN1 object identifier to a byte array
     * @param oid String
     * @return TLV encoded object identifier
     */
    public static byte[] toByteArray(String oid) {
        return TLV.encode((byte) 0x06, getValue(oid));
    }

    /**
     * Converts a ASN1 object identifier to a byte array. Returns only the value
     * without the length and 0x06 tag.
     * @param oid String
     * @return Value of the object identifier
     */
    public static byte[] getValue(String oid) {
        StringTokenizer st = new StringTokenizer(oid, ".");
        byte[] ret = new byte[st.countTokens() - 1];

        // Skip leading null byte
        st.nextElement();

        for (int i = 0; st.hasMoreElements(); i++) {
            int t = Integer.valueOf((String) st.nextElement());
            ret[i] = (byte) (t & 0xFF);
        }

        return ret;
    }
}
