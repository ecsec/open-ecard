package org.openecard.addons.tr03124

import io.ktor.client.plugins.logging.LoggingConfig

object Tr03124Config {
	var httpLog: KtorLoggingConfigurator? = null
	var paosLog: KtorLoggingConfigurator? = null
	var nonBsiApprovedCiphers: Boolean = false
	var disableKeySizeCheck: Boolean = false
}

typealias KtorLoggingConfigurator = LoggingConfig.() -> Unit
