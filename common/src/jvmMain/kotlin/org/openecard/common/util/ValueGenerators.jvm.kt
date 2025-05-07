/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.common.util

import java.security.SecureRandom
import java.util.UUID

/**
 * Implements convenience methods to generates random values.
 *
 * @author Tobias Wich
 */
object ValueGenerators {
	private val rand: SecureRandom = SecureRandomFactory.create(32)
	private var counter: Long = 0

	private fun reseed() {
		counter++
		rand.setSeed(counter)
		rand.setSeed(System.nanoTime())
	}

	/**
	 * Generates a new pre-shared key (PSK).
	 *
	 * @param nibbleLength Length of the PSK in nibbles.
	 * @return PSK in hex notation.
	 */
	@JvmStatic
	@JvmOverloads
	fun generatePSK(nibbleLength: Int = 64): String = generateRandomHex(nibbleLength)

	/**
	 * Generates a secure session identifier encoded as web safe base 64
	 *
	 * @param nibbleLength Length of the session identifier in nibbles.
	 * @return Session identifier.
	 */
	@JvmStatic
	@JvmOverloads
	fun genBase64Session(nibbleLength: Int = 32): String {
		val random = generateRandom(nibbleLength)
		return ByteUtils.toWebSafeBase64String(random)!!
	}

	/**
	 * Generates a secure session identifier in hex format.
	 *
	 * @param nibbleLength Length of the session identifier in nibbles.
	 * @return Session identifier.
	 */
	@JvmStatic
	@JvmOverloads
	fun genHexSession(nibbleLength: Int = 32): String = generateRandomHex(nibbleLength)

	/**
	 * Generates a UUID.
	 * Using Java UUID and adds the prefix 'urn:uuid:'.
	 *
	 * @return UUID urn.
	 */
	@JvmStatic
	fun generateUUID(): String {
		val uuid = UUID.randomUUID().toString()
		return "urn:uuid:$uuid"
	}

	/**
	 * Generates a secure random hex string.
	 *
	 * @param nibbleLength Length of the random in nibbles.
	 * @return Secure random hex string.
	 */
	@JvmStatic
	fun generateRandomHex(nibbleLength: Int): String = ByteUtils.toHexString(generateRandom(nibbleLength))!!

	/**
	 * Generates a secure random value.
	 * Using 'java.security.SecureRandom'. The random instance is reseeded with a counter and the current system time in
	 * order to provide better random numbers.
	 *
	 * @param nibbleLength Length of the random in nibbles
	 * @return Secure random value
	 */
	@Throws(IllegalArgumentException::class)
	@JvmStatic
	fun generateRandom(nibbleLength: Int): ByteArray {
		var nibbleLength = nibbleLength
		if (nibbleLength < 1) {
			throw IllegalArgumentException("nibbleLength must be greater than 0")
		}

		nibbleLength = (nibbleLength / 2 + nibbleLength % 2)

		val randomBytes = ByteArray(nibbleLength)
		reseed()
		rand.nextBytes(randomBytes)

		return randomBytes
	}
}
