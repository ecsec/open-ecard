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
public class RIObjectIdentifier extends EACObjectIdentifier {

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
