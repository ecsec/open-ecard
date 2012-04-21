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
public abstract interface CAObjectIdentifier extends EACObjectIdentifier {

    /**
     * id-CA-DH OBJECT IDENTIFIER ::= {id-CA 1}
     */
    public static final String id_CA_DH = id_CA + ".1";
    /**
     * id-CA-DH-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-CA-DH 1}
     */
    public static final String id_CA_DH_3DES_CBC_CBC = id_CA_DH + ".1";
    /**
     * id-CA-DH-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-CA-DH 2}
     */
    public static final String id_CA_DH_AES_CBC_CMAC_128 = id_CA_DH + ".2";
    /**
     * id-CA-DH-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-CA-DH 3}
     */
    public static final String id_CA_DH_AES_CBC_CMAC_192 = id_CA_DH + ".3";
    /**
     * id-CA-DH-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-CA-DH 4}
     */
    public static final String id_CA_DH_AES_CBC_CMAC_256 = id_CA_DH + ".4";
    /**
     * id-CA-ECDH OBJECT IDENTIFIER ::= {id-CA 2}
     */
    public static final String id_CA_ECDH = id_CA + ".2";
    /**
     * id-CA-ECDH-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-CA-ECDH 1}
     */
    public static final String id_CA_ECDH_3DES_CBC_CBC = id_CA_ECDH + ".1";
    /**
     * id-CA-ECDH-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-CA-ECDH 2}
     */
    public static final String id_CA_ECDH_AES_CBC_CMAC_128 = id_CA_ECDH + ".2";
    /**
     * id-CA-ECDH-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-CA-ECDH 3}
     */
    public static final String id_CA_ECDH_AES_CBC_CMAC_192 = id_CA_ECDH + ".3";
    /**
     * id-CA-ECDH-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-CA-ECDH 4}
     */
    public static final String id_CA_ECDH_AES_CBC_CMAC_256 = id_CA_ECDH + ".4";

}
