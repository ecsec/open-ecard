package org.openecard.addons.tr03124

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient

private val log = KotlinLogging.logger { }

@Throws(BindingException::class)
suspend fun <T> runEacCatching(
	eserviceClient: EserviceClient,
	eidServer: EidServerInterface?,
	block: suspend () -> T,
): T {
	try {
		return block()
	} catch (ex: Exception) {
		log.info(ex.takeIf { log.isDebugEnabled() }) { "Exception raised during EAC process" }
		val bindEx = handleExeptions(eserviceClient, eidServer, ex)

		// close channel to eID-Server if necessary
		eidServer?.sendError(bindEx)
		throw bindEx
	}
}

expect fun handleExeptions(
	eserviceClient: EserviceClient,
	eidServer: EidServerInterface?,
	ex: Exception,
): BindingException
