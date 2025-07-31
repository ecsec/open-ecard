package org.openecard.addons.tr03124

import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.SupportedAPIVersionsType
import org.openecard.addons.tr03124.xml.UserAgentType

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

fun UserAgent.toXmlType(): UserAgentType =
	UserAgentType(
		name = this.name,
		versionMajor = this.version.major,
		versionMinor = this.version.minor,
		versionSubminor = this.version.patch,
	)

fun ApiVersion.toXmlType(): SupportedAPIVersionsType =
	SupportedAPIVersionsType(major = this.major, minor = this.minor, subminor = this.patch)
