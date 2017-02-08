/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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

package org.openecard.sal.protocol.genericcryptography;


/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public final class GenericCryptoUris {

    /**
     * 1.2.840.113549.1.1.1.
     * RSA_ENCRYPTION OBJECT IDENTIFIER ::= { pkcs-1 1 }
     */
    public static final String RSA_ENCRYPTION = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
    /**
     * 1.2.840.113549.1.1.7.
     * id-RSAES-OAEP OBJECT IDENTIFIER ::= { pkcs-1 7 }
     */
    public static final String RSAES_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
    /**
     * 1.2.840.113549.1.1.10.
     * id-RSASSA-PSS OBJECT IDENTIFIER ::= { pkcs-1 10 }
     */
    public static final String RSASSA_PSS_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
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
