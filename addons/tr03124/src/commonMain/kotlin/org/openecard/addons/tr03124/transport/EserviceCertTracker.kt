package org.openecard.addons.tr03124.transport

import io.ktor.http.Url
import io.ktor.http.hostWithPort
import org.openecard.sc.pace.cvc.CertificateDescription
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

class EserviceCertTracker {
	@OptIn(ExperimentalUnsignedTypes::class)
	private var certsSeen = setOf<PrintableUByteArray>()
	private var certDesc: CertificateDescription? = null
	private var allowedCommCerts: Set<PrintableUByteArray>? = null

	@Throws(UntrustedCertificateError::class, IllegalArgumentException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun setCertDesc(certDesc: CertificateDescription) {
		this.certDesc = certDesc
		val allowedCommCerts =
			certDesc.commCertificates?.map { it.toPrintable() }?.toSet()
				?: throw IllegalArgumentException("Certificate description is missing communication certificates")
		this.allowedCommCerts = allowedCommCerts

		// now that we have the reference, it's time to check it
		allowedCommCerts.checkCertHashes(certsSeen)
	}

	fun matchesSop(
		tokenUrl: String,
		urlToCheck: String,
	): Boolean {
		// fallback to TCToken URL
		val referenceStr = certDesc?.subjectUrl ?: tokenUrl
		val reference = Url(referenceStr)
		val url = Url(urlToCheck)

		if (reference.protocol != url.protocol) {
			return false
		}

		if (reference.hostWithPort != url.hostWithPort) {
			return false
		}

		return true
	}

	@Throws(UntrustedCertificateError::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun addCertHash(certHash: UByteArray) {
		val newCerts = certsSeen + certHash.toPrintable()
		// validate if we know our hashes
		allowedCommCerts?.checkCertHashes(newCerts)
		certsSeen = newCerts
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(UntrustedCertificateError::class, IllegalArgumentException::class)
		fun Set<PrintableUByteArray>.checkCertHashes(certsSeen: Set<PrintableUByteArray>) {
			val allowedCommCerts = this

			val allAllowed = certsSeen.all { toTest -> allowedCommCerts.any { it == toTest } }
			if (!allAllowed) {
				throw UntrustedCertificateError("")
			}
		}
	}
}
