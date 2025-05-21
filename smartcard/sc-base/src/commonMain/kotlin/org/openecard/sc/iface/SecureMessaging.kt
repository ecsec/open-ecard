package org.openecard.sc.iface

interface SecureMessaging {
	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processRequest(requestApdu: UByteArray): UByteArray

	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processResponse(responseApdu: UByteArray): UByteArray
}

class SecureMessagingException(
	msg: String? = null,
	cause: Throwable? = null,
) : Exception(msg ?: "There was an error while executing secure messaging.", cause)
