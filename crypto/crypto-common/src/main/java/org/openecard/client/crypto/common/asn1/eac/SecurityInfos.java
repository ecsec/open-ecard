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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1Set;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class SecurityInfos {

    private ASN1Set securityInfos;

    /**
     * Gets the single instance of SecurityInfos.
     *
     * @param obj
     * @return single instance of SecurityInfos
     */
    public static SecurityInfos getInstance(Object obj) {
        if (obj instanceof SecurityInfo) {
            return (SecurityInfos) obj;
        } else if (obj instanceof ASN1Set) {
            return new SecurityInfos((ASN1Set) obj);
        } else if (obj instanceof byte[]) {
            try {
                return new SecurityInfos((ASN1Set) ASN1Set.fromByteArray((byte[]) obj));
            } catch (IOException e) {
                Logger.getLogger("ASN1").log(Level.SEVERE, "Cannot parse SecurityInfos", e);
            }
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass());
    }

    /**
     * Instantiates a new set of SecurityInfos.
     *
     * @param seq the ASN1 encoded SecurityInfos set
     */
    private SecurityInfos(ASN1Set seq) {
        securityInfos = seq;
    }

    /**
     * Gets the SecurityInfos.
     *
     * @return the SecurityInfos
     */
    public ASN1Set getSecurityInfos() {
        return securityInfos;
    }
}
