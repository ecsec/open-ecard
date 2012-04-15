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
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public abstract interface CVCertificatesObjectIdentifier extends EACObjectIdentifier {

    /**
     * Certificate Extensions.
     * id-extensions OBJECT IDENTIFIER ::= {bsi-de applictions(3) mrtd(1) 3}
     */
    public static String id_extensions = bsi_de + ".3.1.3";
    /**
     * Certificate Description.
     * id-description OBJECT IDENTIFIER ::= {id-extensions 1}
     */
    public static String id_description = id_extensions + ".1";
    /**
     * Certificate Description plain format.
     * id-plainFormat OBJECT IDENTIFIER ::= {id-description 1}
     */
    public static String id_plainFormat = id_description + ".1";
    /**
     * Certificate Description html format.
     * id-htmlFormat OBJECT IDENTIFIER ::= {id-description 2}
     */
    public static String id_htmlFormat = id_description + ".2";
    /**
     * Certificate Description pdf format.
     * id-pdfFormat OBJECT IDENTIFIER ::= {id-description 3}
     */
    public static String id_pdfFormat = id_description + ".3";
    /**
     * Terminal Sector.
     * id-sector OBJECT IDENTIFIER ::= {id-extensions 2}
     */
    public static final String id_sector = id_extensions + ".2";
    /**
     * Roles and Authorization Levels.
     * id-roles OBJECT IDENTIFIER ::= {bsi-de applications(3) mrtd(1) 2}
     */
    public static final String id_roles = bsi_de + ".3.1.2";
    /**
     * Inspection Systems.
     * id-IS OBJECT IDENTIFIER ::= {id-roles 1}
     */
    public static final String id_IS = id_roles + ".1";
    /**
     * Authentication Terminals.
     * id-AT OBJECT IDENTIFIER ::= {id-roles 2}
     */
    public static final String id_AT = id_roles + ".2";
    /**
     * Signature Terminals.
     * id-ST OBJECT IDENTIFIER ::= {id-roles 3}
     */
    public static final String id_ST = id_roles + ".3";

}
