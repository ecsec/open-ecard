package org.openecard.addons.tr03124

import kotlinx.coroutines.CancellationException
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.transport.UntrustedCertificateError

@Throws(BindingException::class)
suspend fun <T> runEacCatching(
	eserviceClient: EserviceClient,
	eidServer: EidServerInterface?,
	block: suspend () -> T,
): T {
	try {
		return block()
	} catch (ex: Exception) {
		val bindEx =
			when (ex) {
				is CancellationException ->
					UserCanceled(eserviceClient, cause = ex)
				is UntrustedCertificateError ->
					UnknownTrustedChannelError(eserviceClient, "Channel used untrusted certificate", ex)
				is BindingException ->
					ex
				else ->
					UnknownClientError(eserviceClient, cause = ex)
			}

		// close channel to eID-Server if necessary
		eidServer?.sendError(bindEx)
		throw bindEx
	}
}
