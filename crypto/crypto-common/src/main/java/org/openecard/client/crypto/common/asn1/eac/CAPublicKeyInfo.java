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


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class CAPublicKeyInfo {

    private String protocol;
    private SubjectPublicKeyInfo subjectPublicKeyInfo;
    private int keyID;

    /**
     * Instantiates a new ChipAuthenticationPublicKeyInfo. See BSI-TR-03110 version 2.05 section A.1.1.2.
     *
     * @param seq the ASN1 encoded sequence
     */
    public CAPublicKeyInfo(ASN1Sequence seq) {
        if (seq.size() == 2) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
            subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(1));

        } else if (seq.size() == 3) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
            subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(1));
            keyID = ((ASN1Integer) ASN1Integer.getInstance(seq.getObjectAt(2))).getValue().intValue();
        } else {
            throw new IllegalArgumentException("Sequence wrong size for CAPublicKeyInfo");
        }
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Gets the SubjectPublicKeyInfo.
     *
     * @return the SubjectPublicKeyInfo
     */
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return subjectPublicKeyInfo;
    }

    /**
     * Gets the key id.
     *
     * @return the key id
     */
    public int getKeyID() {
        return keyID;
    }
}
