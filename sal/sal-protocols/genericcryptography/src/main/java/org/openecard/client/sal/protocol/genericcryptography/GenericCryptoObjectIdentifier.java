/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.sal.protocol.genericcryptography;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class GenericCryptoObjectIdentifier {

    /**
     * 1.2.840.113549.1.1.
     * pkcs-1 OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 1 }
     * See RFC 3447, section Appendix C.
     */
    public static final String pkcs_1 = "urn:oid:1.2.840.113549.1.1";
    /**
     * 1.2.840.113549.1.1.1.
     * rsaEncryption OBJECT IDENTIFIER ::= { pkcs-1 1 }
     */
    public static final String rsaEncryption = pkcs_1 + ".1";
    /**
     * 1.2.840.113549.1.1.7.
     * id-RSAES-OAEP OBJECT IDENTIFIER ::= { pkcs-1 7 }
     */
    public static final String id_RSAES_OAEP = pkcs_1 + ".7";
    /**
     * 1.2.840.113549.1.1.10.
     * id-RSASSA-PSS OBJECT IDENTIFIER ::= { pkcs-1 10 }
     */
    public static final String id_RSASSA_PSS = pkcs_1 + ".10";
    /**
     * 1.3.36.3.4.2.
     * iso(1) identified-organization(3) teletrust(36) algorithm(3) signatureScheme(4) sigS-ISO9796-2(2)
     */
    public static final String sigS_ISO9796_2 = "urn:oid:1.3.36.3.4.2";
    /**
     * 1.3.36.3.4.2.3.
     */
    public static final String sigS_ISO9796_2rnd = sigS_ISO9796_2 + ".3";

}
