package org.openecard.sc.apdu.sm

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.tlv.Tlv

interface CommandStage {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processCommand(
		smApdu: CommandApdu,
		dos: List<Tlv>,
	): List<Tlv>
}

interface ResponseStage {
	/**
	 * Indicate that the stage must not fail the process, if it is not applicable.
	 * This can for example happen if the card can optionally send a mac.
	 * Setting this flag to `true` continues without error, when the mac is missing.
	 */
	val isOptional: Boolean

	fun processResponse(dos: List<Tlv>): List<Tlv>
}
