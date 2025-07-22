package org.openecard.addons.tr03124.eac

interface EacUiData {
	val certificateDescription: Any
	val requiredChat: Any
	val optionalChat: Any
	// TODO: Add more data of the process

	companion object {
		fun build(): EacUiData {
			TODO()
		}
	}
}
