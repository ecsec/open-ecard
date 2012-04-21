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
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.client.crypto.common.asn1.eac.oid.PACEObjectIdentifier;


/**
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACEDomainParameterInfo implements PACEObjectIdentifier {

    private String protocol;
    private AlgorithmIdentifier domainParameter;
    private int parameterID;
    private static final String[] protocols = new String[]{
	id_PACE_DH_GM,
	id_PACE_DH_IM,
	id_PACE_ECDH_GM,
	id_PACE_ECDH_IM
    };

    /**
     * Creates a new PACEDomainParameterInfo object.
     *
     * @param seq ANS1 encoded data
     */
    public PACEDomainParameterInfo(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    domainParameter = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
	} else if (seq.size() == 3) {
	    protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    domainParameter = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
	    parameterID = ((ASN1Integer) ASN1Integer.getInstance(seq.getObjectAt(2))).getValue().intValue();
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for PACEDomainParameterInfo");
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
     * Returns the PACE domain parameter.
     *
     * @return domain parameter
     */
    public AlgorithmIdentifier getDomainParameter() {
	return domainParameter;
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
     * @return true if oid is a PACE object identifier, otherwise false
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
