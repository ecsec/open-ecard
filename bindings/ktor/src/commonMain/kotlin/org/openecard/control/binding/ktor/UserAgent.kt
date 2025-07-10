package org.openecard.control.binding.ktor

import org.openecard.control.binding.ktor.Versioned.Companion.toTr03124Version

data class Versioned(
	val name: String,
	val version: String,
) {
	val commentString by lazy { "$name/$version" }

	companion object {
		fun String.toTr03124Version(): Versioned = Versioned("TR-03124-1", this)
	}
}

data class UserAgent(
	val appName: String,
	val appVersion: String,
	val comment: String? = null,
) {
	fun toHeaderValue(): String = "$appName/$appVersion (${comment ?: ""})"

	companion object {
		fun tr01324UserAgent(
			appName: String,
			appVersion: String,
			supportedSpecifications: List<Versioned> = listOf("1.1", "1.2", "1.3").map { it.toTr03124Version() },
		): UserAgent {
			val comment =
				if (supportedSpecifications.isNotEmpty()) {
					supportedSpecifications.joinToString(" ") { it.commentString }
				} else {
					null
				}
			return UserAgent(appName, appVersion, comment)
		}
	}
}
