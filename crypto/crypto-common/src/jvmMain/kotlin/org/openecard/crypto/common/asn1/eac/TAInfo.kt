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
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier

/**
 *
 * @author Moritz Horsch
 */
class TAInfo(
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
	 * @return version
	 */
	val version: Int = (requiredData as ASN1Integer).value.toInt()

	/**
	 * Returns the EF.CVCA.
	 *
	 * @return EF.CVCA
	 */
	val eFCVCA: FileID? =
		optionalData?.let {
			FileID.Companion.getInstance(optionalData)
		}

	val isECDSA: Boolean
		/**
		 * Checks if the protocol identifier indicates ECDSA.
		 *
		 * @return True if ECDSA is used, otherwise false
		 */
		get() {
			return protocol.startsWith(TAObjectIdentifier.id_TA_ECDSA)
		}

	val isRSA: Boolean
		/**
		 * Checks if the protocol identifier indicates elliptic curve RSA.
		 *
		 * @return True if elliptic curve RSA is used, otherwise false
		 */
		get() {
			return protocol.startsWith(TAObjectIdentifier.id_TA_RSA)
		}

	companion object {
		private val protocols: Array<String?> =
			arrayOf(
				TAObjectIdentifier.id_TA_ECDSA_SHA_1,
				TAObjectIdentifier.id_TA_ECDSA_SHA_224,
				TAObjectIdentifier.id_TA_ECDSA_SHA_256,
				TAObjectIdentifier.id_TA_ECDSA_SHA_384,
				TAObjectIdentifier.id_TA_ECDSA_SHA_512,
				TAObjectIdentifier.id_TA_RSA_PSS_SHA_1,
				TAObjectIdentifier.id_TA_RSA_PSS_SHA_256,
				TAObjectIdentifier.id_TA_RSA_PSS_SHA_512,
				TAObjectIdentifier.id_TA_RSA_v1_5_SHA_1,
				TAObjectIdentifier.id_TA_RSA_v1_5_SHA_256,
				TAObjectIdentifier.id_TA_RSA_v1_5_SHA_512,
			)

		/**
		 * Compares the object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if o is a TA object identifier; false otherwise
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
