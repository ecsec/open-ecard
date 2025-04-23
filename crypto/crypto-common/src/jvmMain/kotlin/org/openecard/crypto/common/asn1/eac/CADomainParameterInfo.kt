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
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.crypto.common.asn1.eac.oid.CAObjectIdentifier

/**
 *
 * @author Moritz Horsch
 */
class CADomainParameterInfo(
	seq: ASN1Sequence,
) {
	/**
	 * Returns the object identifier of the protocol.
	 *
	 * @return Protocol
	 */
	val protocol: String

	/**
	 * Returns the ChipAuthentication domain parameter.
	 *
	 * @return domain parameter
	 */
	val domainParameter: AlgorithmIdentifier

	/**
	 * Returns the key identifier.
	 *
	 * @return KeyID
	 */
	val keyID: Int

	/**
	 * Creates a new ChipAuthenticationDomainParameterInfo object. See TR-03110
	 * Section A.1.1.2.
	 *
	 * @param seq ANS1 encoded data
	 */
	init {
		if (seq.size() == 2) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			domainParameter = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
			keyID = 0
		} else if (seq.size() == 3) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			domainParameter = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
			keyID = (ASN1Integer.getInstance(seq.getObjectAt(2)) as ASN1Integer).value.toInt()
		} else {
			throw IllegalArgumentException("Sequence wrong size for CADomainParameterInfo")
		}
	}

	companion object {
		private val protocols: Array<String> =
			arrayOf(
				CAObjectIdentifier.id_CA_DH,
				CAObjectIdentifier.id_CA_ECDH,
			)

		/**
		 * Checks if the id is a CA object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if o is a CA object identifier, otherwise false
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
