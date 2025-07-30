package org.openecard.addons.tr03124.eac

import org.openecard.sal.sc.SmartcardDataset
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sal.sc.SmartcardEf
import org.openecard.sal.sc.dids.SmartcardPaceDid
import org.openecard.sc.apdu.command.GeneralAuthenticate
import org.openecard.sc.apdu.command.Mse
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.pace.asn1.GeneralAuthenticateCommandTags
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponseTags
import org.openecard.sc.pace.asn1.MseTags
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.tlvCustom
import org.openecard.sc.tlv.toTlv
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.serialization.toPrintable

class ChipAuthenticationImpl(
	val card: SmartcardDeviceConnection,
	val paceDid: SmartcardPaceDid,
) : ChipAuthentication {
	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readEfCardSecurity(): UByteArray {
		val ds: SmartcardDataset = TODO()
		// don't use dataset directly to circumvent acl
		val file = SmartcardEf(card.channel, ds.ds.path.v, ds.ds.shortEf, ds.ds.type, ds)
		val efcsData = file.read()
		return efcsData
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun authenticate(): ChipAuthentication.ChipAuthenticationResult {
		val efCardSecurity = readEfCardSecurity()

		val oid: ObjectIdentifier = TODO()
		val keyId: Int = TODO()
		val mse =
			Mse.mseSet(
				Mse.p1FlagsAllUnset.setComputationDecipherIntAuthKeyAgree(true),
				Mse.Tag.AT,
				listOfNotNull(
					oid.tlvCustom(MseTags.cryptoMechanismReference),
					keyId.toUInt().toTlv(MseTags.sessionKeyComputationReference),
				),
			)
		mse.transmit(card.channel).success()

		val key: UByteArray = TODO()
		val ga =
			GeneralAuthenticate.withData(
				listOf(TlvPrimitive(GeneralAuthenticateCommandTags.caEphemeralPublicKey, key.toPrintable())),
			)
		val gResp = ga.transmit(card.channel).success()
		val gaTlv =
			gResp.data
				.toTlvBer()
				.tlv.asConstructedAsserted
		val nonce =
			gaTlv.childList().findTlv(GeneralAuthenticateResponseTags.caNonce)?.contentAsBytesBer
				?: throw IllegalStateException("Expected data is missing in card response")
		val token =
			gaTlv.childList().findTlv(GeneralAuthenticateResponseTags.caAuthenticationToken)?.contentAsBytesBer
				?: throw IllegalStateException("Expected data is missing in card response")

		// destroy pace channel as CA no has taken over
		paceDid.closeChannel()

		return ChipAuthentication.ChipAuthenticationResult(efCardSecurity, nonce, token)
	}
}
