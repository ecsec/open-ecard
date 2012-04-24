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


/**
 * See BSI-TR-03110, version 2.10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract interface RIObjectIdentifier extends EACObjectIdentifier {

    /**
     * id-RI-DH OBJECT IDENTIFIER ::= {id-RI 1}
     */
    public static final String id_RI_DH = id_RI + ".1";
    /**
     * id-RI-DH-SHA-1 OBJECT IDENTIFIER ::= {id-RI-DH 1}
     */
    public static final String id_RI_DH_SHA_1 = id_RI_DH + ".1";
    /**
     * id-RI-DH-SHA-224 OBJECT IDENTIFIER ::= {id-RI-DH 2}
     */
    public static final String id_RI_DH_SHA_224 = id_RI_DH + ".2";
    /**
     * id-RI-DH-SHA-256 OBJECT IDENTIFIER ::= {id-RI-DH 3}
     */
    public static final String id_RI_DH_SHA_256 = id_RI_DH + ".3";
    /**
     * id-RI-ECDH OBJECT IDENTIFIER ::= {id-RI 2}
     */
    public static final String id_RI_ECDH = id_RI + ".2";
    /**
     * id-RI-ECDH-SHA-1 OBJECT IDENTIFIER ::= {id-RI-ECDH 1}
     */
    public static final String id_RI_ECDH_SHA_1 = id_RI_ECDH + ".1";
    /**
     * id-RI-ECDH-SHA-224 OBJECT IDENTIFIER ::= {id-RI-ECDH 2}
     */
    public static final String id_RI_ECDH_SHA_224 = id_RI_ECDH + ".2";
    /**
     * id-RI-ECDH-SHA-256 OBJECT IDENTIFIER ::= {id-RI-ECDH 3}
     */
    public static final String id_RI_ECDH_SHA_256 = id_RI_ECDH + ".3";

}
