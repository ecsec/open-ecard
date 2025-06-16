package org.openecard.sc.iface

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.iface.SecureMessagingException

interface SecureMessaging {
	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processRequest(requestApdu: CommandApdu): CommandApdu

	@Throws(SecureMessagingException::class)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processResponse(responseApdu: ResponseApdu): ResponseApdu
}
