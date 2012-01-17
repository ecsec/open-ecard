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
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public abstract interface PACEObjectIdentifier extends EACObjectIdentifier {

    /**
     * id-PACE-DH-GM OBJECT IDENTIFIER ::= {id-PACE 1}
     */
    public static final String id_PACE_DH_GM = id_PACE + ".1";
    /**
     * id-PACE-DH-GM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-DH-GM 1}
     */
    public static final String id_PACE_DH_GM_3DES_CBC_CBC = id_PACE_DH_GM + ".1";
    /**
     * id-PACE-DH-GM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 2}
     */
    public static final String id_PACE_DH_GM_AES_CBC_CMAC_128 = id_PACE_DH_GM + ".2";
    /**
     * id-PACE-DH-GM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 3}
     */
    public static final String id_PACE_DH_GM_AES_CBC_CMAC_192 = id_PACE_DH_GM + ".3";
    /**
     * id-PACE-DH-GM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 4}
     */
    public static final String id_PACE_DH_GM_AES_CBC_CMAC_256 = id_PACE_DH_GM + ".4";
    /**
     * id-PACE-ECDH-GM OBJECT IDENTIFIER ::= {id-PACE 2}
     */
    public static final String id_PACE_ECDH_GM = id_PACE + ".2";
    /**
     * id-PACE-ECDH-GM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 1}
     */
    public static final String id_PACE_ECDH_GM_3DES_CBC_CBC = id_PACE_ECDH_GM + ".1";
    /**
     * id-PACE-ECDH-GM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 2}
     */
    public static final String id_PACE_ECDH_GM_AES_CBC_CMAC_128 = id_PACE_ECDH_GM + ".2";
    /**
     * id-PACE-ECDH-GM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 3}
     */
    public static final String id_PACE_ECDH_GM_AES_CBC_CMAC_192 = id_PACE_ECDH_GM + ".3";
    /**
     * id-PACE-ECDH-GM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 4}
     */
    public static final String id_PACE_ECDH_GM_AES_CBC_CMAC_256 = id_PACE_ECDH_GM + ".4";
    /**
     * id-PACE-DH-IM OBJECT IDENTIFIER ::= {id-PACE 3}
     */
    public static final String id_PACE_DH_IM = id_PACE + ".3";
    /**
     * id-PACE-DH-IM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-DH-IM 1}
     */
    public static final String id_PACE_DH_IM_3DES_CBC_CBC = id_PACE_DH_IM + ".1";
    /**
     * id-PACE-DH-IM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 2}
     */
    public static final String id_PACE_DH_IM_AES_CBC_CMAC_128 = id_PACE_DH_IM + ".2";
    /**
     * id-PACE-DH-IM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 3}
     */
    public static final String id_PACE_DH_IM_AES_CBC_CMAC_192 = id_PACE_DH_IM + ".3";
    /**
     * id-PACE-DH-IM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 4}
     */
    public static final String id_PACE_DH_IM_AES_CBC_CMAC_256 = id_PACE_DH_IM + ".4";
    /**
     * id-PACE-ECDH-IM OBJECT IDENTIFIER ::= {id-PACE 4}
     */
    public static final String id_PACE_ECDH_IM = id_PACE + ".4";
    /**
     * id-PACE-ECDH-IM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 1}
     */
    public static final String id_PACE_ECDH_IM_3DES_CBC_CBC = id_PACE_ECDH_IM + ".1";
    /**
     * id-PACE-ECDH-IM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 2}
     */
    public static final String id_PACE_ECDH_IM_AES_CBC_CMAC_128 = id_PACE_ECDH_IM + ".2";
    /**
     * id-PACE-ECDH-IM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 3}
     */
    public static final String id_PACE_ECDH_IM_AES_CBC_CMAC_192 = id_PACE_ECDH_IM + ".3";
    /**
     * id-PACE-ECDH-IM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 4}
     */
    public static final String id_PACE_ECDH_IM_AES_CBC_CMAC_256 = id_PACE_ECDH_IM + ".4";
}
