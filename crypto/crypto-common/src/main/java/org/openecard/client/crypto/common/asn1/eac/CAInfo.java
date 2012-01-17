/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.eac;

import org.openecard.bouncycastle.asn1.ASN1Integer;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.client.crypto.common.asn1.eac.oid.CAObjectIdentifier;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public final class CAInfo extends SecurityInfo implements CAObjectIdentifier {

    private String protocol;
    private int version;
    private int keyID;
    private static final String[] protocols = new String[]{
        id_CA_DH_3DES_CBC_CBC,
        id_CA_DH_AES_CBC_CMAC_128,
        id_CA_DH_AES_CBC_CMAC_192,
        id_CA_DH_AES_CBC_CMAC_256,
        id_CA_ECDH_3DES_CBC_CBC,
        id_CA_ECDH_AES_CBC_CMAC_128,
        id_CA_ECDH_AES_CBC_CMAC_192,
        id_CA_ECDH_AES_CBC_CMAC_256
    };

    /**
     * Creates a new ChipAuthenticationInfo object. See TR-03110 Section A.1.1.2.
     *
     * @param seq ANS1 encoded data
     */
    public CAInfo(ASN1Sequence seq) {
        super(seq);
        protocol = getIdentifier();
        version = ((ASN1Integer) getRequiredData()).getValue().intValue();
        if (seq.size() == 3) {
            keyID = ((ASN1Integer) getOptionalData()).getValue().intValue();
        }
    }

    /**
     * Returns the object identifier of the protocol.
     *
     * @return Protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the version of the protocol.
     *
     * @return Version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the key identifier.
     *
     * @return KeyID
     */
    public int getKeyID() {
        return keyID;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if o is a ChipAuthentication object identifier; false otherwise
     */
    public static boolean isObjectIdentifier(String oid) {
        for (int i = 0; i < protocols.length; i++) {
            if (protocols[i].equals(oid)) {
                return true;
            }
        }
        return false;
    }
}
