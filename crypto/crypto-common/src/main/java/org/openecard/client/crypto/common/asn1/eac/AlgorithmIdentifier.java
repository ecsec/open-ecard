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

import org.openecard.bouncycastle.asn1.ASN1Object;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class AlgorithmIdentifier {

    private String algorithm;
    private ASN1Object parameters;

    /**
     * Instantiates a new algorithm identifier.
     *
     * @param seq ASN1 encoded sequence
     */
    public AlgorithmIdentifier(ASN1Sequence seq) {
	if (seq.size() == 1) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	} else if (seq.size() == 2) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    parameters = (ASN1Object) seq.getObjectAt(1);
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for AlgorithmIdentifier");
	}
    }

    /**
     * Gets the single instance of AlgorithmIdentifier.
     *
     * @param obj
     * @return Single instance of AlgorithmIdentifier
     */
    public static AlgorithmIdentifier getInstance(Object obj) {
	if (obj == null || obj instanceof AlgorithmIdentifier) {
	    return (AlgorithmIdentifier) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new AlgorithmIdentifier((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the object identifier.
     *
     * @return Object identifier
     */
    public String getObjectIdentifier() {
	return algorithm;
    }

    /**
     * Gets the parameters.
     *
     * @return Parameters
     */
    public ASN1Object getParameters() {
	return parameters;
    }

}
