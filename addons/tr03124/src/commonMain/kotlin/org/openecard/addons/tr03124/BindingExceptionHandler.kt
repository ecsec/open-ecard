package org.openecard.addons.tr03124

import kotlinx.coroutines.CancellationException
import org.openecard.addons.tr03124.transport.EserviceClient

suspend fun <T> runEacCatching(
	eserviceClient: EserviceClient,
	block: suspend () -> T,
): T {
	try {
		return block()
	} catch (ex: CancellationException) {
		throw UserCanceled(eserviceClient, cause = ex)
	} catch (ex: BindingException) {
		throw ex
	} catch (ex: Exception) {
		throw ClientError(eserviceClient, cause = ex)
	}
}
