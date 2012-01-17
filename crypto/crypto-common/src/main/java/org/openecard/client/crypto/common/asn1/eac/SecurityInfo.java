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

import org.openecard.bouncycastle.asn1.ASN1Encodable;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;

/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class SecurityInfo {

    private ASN1ObjectIdentifier identifier;
    private ASN1Encodable requiredData;
    private ASN1Encodable optionalData;

    /**
     * Gets the single instance of SecurityInfo.
     *
     * @param obj
     * @return single instance of SecurityInfo
     */
    public static SecurityInfo getInstance(Object obj) {
        if (obj == null || obj instanceof SecurityInfo) {
            return (SecurityInfo) obj;
        } else if (obj instanceof ASN1Sequence) {
            return new SecurityInfo((ASN1Sequence) obj);
        }

        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Instantiates a new security info.
     *
     * @param seq
     */
    public SecurityInfo(ASN1Sequence seq) {
        if (seq.size() == 2) {
            identifier = (ASN1ObjectIdentifier) ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
            requiredData = (ASN1Encodable) seq.getObjectAt(1);

        } else if (seq.size() == 3) {
            identifier = (ASN1ObjectIdentifier) ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
            requiredData = (ASN1Encodable) seq.getObjectAt(1);
            optionalData = (ASN1Encodable) seq.getObjectAt(2);
        } else {
            throw new IllegalArgumentException("sequence wrong size for CertificateList");
        }
    }

    /**
     * Instantiates a new security info.
     *
     * @param contentType the content type
     * @param requiredData the required data
     */
    public SecurityInfo(ASN1ObjectIdentifier contentType, ASN1Encodable requiredData) {
        this.identifier = contentType;
        this.requiredData = requiredData;
    }

    /**
     * Instantiates a new security info.
     *
     * @param contentType the content type
     * @param requiredData the required data
     * @param optionalData the optional data
     */
    public SecurityInfo(ASN1ObjectIdentifier contentType, ASN1Encodable requiredData, ASN1Encodable optionalData) {
        this.identifier = contentType;
        this.requiredData = requiredData;
        this.optionalData = optionalData;
    }

    /**
     * Returns the object identifier..
     * 
     * @return Object identifier
     */
    public String getIdentifier() {
        return identifier.toString();
    }

    /**
     * Returns the required data.
     * 
     * @return Required data
     */
    public ASN1Encodable getRequiredData() {
        return requiredData;
    }

    /**
     * Returns the optional data.
     * 
     * @return Optional data
     */
    public ASN1Encodable getOptionalData() {
        return optionalData;
    }
}
