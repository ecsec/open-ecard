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
public abstract interface EACObjectIdentifier {

    /**
     * BSI-TR-03110 - EAC specification version 2.05
     */
    /**
     * bsi-de OBJECT IDENTIFIER ::= {itu-t(0) identified-organization(4)
     * etsi(0) reserved(127) etsi-identified-organization(0) 7}
     */
    public static final String bsi_de = "0.4.0.127.0.7";
    /**
     * id-PK OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 1}
     */
    public static final String id_PK = bsi_de + ".2.2.1";
    /**
     * id-TA OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 2}
     */
    public static final String id_TA = bsi_de + ".2.2.2";
    /**
     * id-CA OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 3}
     */
    public static final String id_CA = bsi_de + ".2.2.3";
    /**
     * id-PACE OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 4}
     */
    public static final String id_PACE = bsi_de + ".2.2.4";
    /**
     * Restricted Identification.
     * id-RI OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 5}
     */
    public static final String id_RI = bsi_de + ".2.2.5";
    /**
     * CardInfoLocator.
     * id-CI OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 6}
     */
    public static final String id_CI = bsi_de + ".2.2.6";
    /**
     * eIDSecurityInfo.
     * id-eIDSecurity OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 7}
     */
    public static final String id_eIDSecurity = bsi_de + ".2.2.7";
    /**
     * PrivilegedTerminalInfo.
     * id-CI OBJECT IDENTIFIER ::= {bsi-de protocols(2) smartcard(2) 8}
     */
    public static final String id_PT = bsi_de + ".2.2.8";
    /**
     * Roles and Authorization Levels. BSI-TR-03110 section C.4.
     * id-roles OBJECT IDENTIFIER ::= {bsi-de applications(3) mrtd(1) 2}
     */
    public static final String id_roles = bsi_de + ".3.1.2";
    /**
     * Inspection Systems. BSI-TR-03110 section C.4.1.
     * id-IS OBJECT IDENTIFIER ::= {id-roles 1}
     */
    public static final String id_IS = id_roles + ".1";
    /**
     * Authentication Terminals. BSI-TR-03110 section C.4.2.
     * id-AT OBJECT IDENTIFIER ::= {id-roles 2}
     */
    public static final String id_AT = id_roles + ".2";
    /**
     * Signature Terminals. BSI-TR-03110 section C.4.3.
     * id-ST OBJECT IDENTIFIER ::= {id-roles 3}
     */
    public static final String id_ST = id_roles + ".3";
    /**
     * Authenticated Auxiliary Data. BSI-TR-03110 section A.6.5.1.
     * id-AuxiliaryData OBJECT IDENTIFIER ::= {bsi-de applications(3) mrtd(1) 4}
     */
    public static final String id_AuxiliaryData = bsi_de + ".3.1.4";
    /**
     * Age Verification. BSI-TR-03110 section A.6.5.2.
     * id-DateOfBirth OBJECT IDENTIFIER ::= {id-AuxiliaryData 1}
     */
    public static final String id_DateOfBirth = id_AuxiliaryData + ".1";
    /**
     * Document Validity Verification. BSI-TR-03110 section A.6.5.3.
     * id-DateOfExpiry OBJECT IDENTIFIER ::= {id-AuxiliaryData 2}
     */
    public static final String id_DateOfExpiry = id_AuxiliaryData + ".2";
    /**
     * Community ID Verification. BSI-TR-03110 section A.6.5.4.
     * id-CommunityID OBJECT IDENTIFIER ::= {id-AuxiliaryData 3}
     */
    public static final String id_CommunityID = id_AuxiliaryData + ".3";
    /**
     * Signature Format. BSI-TR-03110 section A.1.2.5.
     * id-SecurityObject OBJECT IDENTIFIER ::= {bsi-de applications(3) eID(2) 1}
     */
    public static final String id_SecurityObject = bsi_de + ".3.2.1";
    /**
     * Standardized Domain Parameters. BSI-TR-03110 section A.2.1.1.
     * standardizedDomainParameters OBJECT IDENTIFIER ::= {bsi-de algorithms(1) 2}
     */
    public static final String standardizedDomainParameters = bsi_de + ".1.2";
}
