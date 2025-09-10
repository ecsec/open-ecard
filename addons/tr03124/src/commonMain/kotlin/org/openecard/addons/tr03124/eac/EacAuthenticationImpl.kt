package org.openecard.addons.tr03124.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.tr03124.xml.Eac2Input
import org.openecard.addons.tr03124.xml.Eac2Output
import org.openecard.addons.tr03124.xml.EacAdditionalInput
import org.openecard.sc.pace.cvc.CvcChain
import org.openecard.sc.tlv.Tlv
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

class EacAuthenticationImpl(
	val ta: TerminalAuthentication,
	val ca: ChipAuthentication,
	val eacInput: Eac2Input,
	val chain: CvcChain,
	val aad: Tlv?,
) : EacAuthentication {
	private val terminalCert by lazy {
		chain.terminalCertificate ?: throw IllegalArgumentException("No terminal certificate found in chain")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun process(): Eac2Output {
		log.info { "Validating certificates on card" }
		ta.verifyCertificates(chain)

		return when (val sig: UByteArray? = eacInput.signature?.v) {
			is UByteArray -> {
				processSignature(sig)
			}
			else -> {
				// return challenge again
				Eac2Output(
					protocol = eacInput.protocol,
					challenge = ta.challenge.toPrintable(),
					efCardSecurity = null,
					nonce = null,
					authenticationToken = null,
				)
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processAdditional(additionalInput: EacAdditionalInput): Eac2Output {
		val signature: UByteArray = additionalInput.signature.v
		return processSignature(signature)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun processSignature(signature: UByteArray): Eac2Output {
		log.info { "Validating terminal signature" }
		val pcdKey = eacInput.ephemeralPublicKey.v
		ta.verifyTerminalSignature(terminalCert, signature, pcdKey, aad)
		log.info { "Performing chip authentication" }
		val caResult = ca.authenticate(eacInput.ephemeralPublicKey.v)

		return Eac2Output(
			protocol = eacInput.protocol,
			challenge = null,
			efCardSecurity = caResult.efCardSecurity.toPrintable(),
			nonce = caResult.nonce.toPrintable(),
			authenticationToken = caResult.token.toPrintable(),
		)
	}
}
