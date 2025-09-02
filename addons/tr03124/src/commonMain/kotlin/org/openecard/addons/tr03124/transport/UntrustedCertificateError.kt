package org.openecard.addons.tr03124.transport

internal class UntrustedCertificateError(
	msg: String,
	cause: Throwable? = null,
) : Exception(msg, cause)
