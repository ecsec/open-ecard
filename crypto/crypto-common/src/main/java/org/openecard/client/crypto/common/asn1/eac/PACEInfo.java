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
import org.openecard.client.crypto.common.asn1.eac.oid.PACEObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 *
 */
public final class PACEInfo extends SecurityInfo implements PACEObjectIdentifier {

    private String protocol;
    private int version;
    private int parameterID = -1;
    private static final String[] protocols = new String[]{
        id_PACE_DH_GM_3DES_CBC_CBC,
        id_PACE_DH_GM_AES_CBC_CMAC_128,
        id_PACE_DH_GM_AES_CBC_CMAC_192,
        id_PACE_DH_GM_AES_CBC_CMAC_256,
        id_PACE_DH_IM_3DES_CBC_CBC,
        id_PACE_DH_IM_AES_CBC_CMAC_128,
        id_PACE_DH_IM_AES_CBC_CMAC_192,
        id_PACE_DH_IM_AES_CBC_CMAC_256,
        id_PACE_ECDH_GM_3DES_CBC_CBC,
        id_PACE_ECDH_GM_AES_CBC_CMAC_128,
        id_PACE_ECDH_GM_AES_CBC_CMAC_192,
        id_PACE_ECDH_GM_AES_CBC_CMAC_256,
        id_PACE_ECDH_IM_3DES_CBC_CBC,
        id_PACE_ECDH_IM_AES_CBC_CMAC_128,
        id_PACE_ECDH_IM_AES_CBC_CMAC_192,
        id_PACE_ECDH_IM_AES_CBC_CMAC_256
    };

    /**
     * Creates a new PACEInfo object. See BSI-TR-03110, version 2.05, section A.1.1.1.
     *
     * @param seq ANS1 encoded data
     */
    public PACEInfo(ASN1Sequence seq) {
        super(seq);
        protocol = getIdentifier();
        version = ((ASN1Integer) getRequiredData()).getValue().intValue();
        if (seq.size() == 3) {
            parameterID = ((ASN1Integer) getOptionalData()).getValue().intValue();
        }
    }

    /**
     * Returns the object identifier of the protocol.
     *
     * @return Protocol object identifier
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the version of the protocol.
     *
     * @return version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the parameter identifier.
     *
     * @return parameter identifier
     */
    public int getParameterID() {
        return parameterID;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if oid is a PACE object identifier; false otherwise
     */
    public static boolean isPACEObjectIdentifer(String oid) {
        for (int i = 0; i < protocols.length; i++) {
            if (protocols[i].equals(oid)) {
                return true;
            }
        }

        return false;
    }
}
