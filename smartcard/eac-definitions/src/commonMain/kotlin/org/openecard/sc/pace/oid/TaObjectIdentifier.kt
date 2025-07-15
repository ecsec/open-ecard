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
object TaObjectIdentifier {
	/**
	 * id-TA-RSA OBJECT IDENTIFIER ::= {id-TA 1}
	 */
	val id_TA_RSA: String = EacObjectIdentifier.ID_TA + ".1"

	/**
	 * id-TA-RSA-v1-5-SHA-1 OBJECT IDENTIFIER ::= {id-TA-RSA 1}
	 */
	val id_TA_RSA_v1_5_SHA_1: String = id_TA_RSA + ".1"

	/**
	 * id-TA-RSA-v1-5-SHA-256 OBJECT IDENTIFIER ::= {id-TA-RSA 2}
	 */
	val id_TA_RSA_v1_5_SHA_256: String = id_TA_RSA + ".2"

	/**
	 * id-TA-RSA-PSS-SHA-1 OBJECT IDENTIFIER ::= {id-TA-RSA 3}
	 */
	val id_TA_RSA_PSS_SHA_1: String = id_TA_RSA + ".3"

	/**
	 * id-TA-RSA-PSS-SHA-256 OBJECT IDENTIFIER ::= {id-TA-RSA 4}
	 */
	val id_TA_RSA_PSS_SHA_256: String = id_TA_RSA + ".4"

	/**
	 * id-TA-RSA-v1-5-SHA-512 OBJECT IDENTIFIER ::= {id-TA-RSA 5}
	 */
	val id_TA_RSA_v1_5_SHA_512: String = id_TA_RSA + ".5"

	/**
	 * id-TA-RSA-PSS-SHA-512 OBJECT IDENTIFIER ::= {id-TA-RSA 6}
	 */
	val id_TA_RSA_PSS_SHA_512: String = id_TA_RSA + ".6"

	/**
	 * id-TA-ECDSA OBJECT IDENTIFIER ::= {id-TA 2}
	 */
	val id_TA_ECDSA: String = EacObjectIdentifier.ID_TA + ".2"

	/**
	 * id-TA-ECDSA-SHA-1 OBJECT IDENTIFIER ::= {id-TA-ECDSA 1}
	 */
	val id_TA_ECDSA_SHA_1: String = id_TA_ECDSA + ".1"

	/**
	 * id-TA-ECDSA-SHA-224 OBJECT IDENTIFIER ::= {id-TA-ECDSA 2}
	 */
	val id_TA_ECDSA_SHA_224: String = id_TA_ECDSA + ".2"

	/**
	 * id-TA-ECDSA-SHA-256 OBJECT IDENTIFIER ::= {id-TA-ECDSA 3}
	 */
	val id_TA_ECDSA_SHA_256: String = id_TA_ECDSA + ".3"

	/**
	 * id-TA-ECDSA-SHA-384 OBJECT IDENTIFIER ::= {id-TA-ECDSA 4}
	 */
	val id_TA_ECDSA_SHA_384: String = id_TA_ECDSA + ".4"

	/**
	 * id-TA-ECDSA-SHA-512 OBJECT IDENTIFIER ::= {id-TA-ECDSA 5}
	 */
	val id_TA_ECDSA_SHA_512: String = id_TA_ECDSA + ".5"
}
