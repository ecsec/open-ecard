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

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.DERBitString;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SubjectPublicKeyInfo {

    private String algorithm;
    private byte[] subjectPublicKey;

    /**
     * Instantiates a new SubjectPublicKeyInfo.
     *
     * @param seq the ASN1 encoded sequence
     */
    public SubjectPublicKeyInfo(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    subjectPublicKey = DERBitString.getInstance(seq.getObjectAt(1)).getBytes();
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for SubjectPublicKeyInfo");
	}
    }

    /**
     * Gets the single instance of SubjectPublicKeyInfo.
     *
     * @param obj
     * @return single instance of SubjectPublicKeyInfo
     */
    public static SubjectPublicKeyInfo getInstance(Object obj) {
	if (obj == null || obj instanceof SubjectPublicKeyInfo) {
	    return (SubjectPublicKeyInfo) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new SubjectPublicKeyInfo((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the algorithm.
     *
     * @return the algorithm
     */
    public String getAlgorithm() {
	return algorithm;
    }

    /**
     * Gets the subject public key.
     *
     * @return the subject public key
     */
    public byte[] getSubjectPublicKey() {
	return subjectPublicKey;
    }

}
