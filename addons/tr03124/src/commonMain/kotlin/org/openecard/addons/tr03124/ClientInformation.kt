package org.openecard.addons.tr03124

import org.openecard.addons.tr03124.xml.ECardConstants

data class ClientInformation(
	val userAgent: UserAgent,
	val apiVersion: List<ApiVersion> = listOf(ECardConstants.ecardApiVersion),
	val supportedDidProtocols: List<String> = listOf(),
)

data class UserAgent(
	val name: String,
	val version: Version,
) {
	data class Version(
		val major: Int,
		val minor: Int,
		val patch: Int?,
	)
}

data class ApiVersion(
	val major: Int,
	val minor: Int,
	val patch: Int?,
)
