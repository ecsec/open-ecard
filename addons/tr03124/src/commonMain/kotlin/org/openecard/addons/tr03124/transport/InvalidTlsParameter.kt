package org.openecard.addons.tr03124.transport

internal class InvalidTlsParameter(
	msg: String,
	cause: Throwable? = null,
) : Exception(msg, cause)
