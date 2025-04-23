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
import org.openecard.crypto.common.asn1.eac.oid.PACEObjectIdentifier

/**
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch
 */
class PACEDomainParameterInfo(
	seq: ASN1Sequence,
) {
	/**
	 * Returns the object identifier of the protocol.
	 *
	 * @return Protocol
	 */
	val protocol: String

	/**
	 * Returns the PACE domain parameter.
	 *
	 * @return domain parameter
	 */
	val domainParameter: AlgorithmIdentifier

	/**
	 * Returns the parameter identifier.
	 *
	 * @return parameter identifier
	 */
	val parameterID: Int

	/**
	 * Creates a new PACEDomainParameterInfo object.
	 *
	 * @param seq ANS1 encoded data
	 */
	init {
		if (seq.size() == 2) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			domainParameter = AlgorithmIdentifier.Companion.getInstance(seq.getObjectAt(1))
			parameterID = 0
		} else if (seq.size() == 3) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			domainParameter = AlgorithmIdentifier.Companion.getInstance(seq.getObjectAt(1))
			parameterID = ASN1Integer.getInstance(seq.getObjectAt(2)).value.toInt()
		} else {
			throw IllegalArgumentException("Sequence wrong size for PACEDomainParameterInfo")
		}
	}

	companion object {
		private val protocols: Array<String?> =
			arrayOf(
				PACEObjectIdentifier.id_PACE_DH_GM,
				PACEObjectIdentifier.id_PACE_DH_IM,
				PACEObjectIdentifier.id_PACE_ECDH_GM,
				PACEObjectIdentifier.id_PACE_ECDH_IM,
			)

		/**
		 * Compares the object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if oid is a PACE object identifier, otherwise false
		 */
		fun isPACEObjectIdentifer(oid: String): Boolean {
			for (p in protocols) {
				if (p == oid) {
					return true
				}
			}

			return false
		}
	}
}
