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
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class TAInfo extends SecurityInfo implements TAObjectIdentifier {

    private String protocol;
    private int version;
    private FileID efCVCA;
    private static final String[] protocols = new String[]{
	id_TA_ECDSA_SHA_1,
	id_TA_ECDSA_SHA_224,
	id_TA_ECDSA_SHA_256,
	id_TA_ECDSA_SHA_384,
	id_TA_ECDSA_SHA_512,
	id_TA_RSA_PSS_SHA_1,
	id_TA_RSA_PSS_SHA_256,
	id_TA_RSA_PSS_SHA_512,
	id_TA_RSA_v1_5_SHA_1,
	id_TA_RSA_v1_5_SHA_256,
	id_TA_RSA_v1_5_SHA_512
    };

    /**
     * Creates a new TAInfo object. See TR-03110 Section A.1.1.3.
     *
     * @param seq ANS1 encoded sequence
     */
    public TAInfo(ASN1Sequence seq) {
	super(seq);

	protocol = getIdentifier();
	version = ((ASN1Integer) getRequiredData()).getValue().intValue();
	if (seq.size() == 3) {
	    efCVCA = FileID.getInstance(getOptionalData());
	}
    }

    /**
     * Checks if the protocol identifier indicates ECDSA.
     *
     * @return True if ECDSA is used, otherwise false
     */
    public boolean isECDSA() {
	if (protocol.startsWith(id_TA_ECDSA)) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if the protocol identifier indicates elliptic curve RSA.
     *
     * @return True if elliptic curve RSA is used, otherwise false
     */
    public boolean isRSA() {
	if (protocol.startsWith(id_TA_RSA)) {
	    return true;
	}
	return false;
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
     * @return version
     */
    public int getVersion() {
	return version;
    }

    /**
     * Returns the EF.CVCA.
     *
     * @return EF.CVCA
     */
    public FileID getEFCVCA() {
	return efCVCA;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if o is a TA object identifier; false otherwise
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
