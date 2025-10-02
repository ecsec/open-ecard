package org.openecard.sc.pace.crypto

import MemoryManager.Companion.memoryManaged
import OpenSSLError
import assertNotNull
import assertSuccess
import cnames.structs.ec_point_st
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BIGNUM
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_CTX_new
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_mul
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_new
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP_free
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP_get0_cofactor
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP_get0_generator
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP_get_degree
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_GROUP_new_by_curve_name
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_add
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_get_affine_coordinates
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_mul
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_new
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_set_affine_coordinates
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_X9_62_prime192v1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_X9_62_prime256v1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP192r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP224r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP256r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP320r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP384r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_brainpoolP512r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_secp224r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_secp384r1
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.NID_secp521r1
import dev.whyoleg.cryptography.random.CryptographyRandom
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.sc.pace.asn1.StandardizedDomainParameters
import org.openecard.utils.common.nextBitField
import toBigNum
import toUByteArray
import kotlin.UByteArray
import kotlin.random.Random

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalForeignApi::class)
internal class OSslEcCurve(
	val parameters: StandardizedDomainParameters,
	setG: EcPoint? = null,
) : AutoCloseable {
	fun StandardizedDomainParameters.openSslCurveName() =
		when (this) {
			StandardizedDomainParameters.Curve.Secp192r1 -> NID_X9_62_prime192v1
			StandardizedDomainParameters.Curve.BrainpoolP192r1 -> NID_brainpoolP192r1
			StandardizedDomainParameters.Curve.Secp224r1 -> NID_secp224r1
			StandardizedDomainParameters.Curve.BrainpoolP224r1 -> NID_brainpoolP224r1
			StandardizedDomainParameters.Curve.Secp256r1 -> NID_X9_62_prime256v1
			StandardizedDomainParameters.Curve.BrainpoolP256r1 -> NID_brainpoolP256r1
			StandardizedDomainParameters.Curve.BrainpoolP320r1 -> NID_brainpoolP320r1
			StandardizedDomainParameters.Curve.Secp384r1 -> NID_secp384r1
			StandardizedDomainParameters.Curve.BrainpoolP384r1 -> NID_brainpoolP384r1
			StandardizedDomainParameters.Curve.BrainpoolP512r1 -> NID_brainpoolP512r1
			StandardizedDomainParameters.Curve.Secp521r1 -> NID_secp521r1
		}

	override fun close() {
		logger.debug { "curve implementation closing" }
		EC_GROUP_free(group)
	}

	val group: CPointer<EC_GROUP> =
		EC_GROUP_new_by_curve_name(
			parameters.openSslCurveName(),
		).assertNotNull()

	val g: EcPoint =
		when (setG) {
			null -> EC_GROUP_get0_generator(group).assertNotNull().toECPoint(this)
			else -> setG
		}

	// EC_GROUP_get0_cofactor returns an internal pointer - don't try to free it
	val h = EC_GROUP_get0_cofactor(group).assertNotNull()

	fun generateKeyPair(rand: Random) = OSslEcKeyPair(this, rand)

	fun withGenerator(g: EcPoint): OSslEcCurve = OSslEcCurve(parameters, g)
}

@OptIn(ExperimentalForeignApi::class)
internal class EcPoint(
	val x: UByteArray,
	val y: UByteArray,
	val curve: OSslEcCurve,
) {
	fun toOpensslECPoint(): CValuesRef<EC_POINT> {
		val opensslEcPoint = EC_POINT_new(curve.group).assertNotNull()
		EC_POINT_set_affine_coordinates(
			group = curve.group,
			p = opensslEcPoint,
			x = x.toBigNum(),
			y = y.toBigNum(),
			ctx = null,
		).assertSuccess()
		return opensslEcPoint
	}

	fun assertOnCurve() = memoryManaged { toOpensslECPoint().autoFree() }

	operator fun times(by: UByteArray) = multiply(by)

	fun multiply(by: UByteArray): EcPoint =
		memoryManaged {
			logger.debug { "Point multiplying" }
			val opensslEcPoint = EC_POINT_new(curve.group).assertNotNull().autoFree()
			val bn = by.toBigNum().autoFree()
			val q = this@EcPoint.toOpensslECPoint().autoFree()

			EC_POINT_mul(
				group = curve.group,
				r = opensslEcPoint,
				n = null,
				q = q,
				m = bn,
				ctx = null,
			).assertSuccess()

			opensslEcPoint.toECPoint(curve)
		}

	operator fun plus(other: EcPoint) = add(other)

	fun add(other: EcPoint): EcPoint =
		memoryManaged {
			logger.debug { "Point adding" }
			val opensslEcPoint = EC_POINT_new(curve.group).assertNotNull().autoFree()
			val a = this@EcPoint.toOpensslECPoint().autoFree()
			val b = other.toOpensslECPoint().autoFree()

			EC_POINT_add(
				group = curve.group,
				r = opensslEcPoint,
				a = a,
				b = b,
				ctx = null,
			).assertSuccess()

			opensslEcPoint.toECPoint(curve)
		}

	override fun toString() = "PubKey:\nX: ${x.toHexString()}\nY: ${y.toHexString()}"
}

internal class OSslEcPublicKey(
	val point: EcPoint,
) {
	val encoded: UByteArray
		get() =
			ubyteArrayOf(0x04.toUByte()) + point.x + point.y

	val encodedCompressed: UByteArray
		get() =
			ubyteArrayOf(
				if (point.y.last().and(0x01.toUByte()) == 0x01.toUByte()) {
					0x03.toUByte()
				} else {
					0x02.toUByte()
				},
			) + point.x

	override fun equals(other: Any?): Boolean =
		other is OSslEcPublicKey &&
			point.x.contentEquals(other.point.x) &&
			point.y.contentEquals(other.point.y)

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun hashCode() = encoded.contentHashCode()

	companion object {
		@OptIn(ExperimentalForeignApi::class)
		fun decodePublicKey(
			keyData: UByteArray,
			curve: OSslEcCurve,
		) = OSslEcPublicKey(
			if (keyData[0] != 0x04.toUByte()) {
				throw OpenSSLError("Found no uncompressed point!")
			} else {
				val pointLength = (keyData.size - 1) / 2
				EcPoint(
					x = keyData.copyOfRange(1, pointLength + 1),
					y = keyData.copyOfRange(pointLength + 1, keyData.size),
					curve = curve,
				).also { it.assertOnCurve() }
			},
		)
	}
}

@OptIn(ExperimentalForeignApi::class)
operator fun CPointer<BIGNUM>.times(by: CPointer<BIGNUM>): CPointer<BIGNUM> = multiply(by)

@OptIn(ExperimentalForeignApi::class)
fun CPointer<BIGNUM>.multiply(by: CPointer<BIGNUM>): CPointer<BIGNUM> =
	memoryManaged {
		val bnCtx = BN_CTX_new().assertNotNull().autoFree()
		val res = BN_new().assertNotNull()

		BN_mul(
			res,
			by,
			this@multiply,
			bnCtx,
		).assertSuccess()

		res
	}

@OptIn(ExperimentalForeignApi::class)
internal class OSslEcPrivateKey(
	val d: UByteArray,
	private val curve: OSslEcCurve,
) {
	fun mapGeneral(
		iccKey: OSslEcPublicKey,
		nonce: ByteArray,
	): OSslEcCurve {
		logger.debug { "mapGeneral moving g" }
		val newG = curve.g * nonce.toUByteArray() + sharedSecret(iccKey)
		return curve.withGenerator(newG)
	}

	fun sharedSecret(iccKey: OSslEcPublicKey): EcPoint =
		memoryManaged {
			logger.debug { "calculating shared secret" }
			val q = iccKey.point
			val dBN = d.toBigNum().autoFree()
			val hXd = (curve.h * dBN).autoFree()

			q.multiply(hXd.toUByteArray())
		}

	companion object {
		internal fun generate(
			curve: OSslEcCurve,
			rand: Random,
		) = OSslEcPrivateKey(
			rand.nextBitField(EC_GROUP_get_degree(curve.group)),
			curve,
		).also {
			logger.debug { "Private key was generated" }
		}
	}
}

internal class OSslEcKeyPair(
	private val curve: OSslEcCurve,
	rand: Random,
) {
	val privateKey = OSslEcPrivateKey.generate(curve, rand)
	val publicKey: OSslEcPublicKey by lazy { publicKeyFromPrivateKey(privateKey.d) }

	fun publicKeyFromPrivateKey(privateKey: UByteArray): OSslEcPublicKey = OSslEcPublicKey(curve.g.multiply(privateKey))
}

@OptIn(ExperimentalForeignApi::class)
internal fun CPointer<ec_point_st>.toECPoint(curve: OSslEcCurve): EcPoint =
	memoryManaged {
		val x = BN_new().assertNotNull().autoFree()
		val y = BN_new().assertNotNull().autoFree()

		EC_POINT_get_affine_coordinates(
			group = curve.group,
			this@toECPoint,
			x = x,
			y = y,
			ctx = null,
		).assertSuccess()

		EcPoint(
			x.toUByteArray(),
			y.toUByteArray(),
			curve,
		)
	}
