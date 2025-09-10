package org.openecard.sc.pace.cvc

import org.openecard.utils.common.returnIf

typealias CardVerifiableCertificates = Collection<CardVerifiableCertificate>

class CvcChain(
	val path: CardVerifiableCertificates,
) {
	val terminalCertificate: CardVerifiableCertificate? by lazy { path.find { it.isTerminalCertificate } }

	companion object {
		fun CardVerifiableCertificates.toChain(car: PublicKeyReference): CvcChain? {
			val chain = buildChain(car)
			return chain.returnIf { it.isNotEmpty() }?.let { CvcChain(it) }
		}

		private fun CardVerifiableCertificates.buildChain(car: PublicKeyReference): List<CardVerifiableCertificate> {
			val certChain = mutableListOf<CardVerifiableCertificate>()

			for (c in this) {
				if (c.certificateAuthorityReference == car) {
					certChain.add(c)
					certChain.addAll(this.buildChain(c.certificateHolderReference))
				}
			}

			return certChain
		}
	}
}
