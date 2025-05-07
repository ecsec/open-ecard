/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
 */
package org.openecard.common.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom

private val logger = KotlinLogging.logger { }

/**
 * Factory class for SecureRandom instances.
 * This factory contains special handling for platforms where inadequate SecureRandom objects are created. For Linux
 * that means /dev/urandom is used instead of /dev/random, so that the input dows not block. For special purposes it is
 * still possible to use Java's default SecureRandom.
 *
 * @author Tobias Wich
 */
object SecureRandomFactory {
	/**
	 * Creates a new SecureRandom instance.
	 * The instance is seeded with its own seed mechanism.
	 *
	 * @param numSeedBytes
	 * @return Seeded instance of SecureRandom.
	 * @see SecureRandom.generateSeed
	 */
	fun create(numSeedBytes: Int): SecureRandom {
		var numSeedBytes = numSeedBytes
		numSeedBytes = if (numSeedBytes < 0) 0 else numSeedBytes
		val r =
			if ("Linux" == System.getProperty("os.name")) {
				LinuxSecureRandom()
			} else {
				// the default is fine
				SecureRandom()
			}

		if (numSeedBytes > 0) {
			r.setSeed(r.generateSeed(numSeedBytes))
		}

		return r
	}

	private class LinuxSecureRandom : SecureRandom() {
		private val seedSource: SeedSource

		init {
			var s: SeedSource
			try {
				s = UrandomSeedSource()
			} catch (ex: FileNotFoundException) {
				logger.warn { "Failed to open entropy source to /dev/urandom, Falling back to default implementation." }
				s = SecureRandomSeedSource()
			} catch (ex: SecurityException) {
				logger.warn { "Failed to open entropy source to /dev/urandom, Falling back to default implementation." }
				s = SecureRandomSeedSource()
			}
			seedSource = s
		}

		override fun generateSeed(numBytes: Int): ByteArray = seedSource.genSeed(numBytes)
	}

	/**
	 * Seed source interface to abstract the dirty details of reading file streams in the actual implementation.
	 */
	private interface SeedSource {
		fun genSeed(numBytes: Int): ByteArray
	}

	private class SecureRandomSeedSource : SeedSource {
		override fun genSeed(numBytes: Int): ByteArray = SecureRandom.getSeed(numBytes)
	}

	private class UrandomSeedSource : SeedSource {
		private val randomStream: InputStream = FileInputStream("/dev/urandom")

		override fun genSeed(numBytes: Int): ByteArray {
			try {
				val result = ByteArray(numBytes)
				var remaining = numBytes
				var start = 0
				while (remaining > 0) {
					val len = randomStream.read(result, start, remaining)
					if (len < 0) {
						throw InternalError("Entropy source /dev/urandom returned EOF.")
					}
					start += len
					remaining -= len
				}
				return result
			} catch (ex: IOException) {
				throw InternalError(
					"""Error while reading random numbers from /dev/urandom.
  ${ex.message}""",
				)
			}
		}
	}
}
