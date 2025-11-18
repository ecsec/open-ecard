package org.openecard.addons.tr03124

import kotlinx.coroutines.CancellationException
import org.bouncycastle.tls.TlsFatalAlert
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.transport.InvalidTlsParameter
import org.openecard.addons.tr03124.transport.UntrustedCertificateError
import org.openecard.utils.common.doIf
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException

actual fun handleExeptions(
	eserviceClient: EserviceClient,
	eidServer: EidServerInterface?,
	ex: Exception,
): BindingException =
	when (ex) {
		is CancellationException ->
			UserCanceled(eserviceClient, cause = ex)
		is UntrustedCertificateError ->
			UnknownTrustedChannelError(eserviceClient, "Channel used untrusted certificate", ex)
		is InvalidTlsParameter ->
			UnknownTrustedChannelError(eserviceClient, "Channel used invalid parameters", ex)
		is IOException -> {
			doIf(ex.cause != null) { handleIoExceptions(eserviceClient, ex) }
				?: UnknownTrustedChannelError(eserviceClient, "Unknown error in channel establishment", ex)
		}
		is BindingException ->
			ex
		else ->
			UnknownClientError(eserviceClient, cause = ex)
	}

private fun handleIoExceptions(
	eserviceClient: EserviceClient,
	ex: Exception,
): BindingException =
	when (val cause = ex.cause) {
		is CertificateExpiredException ->
			UnknownTrustedChannelError(eserviceClient, "Channel used expired certificate", ex)
		is CertificateNotYetValidException ->
			UnknownTrustedChannelError(eserviceClient, "Channel used not yet valid certificate", ex)
		is InvalidTlsParameter ->
			UnknownTrustedChannelError(eserviceClient, "Channel used invalid parameters", ex)
		is UntrustedCertificateError ->
			UnknownTrustedChannelError(eserviceClient, "Channel used untrusted certificate", ex)
		is CertificateException ->
			UnknownTrustedChannelError(eserviceClient, "Channel used invalid certificate", ex)
		else ->
			UnknownClientError(eserviceClient, cause = ex)
	}
