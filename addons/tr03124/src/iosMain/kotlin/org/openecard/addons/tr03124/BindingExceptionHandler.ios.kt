package org.openecard.addons.tr03124

import kotlinx.coroutines.CancellationException
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.transport.InvalidTlsParameter
import org.openecard.addons.tr03124.transport.UntrustedCertificateError

actual fun handleExeptions(
	eserviceClient: EserviceClient,
	eidServer: EidServerInterface?,
	ex: Exception,
): BindingException =

	when (ex) {
		is CancellationException -> {
			UserCanceled(eserviceClient, cause = ex)
		}

		is UntrustedCertificateError -> {
			UnknownTrustedChannelError(eserviceClient, "Channel used untrusted certificate", ex)
		}

		is InvalidTlsParameter -> {
			UnknownTrustedChannelError(eserviceClient, "Channel used invalid parameters", ex)
		}

		// is IOException -> {
		// 	doIf(ex.cause != null) { handleIoExceptions(eserviceClient, ex) }
		// 		?: UnknownTrustedChannelError(eserviceClient, "Unknown error in channel establishment", ex)
		// }
		is BindingException -> {
			ex
		}

		else -> {
			UnknownClientError(eserviceClient, cause = ex)
		}
	}
