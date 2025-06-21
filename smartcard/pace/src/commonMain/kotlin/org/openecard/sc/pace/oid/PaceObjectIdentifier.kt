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
object PaceObjectIdentifier {
	/**
	 * id-PACE-DH-GM OBJECT IDENTIFIER ::= {id-PACE 1}
	 */
	val id_PACE_DH_GM: String = EacObjectIdentifier.ID_PACE + ".1"

	/**
	 * id-PACE-DH-GM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-DH-GM 1}
	 */
	val id_PACE_DH_GM_3DES_CBC_CBC: String = id_PACE_DH_GM + ".1"

	/**
	 * id-PACE-DH-GM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 2}
	 */
	val id_PACE_DH_GM_AES_CBC_CMAC_128: String = id_PACE_DH_GM + ".2"

	/**
	 * id-PACE-DH-GM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 3}
	 */
	val id_PACE_DH_GM_AES_CBC_CMAC_192: String = id_PACE_DH_GM + ".3"

	/**
	 * id-PACE-DH-GM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-DH-GM 4}
	 */
	val id_PACE_DH_GM_AES_CBC_CMAC_256: String = id_PACE_DH_GM + ".4"

	/**
	 * id-PACE-ECDH-GM OBJECT IDENTIFIER ::= {id-PACE 2}
	 */
	@JvmField
	val id_PACE_ECDH_GM: String = EacObjectIdentifier.ID_PACE + ".2"

	/**
	 * id-PACE-ECDH-GM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 1}
	 */
	val id_PACE_ECDH_GM_3DES_CBC_CBC: String = id_PACE_ECDH_GM + ".1"

	/**
	 * id-PACE-ECDH-GM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 2}
	 */
	@JvmField
	val id_PACE_ECDH_GM_AES_CBC_CMAC_128: String = id_PACE_ECDH_GM + ".2"

	/**
	 * id-PACE-ECDH-GM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 3}
	 */
	@JvmField
	val id_PACE_ECDH_GM_AES_CBC_CMAC_192: String = id_PACE_ECDH_GM + ".3"

	/**
	 * id-PACE-ECDH-GM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-ECDH-GM 4}
	 */
	@JvmField
	val id_PACE_ECDH_GM_AES_CBC_CMAC_256: String = id_PACE_ECDH_GM + ".4"

	/**
	 * id-PACE-DH-IM OBJECT IDENTIFIER ::= {id-PACE 3}
	 */
	val id_PACE_DH_IM: String = EacObjectIdentifier.ID_PACE + ".3"

	/**
	 * id-PACE-DH-IM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-DH-IM 1}
	 */
	val id_PACE_DH_IM_3DES_CBC_CBC: String = id_PACE_DH_IM + ".1"

	/**
	 * id-PACE-DH-IM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 2}
	 */
	val id_PACE_DH_IM_AES_CBC_CMAC_128: String = id_PACE_DH_IM + ".2"

	/**
	 * id-PACE-DH-IM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 3}
	 */
	val id_PACE_DH_IM_AES_CBC_CMAC_192: String = id_PACE_DH_IM + ".3"

	/**
	 * id-PACE-DH-IM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-DH-IM 4}
	 */
	val id_PACE_DH_IM_AES_CBC_CMAC_256: String = id_PACE_DH_IM + ".4"

	/**
	 * id-PACE-ECDH-IM OBJECT IDENTIFIER ::= {id-PACE 4}
	 */
	val id_PACE_ECDH_IM: String = EacObjectIdentifier.ID_PACE + ".4"

	/**
	 * id-PACE-ECDH-IM-3DES-CBC-CBC OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 1}
	 */
	val id_PACE_ECDH_IM_3DES_CBC_CBC: String = id_PACE_ECDH_IM + ".1"

	/**
	 * id-PACE-ECDH-IM-AES-CBC-CMAC-128 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 2}
	 */
	val id_PACE_ECDH_IM_AES_CBC_CMAC_128: String = id_PACE_ECDH_IM + ".2"

	/**
	 * id-PACE-ECDH-IM-AES-CBC-CMAC-192 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 3}
	 */
	val id_PACE_ECDH_IM_AES_CBC_CMAC_192: String = id_PACE_ECDH_IM + ".3"

	/**
	 * id-PACE-ECDH-IM-AES-CBC-CMAC-256 OBJECT IDENTIFIER ::= {id-PACE-ECDH-IM 4}
	 */
	val id_PACE_ECDH_IM_AES_CBC_CMAC_256: String = id_PACE_ECDH_IM + ".4"
}
