package org.openecard.addons.tr03124.xml

import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toBindingType
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toSecurityProtocolType
import org.openecard.utils.serialization.PrintableUByteArray

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
		override val communicationErrorAddress: String?,
		val refreshAddress: String?,
		val invalidData: Boolean,
	) : TcToken

	companion object {
		@Throws(IllegalArgumentException::class)
		fun TcTokenXml.toTcToken(): TcToken {
			val serverAddressOk = serverAddress != null && serverAddress.isNotEmpty()
			val sessionOk = sessionIdentifier != null && sessionIdentifier.isNotEmpty()
			val refreshOk = refreshAddress != null && refreshAddress.isNotEmpty()
			val bindingType = runCatching { binding?.toBindingType() }.getOrNull()
			return if (serverAddressOk && sessionOk && refreshOk && bindingType != null) {
				val securityProtocol = securityProtocol?.toSecurityProtocolType()
				val psk = securityParameters?.psk
				if (securityProtocol == TcTokenXml.SecurityProtocolType.TLS_PSK && psk != null) {
					TcTokenPsk(
						serverAddress,
						sessionIdentifier,
						refreshAddress,
						communicationErrorAddress,
						bindingType,
						psk,
					)
				} else {
					TcTokenAttached(
						serverAddress,
						sessionIdentifier,
						refreshAddress,
						communicationErrorAddress,
						bindingType,
					)
				}
			} else {
				val invalidData = serverAddressOk || sessionOk || refreshOk || binding != null
				TcTokenError(communicationErrorAddress, refreshAddress.takeIf { invalidData }, invalidData)
			}
		}
	}
}
