package org.openecard.richclient.tr03124

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient

object TransportLogging {
	fun loadEidLogger() {
		Tr03124Config.httpLog = {
			level = LogLevel.ALL
			logger = buildKtorLogger(EserviceClient::class.java.name)
		}
		Tr03124Config.paosLog = {
			level = LogLevel.ALL
			logger = buildKtorLogger(EidServerInterface::class.java.name)
		}
	}

	private fun buildKtorLogger(name: String): Logger {
		val logger = KotlinLogging.logger(name)
		return object : Logger {
			override fun log(message: String) {
				logger.debug { message }
			}
		}
	}
}
