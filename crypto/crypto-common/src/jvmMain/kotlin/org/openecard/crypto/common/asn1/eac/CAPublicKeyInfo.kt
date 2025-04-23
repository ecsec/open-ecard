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

/**
 * See BSI-TR-03110, version 2.05, section A.1.1.2.
 *
 * @author Moritz Horsch
 */
class CAPublicKeyInfo(
	seq: ASN1Sequence,
) {
	/**
	 * Gets the protocol.
	 *
	 * @return Protocol
	 */
	val protocol: String

	/**
	 * Gets the SubjectPublicKeyInfo.
	 *
	 * @return SubjectPublicKeyInfo
	 */
	val subjectPublicKeyInfo: SubjectPublicKeyInfo

	/**
	 * Gets the key id.
	 *
	 * @return Key identifier
	 */
	val keyID: Int

	/**
	 * Instantiates a new ChipAuthenticationPublicKeyInfo.
	 *
	 * @param seq the ASN1 encoded sequence
	 */
	init {
		if (seq.size() == 2) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			subjectPublicKeyInfo = SubjectPublicKeyInfo.Companion.getInstance(seq.getObjectAt(1))
			keyID = 0
		} else if (seq.size() == 3) {
			protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			subjectPublicKeyInfo = SubjectPublicKeyInfo.Companion.getInstance(seq.getObjectAt(1))
			keyID = ASN1Integer.getInstance(seq.getObjectAt(2)).value.toInt()
		} else {
			throw IllegalArgumentException("Sequence wrong size for CAPublicKeyInfo")
		}
	}
}
