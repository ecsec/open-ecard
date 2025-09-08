package org.openecard.addons.tr03124.eac

import io.ktor.utils.io.CancellationException
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.ClientError
import org.openecard.addons.tr03124.transport.EserviceClient

suspend fun <T> runEacCatching(
	eserviceClient: EserviceClient,
	block: suspend () -> T,
): T {
	try {
		return block()
	} catch (ex: CancellationException) {
		throw ex
	} catch (ex: BindingException) {
		throw ex
	} catch (ex: Exception) {
		throw ClientError(eserviceClient, cause = ex)
	}
}
