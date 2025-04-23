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

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.bouncycastle.asn1.DERBitString

/**
 *
 * @author Moritz Horsch
 */
class SubjectPublicKeyInfo(
	seq: ASN1Sequence,
) {
	/**
	 * Gets the algorithm.
	 *
	 * @return the algorithm
	 */
	val algorithm: String

	/**
	 * Gets the subject public key.
	 *
	 * @return the subject public key
	 */
	val subjectPublicKey: ByteArray

	/**
	 * Instantiates a new SubjectPublicKeyInfo.
	 *
	 * @param seq the ASN1 encoded sequence
	 */
	init {
		if (seq.size() == 2) {
			algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			subjectPublicKey = DERBitString.getInstance(seq.getObjectAt(1)).bytes
		} else {
			throw IllegalArgumentException("Sequence wrong size for SubjectPublicKeyInfo")
		}
	}

	companion object {
		/**
		 * Gets the single instance of SubjectPublicKeyInfo.
		 *
		 * @param obj
		 * @return single instance of SubjectPublicKeyInfo
		 */
		fun getInstance(obj: Any): SubjectPublicKeyInfo {
			if (obj is SubjectPublicKeyInfo) {
				return obj
			} else if (obj is ASN1Sequence) {
				return SubjectPublicKeyInfo(obj)
			}

			throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass.getName())
		}
	}
}
