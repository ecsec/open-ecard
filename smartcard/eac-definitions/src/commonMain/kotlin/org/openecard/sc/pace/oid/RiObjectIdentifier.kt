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
package org.openecard.sc.pace.oid

/**
 * See BSI-TR-03110, version 2.10
 *
 * @author Moritz Horsch
 */
object RiObjectIdentifier {
	/**
	 * id-RI-DH OBJECT IDENTIFIER ::= {id-RI 1}
	 */
	val id_RI_DH: String = EacObjectIdentifier.ID_RI + ".1"

	/**
	 * id-RI-DH-SHA-1 OBJECT IDENTIFIER ::= {id-RI-DH 1}
	 */
	val id_RI_DH_SHA_1: String = id_RI_DH + ".1"

	/**
	 * id-RI-DH-SHA-224 OBJECT IDENTIFIER ::= {id-RI-DH 2}
	 */
	val id_RI_DH_SHA_224: String = id_RI_DH + ".2"

	/**
	 * id-RI-DH-SHA-256 OBJECT IDENTIFIER ::= {id-RI-DH 3}
	 */
	val id_RI_DH_SHA_256: String = id_RI_DH + ".3"

	/**
	 * id-RI-ECDH OBJECT IDENTIFIER ::= {id-RI 2}
	 */
	val id_RI_ECDH: String = EacObjectIdentifier.ID_RI + ".2"

	/**
	 * id-RI-ECDH-SHA-1 OBJECT IDENTIFIER ::= {id-RI-ECDH 1}
	 */
	val id_RI_ECDH_SHA_1: String = id_RI_ECDH + ".1"

	/**
	 * id-RI-ECDH-SHA-224 OBJECT IDENTIFIER ::= {id-RI-ECDH 2}
	 */
	val id_RI_ECDH_SHA_224: String = id_RI_ECDH + ".2"

	/**
	 * id-RI-ECDH-SHA-256 OBJECT IDENTIFIER ::= {id-RI-ECDH 3}
	 */
	val id_RI_ECDH_SHA_256: String = id_RI_ECDH + ".3"
}
