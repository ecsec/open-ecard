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
package org.openecard.crypto.common.asn1.eac

import org.openecard.bouncycastle.asn1.ASN1Integer
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.crypto.common.asn1.eac.oid.PACEObjectIdentifier

/**
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch
 */
class PACEInfo(seq: ASN1Sequence) : SecurityInfo(seq) {
	/**
	 * Returns the object identifier of the protocol.
	 *
	 * @return Protocol object identifier
	 */
	val protocol: String = getIdentifierString()

	/**
	 * Returns the version of the protocol.
	 *
	 * @return version
	 */
	val version: Int = (requiredData as ASN1Integer).value.toInt()

	/**
	 * Returns the parameter identifier.
	 *
	 * @return parameter identifier
	 */
	val parameterID: Int = if (seq.size() == 3) {
		(optionalData as ASN1Integer).value.toInt()
	} else {
		-1
	}

	val isGM: Boolean
		/**
		 * Checks if the protocol identifier indicates generic mapping.
		 *
		 * @return True if generic mapping is used, otherwise false
		 */
		get() {
			return (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_GM)
				|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_GM))
		}

	val isIM: Boolean
		/**
		 * Checks if the protocol identifier indicates integrated mapping.
		 *
		 * @return True if integrated mapping is used, otherwise false
		 */
		get() {
			return (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_IM)
				|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_IM))
		}

	val isDH: Boolean
		/**
		 * Checks if the protocol identifier indicates Diffie-Hellman.
		 *
		 * @return True if Diffie-Hellman is used, otherwise false
		 */
		get() {
			return (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_GM)
				|| protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_IM))
		}

	val isECDH: Boolean
		/**
		 * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
		 *
		 * @return True if elliptic curve Diffie-Hellman is used, otherwise false
		 */
		get() {
			return (protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_GM)
				|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_IM))
		}

	val kdfLength: Int
		get() {
			return when (protocol) {
				PACEObjectIdentifier.id_PACE_DH_GM_3DES_CBC_CBC, PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_128, PACEObjectIdentifier.id_PACE_DH_IM_3DES_CBC_CBC, PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_128, PACEObjectIdentifier.id_PACE_ECDH_GM_3DES_CBC_CBC, PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128, PACEObjectIdentifier.id_PACE_ECDH_IM_3DES_CBC_CBC, PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_128 -> 16
				PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_192, PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_192, PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192, PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_192 -> 24
				PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_256, PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_256, PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256, PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_256 -> 32
				else -> throw IllegalArgumentException("Unknown PACE protocol: $protocol")
			}
		}

	companion object {
		private val protocols: Array<String?> = arrayOf(
			PACEObjectIdentifier.id_PACE_DH_GM_3DES_CBC_CBC,
			PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_128,
			PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_192,
			PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_256,
			PACEObjectIdentifier.id_PACE_DH_IM_3DES_CBC_CBC,
			PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_128,
			PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_192,
			PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_256,
			PACEObjectIdentifier.id_PACE_ECDH_GM_3DES_CBC_CBC,
			PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
			PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
			PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
			PACEObjectIdentifier.id_PACE_ECDH_IM_3DES_CBC_CBC,
			PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_128,
			PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_192,
			PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_256
		)

		/**
		 * Compares the object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if oid is a PACE object identifier; false otherwise
		 */
		fun isPACEObjectIdentifer(oid: String): Boolean {
			for (i in protocols.indices) {
				if (protocols[i] == oid) {
					return true
				}
			}

			return false
		}
	}
}
