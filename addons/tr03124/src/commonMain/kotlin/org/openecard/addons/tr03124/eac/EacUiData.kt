package org.openecard.addons.tr03124.eac

import org.openecard.sc.pace.cvc.AuthenticationTerminalChat
import org.openecard.sc.pace.cvc.CertificateDescription

class EacUiData(
	val certificateDescription: CertificateDescription,
	val requiredChat: AuthenticationTerminalChat,
	val optionalChat: AuthenticationTerminalChat,
	val transactionInfo: String?,
	val acceptedEidType: List<String>,
	// TODO: Add more data of the process
)
