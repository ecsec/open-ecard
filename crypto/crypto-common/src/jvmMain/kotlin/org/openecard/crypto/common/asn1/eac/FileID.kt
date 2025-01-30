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

import org.openecard.bouncycastle.asn1.ASN1OctetString
import org.openecard.bouncycastle.asn1.ASN1Sequence

/**
 *
 * @author Moritz Horsch
 */
class FileID(seq: ASN1Sequence) {
	/**
	 * Gets the file identifier (FID).
	 *
	 * @return the FID
	 */
	val fID: ByteArray

	/**
	 * Gets the short file identifier (SFID).
	 *
	 * @return the SFID
	 */
	val sFID: ByteArray?


	/**
	 * Instantiates a new file id.
	 *
	 * @param seq the ASN1 encoded sequence
	 */
	init {
		if (seq.size() == 1) {
			this.fID = ASN1OctetString.getInstance(seq.getObjectAt(0)).octets
			this.sFID = null
		} else if (seq.size() == 2) {
			this.fID = ASN1OctetString.getInstance(seq.getObjectAt(0)).octets
			this.sFID = ASN1OctetString.getInstance(seq.getObjectAt(1)).octets
		} else {
			throw IllegalArgumentException("Sequence wrong size for FileID")
		}
	}

	companion object {
		/**
		 * Gets the single instance of FileID.
		 *
		 * @param obj
		 * @return single instance of FileID
		 */
		fun getInstance(obj: Any): FileID {
			if (obj is FileID) {
				return obj
			} else if (obj is ASN1Sequence) {
				return FileID(obj)
			}

			throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass.getName())
		}
	}
}
