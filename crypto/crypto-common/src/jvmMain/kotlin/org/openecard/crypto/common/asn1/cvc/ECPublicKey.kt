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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.tlv.TLV
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils
import java.math.BigInteger

/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.3.
 *
 * @author Moritz Horsch
 */
class ECPublicKey(
	private val key: TLV,
) : PublicKey() {
	override val tLVEncoded: TLV = key

	private var oid: String? = null
	override val objectIdentifier: String
		get() = oid!!

	/**
	 * Returns the prime number.
	 *
	 * @return Prime number.
	 */
	var prime: BigInteger? = null
		private set

	/**
	 * Returns the coefficient A.
	 *
	 * @return Coefficient A
	 */
	var a: BigInteger? = null
		private set

	/**
	 * Returns the coefficient B.
	 *
	 * @return Coefficient B
	 */
	var b: BigInteger? = null
		private set

	/**
	 * Returns the base point.
	 *
	 * @return Base point
	 */
	var basePoint: ByteArray? = null
		private set

	/**
	 * Returns the order.
	 *
	 * @return Order
	 */
	var order: BigInteger? = null
		private set

	/**
	 * Returns the public point.
	 *
	 * @return Public point
	 */
	var y: ByteArray? = null
		private set

	/**
	 * Returns the cofactor.
	 *
	 * @return Cofactor
	 */
	var cofactor: BigInteger? = null
		private set

	/**
	 * Creates a new ECPublicKey.
	 *
	 * @param key Key
	 * @throws Exception
	 */
	init {
		val bodyElements = key.child!!.asList()

		val it = bodyElements.iterator()
		while (it.hasNext()) {
			val item = it.next()
			val itemTag = item.tagNumWithClass.toInt()

			when (itemTag) {
				OID_TAG -> // MANDATORY
					oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG.toLong())[0].value)

				PRIME_TAG -> // CONDITIONAL
					prime = BigInteger(key.findChildTags(PRIME_TAG.toLong())[0].value)

				COEFFICIENT_A_TAG -> // CONDITIONAL
					a = BigInteger(key.findChildTags(COEFFICIENT_A_TAG.toLong())[0].value)

				COEFFICIENT_B_TAG -> // CONDITIONAL
					b = BigInteger(key.findChildTags(COEFFICIENT_B_TAG.toLong())[0].value)

				BASE_POINT_TAG -> // CONDITIONAL
					this.basePoint = key.findChildTags(BASE_POINT_TAG.toLong())[0].value

				ORDER_TAG -> // CONDITIONAL
					order = BigInteger(key.findChildTags(ORDER_TAG.toLong())[0].value)

				PUBLIC_POINT_TAG -> // MANDATORY
					y = key.findChildTags(PUBLIC_POINT_TAG.toLong())[0].value

				COFACTOR_TAG -> // CONDITIONAL
					this.cofactor = BigInteger(key.findChildTags(COFACTOR_TAG.toLong())[0].value)

				else -> {}
			}
		}
		verify()
	}

	private fun verify() {
		// Object identifier and public point are MANDATORY
		require(!(oid == null || y == null)) { "Malformed ECPublicKey" }
		// CONDITIONAL domain parameters MUST be either all present, except the cofactor, or all absent
		if (prime == null || a == null || b == null || this.basePoint == null || order == null) {
			require(!(prime != null || a != null || b != null || this.basePoint != null || order != null)) {
				"Malformed ECPublicKey"
			}
		}
	}

	companion object {
		private const val PRIME_TAG = 0x81
		private const val COEFFICIENT_A_TAG = 0x82
		private const val COEFFICIENT_B_TAG = 0x83
		private const val BASE_POINT_TAG = 0x84
		private const val ORDER_TAG = 0x85
		private const val PUBLIC_POINT_TAG = 0x86
		private const val COFACTOR_TAG = 0x87
	}
}
