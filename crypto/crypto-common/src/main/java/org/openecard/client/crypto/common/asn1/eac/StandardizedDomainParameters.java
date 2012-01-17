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

import java.util.HashMap;
import java.util.Map;
import org.openecard.bouncycastle.jce.ECNamedCurveTable;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public final class StandardizedDomainParameters extends DomainParameters {

    private final static Map<Integer, String> map = new HashMap<Integer, String>();

    // See BSI-TR-03110 version 2.05 section A.2.1.1.
    static {
        //TODO Add missing parameter
//        map.put(0, "");
//        map.put(1, "");
//        map.put(2, "");
        // 3 - 7 RFU
        map.put(8, "secp192r1");
        map.put(9, "BrainpoolP192r1");
        map.put(10, "secp224r1");
        map.put(11, "BrainpoolP256r1");
        map.put(12, "secp256r1");
        map.put(13, "BrainpoolP256r1");
        map.put(14, "BrainpoolP320r1");
        map.put(15, "secp384r1");
        map.put(16, "BrainpoolP384r1");
        map.put(17, "BrainpoolP512r1");
        map.put(18, "secp521r1");
        // 19 - 31 RFU
    }

    /**
     * Instantiates a new standardized domain parameters.
     *
     * @param index the index of the standardized domain parameters
     */
    public StandardizedDomainParameters(int index) {
        String value = map.get(index);

        if (value == null) {
            throw new IllegalArgumentException("Wrong index for standardized domain parameter");
        } else {
            domainParameter = ECNamedCurveTable.getParameterSpec(value);
        }
    }
}
