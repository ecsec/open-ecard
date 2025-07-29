package org.openecard.sc.pace.crypto

import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import java.math.BigInteger
import java.security.SecureRandom

class BcEcKeyPair(
	val privateKey: BcEcKeyPrivate,
	val publicKey: BcEcKeyPublic,
) {
	val domainParameters: ECDomainParameters = publicKey.key.parameters

	companion object {
		fun generateKeyPair(
			domainParameters: ECDomainParameters,
			rand: SecureRandom = SecureRandom.getInstanceStrong(),
		): BcEcKeyPair {
			val gen = ECKeyPairGenerator()
			val params = ECKeyGenerationParameters(domainParameters, rand)
			gen.init(params)
			val key = gen.generateKeyPair()
			return BcEcKeyPair(
				BcEcKeyPrivate(key.private as ECPrivateKeyParameters),
				BcEcKeyPublic(key.public as ECPublicKeyParameters),
			)
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		fun decodePublicKey(
			domainParameters: ECDomainParameters,
			keyData: UByteArray,
		): BcEcKeyPublic {
			val q = domainParameters.curve.decodePoint(keyData.toByteArray())
			val pk = ECPublicKeyParameters(q, domainParameters)
			return BcEcKeyPublic(pk)
		}
	}

	class BcEcKeyPublic(
		val key: ECPublicKeyParameters,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val encoded: UByteArray by lazy { key.q.getEncoded(false).toUByteArray() }

		@OptIn(ExperimentalUnsignedTypes::class)
		val encodedCompressed: UByteArray by lazy { key.q.getEncoded(true).toUByteArray() }

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun equals(other: Any?): Boolean =
			if (other is BcEcKeyPublic) {
				encoded.contentEquals(other.encoded)
			} else {
				false
			}

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun hashCode(): Int = encoded.contentHashCode()
	}

	class BcEcKeyPrivate(
		val key: ECPrivateKeyParameters,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun mapGeneral(
			iccKey: BcEcKeyPair.BcEcKeyPublic,
			nonce: ByteArray,
		): ECDomainParameters {
			val curParams = key.parameters
			val curG = curParams.g
			val q = iccKey.key.q
			val d = key.d
			val s: BigInteger = BigInteger(1, nonce)

			val h = q.multiply(curParams.h.multiply(d))

			// When calculating ecdh with ECDHBasicAgreement, we also get h, however only the x coordinate of it.
			// For the calculation of the new G, we need an ECPoint and I don't know how to get it.
			//
			// val ecdh = ECDHBasicAgreement()
			// ecdh.init(key)
			// val hX = ecdh.calculateAgreement(iccKey.key)

			val newG = curG.multiply(s).add(h)

			// the seed is null for brainpool, but not secp
			return ECDomainParameters(curParams.curve, newG, curParams.n, curParams.h, curParams.seed)
		}
	}
}
