package org.openecard.sc.pace

import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.SecureMessagingImpl
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.pace.asn1.EfCardAccess

class PaceProtocol {
	fun readEfCardAccess(): EfCardAccess {
		TODO("Implement")
	}

	fun execute(
		channel: CardChannel,
		efca: EfCardAccess,
	) {
		TODO("Add PACE magic")
		val encKey = byteArrayOf()
		val macKey = byteArrayOf()

		// prepare secure messaging object
		val encStage = EncryptionStage(encKey)
		val macStage = CmacStage(macKey)
		val sm =
			SecureMessagingImpl(
				commandStages = listOf(encStage, macStage),
				responseStages = listOf(macStage, encStage),
				smType = SecureMessagingIndication.SM_W_HEADER,
				protectedData = true,
				protectedLe = true,
				protectedHeader = false,
			)
		// set secure messaging in channel
		channel.setSecureMessaging(sm)
	}
}
