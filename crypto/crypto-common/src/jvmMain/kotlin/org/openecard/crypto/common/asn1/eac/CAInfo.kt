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
import org.openecard.crypto.common.asn1.eac.oid.CAObjectIdentifier

/**
 * See BSI-TR-03110, version 2.05, section A.1.1.2.
 *
 * @author Moritz Horsch
 */
class CAInfo(
	seq: ASN1Sequence,
) : SecurityInfo(seq) {
	/**
	 * Returns the object identifier of the protocol.
	 *
	 * @return Protocol
	 */
	val protocol: String = getIdentifierString()

	/**
	 * Returns the version of the protocol.
	 *
	 * @return Version
	 */
	val version: Int = (requiredData as ASN1Integer).value.toInt()

	/**
	 * Returns the key identifier.
	 *
	 * @return KeyID
	 */
	val keyID: Int =
		if (seq.size() == 3) {
			(optionalData as ASN1Integer).value.toInt()
		} else {
			0
		}

	val isDH: Boolean
		/**
		 * Checks if the protocol identifier indicates Diffie-Hellman.
		 *
		 * @return True if Diffie-Hellman is used, otherwise false
		 */
		get() {
			return protocol.startsWith(CAObjectIdentifier.id_CA_DH)
		}

	val isECDH: Boolean
		/**
		 * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
		 *
		 * @return True if elliptic curve Diffie-Hellman is used, otherwise false
		 */
		get() {
			return protocol.startsWith(CAObjectIdentifier.id_CA_ECDH)
		}

	companion object {
		private val protocols: Array<String> =
			arrayOf(
				CAObjectIdentifier.id_CA_DH_3DES_CBC_CBC,
				CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_128,
				CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_192,
				CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_256,
				CAObjectIdentifier.id_CA_ECDH_3DES_CBC_CBC,
				CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128,
				CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_192,
				CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_256,
			)

		/**
		 * Compares the object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if o is a ChipAuthentication object identifier; false otherwise
		 */
		fun isObjectIdentifier(oid: String): Boolean {
			for (i in protocols.indices) {
				if (protocols[i] == oid) {
					return true
				}
			}
			return false
		}
	}
}
