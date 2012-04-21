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
package org.openecard.client.crypto.common.asn1.eac.oid;

/
**
 * See BSI-TR-03110, version 2.10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract interface TAObjectIdentifier extends EACObjectIdentifier {

    /**
     * id-TA-RSA OBJECT IDENTIFIER ::= {id-TA 1}
     */
    public static final String id_TA_RSA = id_TA + ".1";
    /**
     * id-TA-RSA-v1-5-SHA-1 OBJECT IDENTIFIER ::= {id-TA-RSA 1}
     */
    public static final String id_TA_RSA_v1_5_SHA_1 = id_TA_RSA + ".1";
    /**
     * id-TA-RSA-v1-5-SHA-256 OBJECT IDENTIFIER ::= {id-TA-RSA 2}
     */
    public static final String id_TA_RSA_v1_5_SHA_256 = id_TA_RSA + ".2";
    /**
     * id-TA-RSA-PSS-SHA-1 OBJECT IDENTIFIER ::= {id-TA-RSA 3}
     */
    public static final String id_TA_RSA_PSS_SHA_1 = id_TA_RSA + ".3";
    /**
     * id-TA-RSA-PSS-SHA-256 OBJECT IDENTIFIER ::= {id-TA-RSA 4}
     */
    public static final String id_TA_RSA_PSS_SHA_256 = id_TA_RSA + ".4";
    /**
     * id-TA-RSA-v1-5-SHA-512 OBJECT IDENTIFIER ::= {id-TA-RSA 5}
     */
    public static final String id_TA_RSA_v1_5_SHA_512 = id_TA_RSA + ".5";
    /**
     * id-TA-RSA-PSS-SHA-512 OBJECT IDENTIFIER ::= {id-TA-RSA 6}
     */
    public static final String id_TA_RSA_PSS_SHA_512 = id_TA_RSA + ".6";
    /**
     * id-TA-ECDSA OBJECT IDENTIFIER ::= {id-TA 2}
     */
    public static final String id_TA_ECDSA = id_TA + ".2";
    /**
     * id-TA-ECDSA-SHA-1 OBJECT IDENTIFIER ::= {id-TA-ECDSA 1}
     */
    public static final String id_TA_ECDSA_SHA_1 = id_TA_ECDSA + ".1";
    /**
     * id-TA-ECDSA-SHA-224 OBJECT IDENTIFIER ::= {id-TA-ECDSA 2}
     */
    public static final String id_TA_ECDSA_SHA_224 = id_TA_ECDSA + ".2";
    /**
     * id-TA-ECDSA-SHA-256 OBJECT IDENTIFIER ::= {id-TA-ECDSA 3}
     */
    public static final String id_TA_ECDSA_SHA_256 = id_TA_ECDSA + ".3";
    /**
     * id-TA-ECDSA-SHA-384 OBJECT IDENTIFIER ::= {id-TA-ECDSA 4}
     */
    public static final String id_TA_ECDSA_SHA_384 = id_TA_ECDSA + ".4";
    /**
     * id-TA-ECDSA-SHA-512 OBJECT IDENTIFIER ::= {id-TA-ECDSA 5}
     */
    public static final String id_TA_ECDSA_SHA_512 = id_TA_ECDSA + ".5";

}
