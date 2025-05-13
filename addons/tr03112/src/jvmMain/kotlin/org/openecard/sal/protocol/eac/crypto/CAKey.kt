/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.crypto

import org.openecard.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.openecard.bouncycastle.crypto.params.ECDomainParameters
import org.openecard.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.openecard.bouncycastle.crypto.params.ECPublicKeyParameters
import org.openecard.bouncycastle.crypto.params.ElGamalParameters
import org.openecard.bouncycastle.crypto.params.ElGamalPrivateKeyParameters
import org.openecard.bouncycastle.crypto.params.ElGamalPublicKeyParameters
import org.openecard.bouncycastle.jce.spec.ECParameterSpec
import org.openecard.bouncycastle.jce.spec.ElGamalParameterSpec
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.SecureRandomFactory
import org.openecard.crypto.common.asn1.eac.CADomainParameter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

private val LOG: Logger = LoggerFactory.getLogger(CAKey::class.java)
private val RAND: SecureRandom = SecureRandomFactory.create(32)
private var counter: Long = 0

private fun reseed() {
	counter++
	RAND.setSeed(counter)
	RAND.setSeed(System.nanoTime())
}

/**
 * Implements an abstract key for chip authentication.
 *
 * @author Moritz Horsch
 */
class CAKey(
	private val cdp: CADomainParameter,
) {
	private var sk: AsymmetricKeyParameter? = null
	private var pk: AsymmetricKeyParameter? = null

	/**
	 * Decodes a public key from a byte array.
	 *
	 * @param data Encoded key
	 * @return Decoded key
	 * @throws TLVException
	 * @throws IllegalArgumentException
	 */
	fun decodePublicKey(data: ByteArray): ByteArray {
		val keyBytes =
			if (data[0] == 0x7C.toByte()) {
				TLV.fromBER(data).getChild().value!!
			} else if (data[0].toInt() != 4) {
				ByteUtils.concatenate(0x04.toByte(), data)!!
			} else {
				data
			}

		if (cdp.isECDH) {
			val p = cdp.parameter as ECParameterSpec
			val ecp = ECDomainParameters(p.curve, p.g, p.n, p.h)

			val q = p.curve.decodePoint(keyBytes)
			pk = ECPublicKeyParameters(q, ecp)

			return this.encodedPublicKey
		} else if (cdp.isDH) {
			// TODO
			LOG.error("Not implemented yet.")
			throw UnsupportedOperationException("Not implemented yet.")
		} else {
			throw IllegalArgumentException()
		}
	}

	/**
	 * Generate a key pair.
	 */
	fun generateKeyPair() {
		reseed()
		if (cdp.isDH) {
			val p = cdp.parameter as ElGamalParameterSpec
			val numBits = p.g.bitLength()
			val d = BigInteger(numBits, RAND)
			val egp = ElGamalParameters(p.p, p.g)

			sk = ElGamalPrivateKeyParameters(d, egp)
			pk = ElGamalPublicKeyParameters(egp.g.multiply(d), egp)
		} else if (cdp.isECDH) {
			val p = cdp.parameter as ECParameterSpec
			val numBits = p.n.bitLength()
			val d = BigInteger(numBits, RAND)
			val ecp = ECDomainParameters(p.curve, p.g, p.n, p.h)

			sk = ECPrivateKeyParameters(d, ecp)
			pk = ECPublicKeyParameters(ecp.g.multiply(d), ecp)
		} else {
			throw IllegalArgumentException()
		}
	}

	val publicKey: AsymmetricKeyParameter
		/**
		 * Returns the public key.
		 *
		 * @return Public key
		 */
		get() = pk!!

	val encodedPublicKey: ByteArray
		/**
		 * Returns the byte encoded public key.
		 *
		 * @return Public key
		 */
		get() {
			return if (cdp.isDH) {
				(pk as ElGamalPublicKeyParameters).y.toByteArray()
			} else if (cdp.isECDH) {
				(pk as ECPublicKeyParameters).q.getEncoded(false)
			} else {
				throw IllegalArgumentException()
			}
		}

	val encodedCompressedPublicKey: ByteArray?
		/**
		 * Returns the byte encoded compressed public key.
		 *
		 * @return Public key
		 */
		get() {
			if (cdp.isDH) {
				try {
					val md = MessageDigest.getInstance("SHA-1")
					val input =
						(pk as ElGamalPublicKeyParameters)
							.y
							.toByteArray()
					val compKey = md.digest(input)

					return compKey
				} catch (e: NoSuchAlgorithmException) {
					LOG.error(e.message, e)
					throw RuntimeException(e)
				}
			} else if (cdp.isECDH) {
				val compKey =
					(pk as ECPublicKeyParameters)
						.q
						.affineXCoord
						.toBigInteger()
						.toByteArray()
				return ByteUtils.cutLeadingNullByte(compKey)
			} else {
				throw IllegalArgumentException()
			}
		}

	val privateKey: AsymmetricKeyParameter
		/**
		 * Returns the private key.
		 *
		 * @return Private key
		 */
		get() = sk!!

	val encodedPrivateKey: ByteArray
		/**
		 * Returns the byte encoded private key.
		 *
		 * @return Private key
		 */
		get() {
			return if (cdp.isDH) {
				(sk as ElGamalPrivateKeyParameters)
					.x
					.toByteArray()
			} else if (cdp.isECDH) {
				(sk as ECPrivateKeyParameters).getD().toByteArray()
			} else {
				throw IllegalArgumentException()
			}
		}
}
