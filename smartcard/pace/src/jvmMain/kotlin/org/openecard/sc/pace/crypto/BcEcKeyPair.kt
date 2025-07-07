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
			// TODO: I think the first part is ECDH, so we should use an actual ECDH implementation
			val curParams = key.parameters
			val pkMapPICC = curParams.curve.decodePoint(iccKey.encoded.toByteArray())
			val d = key.d
			val s: BigInteger = BigInteger(1, nonce)

			val h = pkMapPICC.multiply(curParams.h.multiply(d))
			val newG = curParams.g.multiply(s).add(h)

			// TODO: check if it is safe to set seed. the old code didn't set it
			// the seed is null for brainpool, but not secp
			return ECDomainParameters(curParams.curve, newG, curParams.n, curParams.h, curParams.seed)
		}
	}
}
