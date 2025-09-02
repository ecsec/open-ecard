package org.openecard.addons.tr03124.eac

import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sc.apdu.command.ExternalAuthenticate
import org.openecard.sc.apdu.command.GetChallenge
import org.openecard.sc.apdu.command.Mse
import org.openecard.sc.apdu.command.PerformSecurityOperation
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.MseTags
import org.openecard.sc.pace.crypto.eacCryptoUtils
import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CvcChain
import org.openecard.sc.pace.cvc.PublicKeyReference
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.sc.tlv.tlvCustom
import org.openecard.utils.serialization.toPrintable

class TerminalAuthenticationImpl(
	val card: SmartcardDeviceConnection,
	val efCa: EfCardAccess,
) : TerminalAuthentication {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val challenge: UByteArray by lazy {
		GetChallenge
			.forAlgorithm(0u)
			.transmit(card.channel)
			.success()
			.data
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun verifyCertificates(chain: CvcChain) {
		chain.path.forEach { cvc ->
			val mse =
				Mse.mseSet(
					Mse.p1FlagsAllUnset.setVerifyEncipherExtAuthKeyAgree(true),
					Mse.Tag.DST,
					listOf(cvc.certificateAuthorityReference.toTlv(MseTags.passwordReference)),
				)
			mse.transmit(card.channel).success()
			val pso = PerformSecurityOperation.verifyCertificateBer(cvc.original)
			pso.transmit(card.channel).success()
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun verifyTerminalSignature(
		terminalCertificate: CardVerifiableCertificate,
		terminalSignature: UByteArray,
		pcdKey: UByteArray,
		aad: Tlv?,
	) {
		val oid: ObjectIdentifier = terminalCertificate.publicKey.identifier
		val chr: PublicKeyReference = terminalCertificate.certificateHolderReference

		// calculate key
		val caDef = efCa.chipAuthenticationV2.first()
		val caDomainParams = caDef.chipAuthenticationDomainParameterInfo.standardizedDomainParameters
		val eacCrypto = eacCryptoUtils()
		val compressedKey: UByteArray = eacCrypto.compressKey(pcdKey, caDomainParams)

		val mse =
			Mse.mseSet(
				Mse.p1FlagsAllUnset.setVerifyEncipherExtAuthKeyAgree(true),
				Mse.Tag.AT,
				listOfNotNull(
					oid.tlvCustom(MseTags.cryptoMechanismReference),
					chr.toTlv(MseTags.passwordReference),
					TlvPrimitive(
						Tag.forTagNumWithClass(0x91u),
						compressedKey.toPrintable(),
					), // TODO: see if this tag can be defined in MseTags
					aad,
				),
			)
		mse.transmit(card.channel).success()

		val extAuth = ExternalAuthenticate.withoutData(terminalSignature)
		extAuth.transmit(card.channel).success()
	}
}
