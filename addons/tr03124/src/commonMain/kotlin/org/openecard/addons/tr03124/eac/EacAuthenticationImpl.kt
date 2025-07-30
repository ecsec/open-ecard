package org.openecard.addons.tr03124.eac

import org.openecard.addons.tr03124.xml.Eac2Input
import org.openecard.addons.tr03124.xml.Eac2Output
import org.openecard.addons.tr03124.xml.EacAdditionalInput
import org.openecard.sc.pace.cvc.CvcChain

class EacAuthenticationImpl(
	val ta: TerminalAuthentication,
	val ca: ChipAuthentication,
	val eacInput: Eac2Input,
) : EacAuthentication {
	override fun process(): Eac2Output {
		val keyData: UByteArray = TODO("get from eacInput")
		val chain: CvcChain = TODO()

		ta.verifyCertificates(chain)

		val sig: UByteArray? = TODO("eac2In.signature")
		when (sig) {
			is UByteArray -> {
				return processAdditional(TODO("EacAdditionalInput(eacInput.signature)"))
			}
			else -> {
				// return challenge again
				TODO("Eac2Output(ta.challenge)")
			}
		}
	}

	override fun processAdditional(additionalInput: EacAdditionalInput): Eac2Output {
		val signature: UByteArray = TODO("Get from additionalData")

		ta.verifyTerminalSignature()
		val caResult = ca.authenticate()

		TODO("return Eac2Output(caResult.efCardSecurity, caResult.nonce, caResult.token)")
	}
}
