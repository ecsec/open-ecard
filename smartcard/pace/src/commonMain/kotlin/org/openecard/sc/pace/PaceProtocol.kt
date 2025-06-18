package org.openecard.sc.pace

import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.SecureMessagingImpl
import org.openecard.sc.iface.CardChannel

class PaceProtocol {
	fun execute(channel: CardChannel) {
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
