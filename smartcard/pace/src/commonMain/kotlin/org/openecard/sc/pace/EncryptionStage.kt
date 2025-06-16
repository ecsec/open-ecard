package org.openecard.sc.pace

import org.openecard.sc.apdu.sm.CommandStage
import org.openecard.sc.apdu.sm.ResponseStage
import org.openecard.sc.tlv.Tlv

class EncryptionStage :
	CommandStage,
	ResponseStage {
	override val isOptional = false

	private var ssc: Long = 0

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processCommand(
		smHeader: UByteArray,
		dos: List<Tlv>,
	): List<Tlv> {
		ssc++

		TODO("Not yet implemented")
	}

	override fun processResponse(dos: List<Tlv>): List<Tlv> {
		ssc++

		TODO("Not yet implemented")
	}
}
