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
import org.openecard.bouncycastle.asn1.ASN1Set;

/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public final class PrivilegedTerminalInfo {

    private String protocol;
    private SecurityInfos privilegedTerminalInfo;

    /**
     * Instantiates a new privileged terminal info.
     *
     * @param seq ASN1 encoded sequence
     */
    public PrivilegedTerminalInfo(ASN1Sequence seq) {
        if (seq.size() == 2) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
            privilegedTerminalInfo = SecurityInfos.getInstance((ASN1Set) seq.getObjectAt(1));
        } else {
            throw new IllegalArgumentException("Sequence wrong size for PrivilegedTerminalInfo");
        }
    }

    /**
     * Gets the single instance of PrivilegedTerminalInfo.
     *
     * @param obj
     * @return single instance of PrivilegedTerminalInfo
     */
    public static PrivilegedTerminalInfo getInstance(Object obj) {
        if (obj == null || obj instanceof PrivilegedTerminalInfo) {
            return (PrivilegedTerminalInfo) obj;
        } else if (obj instanceof ASN1Sequence) {
            return new PrivilegedTerminalInfo((ASN1Sequence) obj);
        }

        throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
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
     * Gets the privileged terminal info.
     *
     * @return the privileged terminal info
     */
    public SecurityInfos getPrivilegedTerminalInfo() {
        return privilegedTerminalInfo;
    }
}
