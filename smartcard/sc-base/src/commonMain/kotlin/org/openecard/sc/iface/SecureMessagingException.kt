package org.openecard.sc.iface

sealed class SecureMessagingException(
	msg: String? = null,
	cause: Throwable? = null,
) : Exception(msg ?: "There was an error while executing secure messaging.", cause)

class NoSwData(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "No SW data object found in secure messaging DOs.", cause)

class InvalidSwData(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Invalid SW data object found in secure messaging DOs.", cause)
