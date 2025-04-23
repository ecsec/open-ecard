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

import org.openecard.bouncycastle.asn1.ASN1Encodable
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1Sequence

/**
 *
 * @author Moritz Horsch
 */
open class SecurityInfo {
	val identifier: ASN1ObjectIdentifier

	/**
	 * Returns the required data.
	 *
	 * @return Required data
	 */
	val requiredData: ASN1Encodable

	/**
	 * Returns the optional data.
	 *
	 * @return Optional data
	 */
	val optionalData: ASN1Encodable?

	/**
	 * Instantiates a new security info.
	 *
	 * @param seq
	 */
	constructor(seq: ASN1Sequence) {
		if (seq.size() == 2) {
			identifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
			requiredData = seq.getObjectAt(1)
			optionalData = null
		} else if (seq.size() == 3) {
			identifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
			requiredData = seq.getObjectAt(1)
			optionalData = seq.getObjectAt(2)
		} else {
			throw IllegalArgumentException("sequence wrong size for CertificateList")
		}
	}

	/**
	 * Instantiates a new security info.
	 *
	 * @param contentType the content type
	 * @param requiredData the required data
	 */
	constructor(contentType: ASN1ObjectIdentifier, requiredData: ASN1Encodable) {
		this.identifier = contentType
		this.requiredData = requiredData
		this.optionalData = null
	}

	/**
	 * Instantiates a new security info.
	 *
	 * @param contentType the content type
	 * @param requiredData the required data
	 * @param optionalData the optional data
	 */
	constructor(contentType: ASN1ObjectIdentifier, requiredData: ASN1Encodable, optionalData: ASN1Encodable?) {
		this.identifier = contentType
		this.requiredData = requiredData
		this.optionalData = optionalData
	}

	/**
	 * Returns the object identifier.
	 *
	 * @return Object identifier
	 */
	fun getIdentifierString(): String = identifier.toString()

	companion object {
		/**
		 * Gets the single instance of SecurityInfo.
		 *
		 * @param obj
		 * @return single instance of SecurityInfo
		 */
		fun getInstance(obj: Any?): SecurityInfo? {
			if (obj == null || obj is SecurityInfo) {
				return obj
			} else if (obj is ASN1Sequence) {
				return SecurityInfo(obj)
			}

			throw IllegalArgumentException("unknown object in factory: " + obj.javaClass.getName())
		}
	}
}
