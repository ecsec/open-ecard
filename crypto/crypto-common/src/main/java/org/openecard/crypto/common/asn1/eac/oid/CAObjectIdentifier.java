/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.crypto.common.asn1.eac.oid;


/**
 * See BSI-TR-03110, version 2.10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CAObjectIdentifier extends EACObjectIdentifier {

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
