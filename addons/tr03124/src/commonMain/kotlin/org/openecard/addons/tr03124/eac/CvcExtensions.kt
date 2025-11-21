package org.openecard.addons.tr03124.eac

import org.openecard.sc.pace.crypto.Digest
import org.openecard.sc.pace.crypto.digest
import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CertificateDescription
import org.openecard.sc.pace.oid.TaObjectIdentifier
import org.openecard.utils.common.throwIf

@OptIn(ExperimentalUnsignedTypes::class)
fun CardVerifiableCertificate.checkDescriptionHash(certDesc: CertificateDescription) {
	val digest = getHashAlg()
	digest.update(certDesc.asBytes.toByteArray())
	val hash = digest.digest()
	val refHash =
		requireNotNull(extensions.certificateDescriptionReference?.certDescriptionHash) {
			"CertificateDescriptionReference is missing"
		}
	throwIf(!hash.contentEquals(refHash.v.toByteArray())) {
		IllegalArgumentException("CertificateDescription does not match terminal certificate")
	}
}

private fun CardVerifiableCertificate.getHashAlg(): Digest =
	when (publicKey.identifier.value) {
		TaObjectIdentifier.id_TA_ECDSA_SHA_1,
		TaObjectIdentifier.id_TA_RSA_PSS_SHA_1,
		TaObjectIdentifier.id_TA_RSA_v1_5_SHA_1,
		-> {
			digest(Digest.Algorithms.SHA1)
		}

		TaObjectIdentifier.id_TA_ECDSA_SHA_224 -> {
			digest(Digest.Algorithms.SHA224)
		}

		TaObjectIdentifier.id_TA_ECDSA_SHA_256,
		TaObjectIdentifier.id_TA_RSA_PSS_SHA_256,
		TaObjectIdentifier.id_TA_RSA_v1_5_SHA_256,
		-> {
			digest(Digest.Algorithms.SHA256)
		}

		TaObjectIdentifier.id_TA_ECDSA_SHA_384 -> {
			digest(Digest.Algorithms.SHA1)
		}

		TaObjectIdentifier.id_TA_ECDSA_SHA_512,
		TaObjectIdentifier.id_TA_RSA_PSS_SHA_512,
		TaObjectIdentifier.id_TA_RSA_v1_5_SHA_512,
		-> {
			digest(Digest.Algorithms.SHA512)
		}

		else -> {
			throw UnsupportedOperationException("Unsupported hash algorithm required")
		}
	}
