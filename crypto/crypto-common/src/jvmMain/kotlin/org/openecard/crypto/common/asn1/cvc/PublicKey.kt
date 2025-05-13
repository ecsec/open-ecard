/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.tlv.TLV
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils

/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.
 *
 * @author Moritz Horsch
 */
abstract class PublicKey {
	/**
	 * Compares the public key.
	 *
	 * @param pk PublicKey
	 * @return True if they are equal, otherwise false
	 */
	open fun compare(pk: PublicKey): Boolean = ByteUtils.compare(this.tLVEncoded.toBER(), pk.tLVEncoded.toBER())

	/**
	 * Returns the object identifier.
	 *
	 * @return Object identifier
	 */
	abstract val objectIdentifier: String

	/**
	 * Returns the TLV encoded key.
	 *
	 * @return TLV encoded key
	 */
	abstract val tLVEncoded: TLV

	companion object {
		/**
		 * Tag for object identifiers.
		 */
		const val OID_TAG: Int = 0x06

		/**
		 * Creates a new public key.
		 *
		 * @param key Key
		 * @return Public key
		 * @throws Exception
		 */
		@Throws(Exception::class)
		fun getInstance(key: ByteArray): PublicKey = getInstance(TLV.fromBER(key))

		/**
		 * Creates a new public key.
		 *
		 * @param key Key
		 * @return Public key
		 * @throws Exception
		 */
		@Throws(Exception::class)
		fun getInstance(key: TLV): PublicKey =
			try {
				val oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG.toLong())[0].value)

				if (oid.startsWith(TAObjectIdentifier.id_TA_ECDSA)) {
					ECPublicKey(key)
				} else if (oid.startsWith(TAObjectIdentifier.id_TA_RSA)) {
					RSAPublicKey(key)
				} else {
					throw IllegalArgumentException("Cannot handle object identifier")
				}
			} catch (e: Exception) {
				throw IllegalArgumentException("Malformed public key: " + e.message)
			}
	}
}
