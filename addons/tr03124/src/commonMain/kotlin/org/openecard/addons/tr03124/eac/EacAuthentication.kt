package org.openecard.addons.tr03124.eac

import org.openecard.addons.tr03124.xml.Eac2Output
import org.openecard.addons.tr03124.xml.EacAdditionalInput

interface EacAuthentication {
	/**
	 * Execute Terminal Authentication.
	 * The value needs to be sent to the eID-Server. If a new message is returned it must be
	 * [EacAdditionalInput]. This new message needs to be provided to the [processAdditional] function.
	 */
	fun process(): Eac2Output

	/**
	 * Process Chip Authentication.
	 * If [EacAdditionalInput] has been received from the eID-Server, then it needs to be provided here.
	 */
	fun processAdditional(additionalInput: EacAdditionalInput): Eac2Output
}
