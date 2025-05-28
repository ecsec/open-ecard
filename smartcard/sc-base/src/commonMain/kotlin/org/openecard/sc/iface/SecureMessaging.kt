package org.openecard.sc.iface

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu

interface SecureMessaging {
	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processRequest(requestApdu: CommandApdu): CommandApdu

	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processResponse(responseApdu: ResponseApdu): ResponseApdu
}

class SecureMessagingException(
	msg: String? = null,
	cause: Throwable? = null,
) : Exception(msg ?: "There was an error while executing secure messaging.", cause)
