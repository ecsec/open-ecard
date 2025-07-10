package org.openecard.control.binding.ktor

data class Versioned(
	val name: String,
	val version: String,
)

data class UserAgent(
	val appName: String,
	val appVersion: String,
	val supportedSpecifications: List<Versioned>,
) {
	fun toHeaderValue(): String {
		val builder = StringBuilder()

		builder.append(appName)
		builder.append("/")
		builder.append(appVersion)

		builder.append(" (")
		var firstSpec = true
		for (specification in supportedSpecifications) {
			if (!firstSpec) {
				builder.append(" ")
			} else {
				firstSpec = false
			}
			builder.append(specification.name)
			builder.append("/")
			builder.append(specification.version)
		}
		builder.append(")")

		return builder.toString()
	}
}
