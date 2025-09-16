package org.openecard.addons.tr03124.eac

import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.pace.cvc.AuthenticatedAuxiliaryData
import org.openecard.sc.pace.cvc.AuthenticationTerminalChat
import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CertificateDescription

class EacUiData(
	val terminalCert: CardVerifiableCertificate,
	val certificateDescription: CertificateDescription,
	val terminalCertChat: AuthenticationTerminalChat,
	val requiredChat: AuthenticationTerminalChat,
	val optionalChat: AuthenticationTerminalChat,
	val aad: AuthenticatedAuxiliaryData?,
	val transactionInfo: String?,
	val pinType: PacePinId,
	val acceptedEidType: List<String>,
)
