package org.openecard.addons.tr03124.xml

import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toBindingType
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toSecurityProtocolType
import org.openecard.utils.serialization.PrintableUByteArray
import kotlin.jvm.Throws

sealed interface TcToken {
	val communicationErrorAddress: String?

	sealed interface TcTokenOk : TcToken {
		val serverAddress: String
		val sessionIdentifier: String
		val refreshAddress: String
		override val communicationErrorAddress: String?
		val binding: TcTokenXml.BindingType
	}

	data class TcTokenPsk(
		override val serverAddress: String,
		override val sessionIdentifier: String,
		override val refreshAddress: String,
		override val communicationErrorAddress: String?,
		override val binding: TcTokenXml.BindingType,
		val psk: PrintableUByteArray,
	) : TcTokenOk

	data class TcTokenAttached(
		override val serverAddress: String,
		override val sessionIdentifier: String,
		override val refreshAddress: String,
		override val communicationErrorAddress: String?,
		override val binding: TcTokenXml.BindingType,
	) : TcTokenOk

	data class TcTokenError(
		override val communicationErrorAddress: String,
	) : TcToken

	companion object {
		@Throws(IllegalArgumentException::class)
		fun TcTokenXml.toTcToken(): TcToken =
			if (serverAddress != null && serverAddress.isNotEmpty() && sessionIdentifier != null && refreshAddress != null &&
				refreshAddress.isNotEmpty() &&
				binding != null
			) {
				val binding = binding.toBindingType()
				val securityProtocol = securityProtocol?.toSecurityProtocolType()
				if (securityProtocol == TcTokenXml.SecurityProtocolType.TLS_PSK && securityParameters != null) {
					TcTokenPsk(
						serverAddress,
						sessionIdentifier,
						refreshAddress,
						communicationErrorAddress,
						binding,
						securityParameters.psk,
					)
				} else {
					TcTokenAttached(
						serverAddress,
						sessionIdentifier,
						refreshAddress,
						communicationErrorAddress,
						binding,
					)
				}
			} else if (communicationErrorAddress != null) {
				TcTokenError(communicationErrorAddress)
			} else {
				throw IllegalArgumentException("Received TCToken which neither usable, nor represents an error")
			}
	}
}
