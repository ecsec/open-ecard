package org.openecard.sc.apdu.sm

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.iface.SecureMessagingException
import org.openecard.sc.tlv.Tlv

/**
 * Interface for a sub-computation of a Command APDU in secure messaging.
 */
interface CommandStage {
	/**
	 * Process a Command APDU as part of the secure messaging computation.
	 *
	 * @return The processed SM DOs for processing in the next stage, or for building the secured APDU when this is the
	 *   last stage.
	 * @param dos Initial (plain) SM DOs based on the Command APDU when this is the first stage, or the DOs of the
	 *   previous stage for all remaining stages.
	 * @param smApdu Original Command APDU for reference, when the processed DOs are not sufficient.
	 * @throws SecureMessagingException Thrown in case there is an error processing the stage.
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	@Throws(SecureMessagingException::class)
	fun processCommand(
		smApdu: CommandApdu,
		dos: List<Tlv>,
	): List<Tlv>
}

/**
 * Interface for a sub-computation of a Response APDU in secure messaging.
 */
interface ResponseStage {
	/**
	 * Indicate that the stage must not fail the process, if it is not applicable.
	 * This can for example happen if the card can optionally send a mac.
	 * Setting this flag to `true` continues without error, when the mac is missing.
	 */
	val isOptional: Boolean

	/**
	 * Process a Response APDU as part of the secure messaging computation.
	 *
	 * @return The processed SM DOs for processing in the next stage, or for building the unsecured APDU when this is
	 *   the last stage.
	 * @param dos SM DOs based on the Response APDU when this is the first stage, or the DOs of the
	 *   previous stage for all remaining stages.
	 * @throws SecureMessagingException Thrown in case there is an error processing the stage.
	 */
	@Throws(SecureMessagingException::class)
	fun processResponse(dos: List<Tlv>): List<Tlv>

	/**
	 * Processes the stage with an error APDU.
	 * Typically, this is used to advance internal counters without actually processing anything.
	 */
	fun processError()
}
