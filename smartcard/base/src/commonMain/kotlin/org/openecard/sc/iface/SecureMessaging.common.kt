package org.openecard.sc.iface

interface SecureMessaging {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun processRequest(requestApdu: UByteArray): UByteArray

	@OptIn(ExperimentalUnsignedTypes::class)
	fun processResponse(responseApdu: UByteArray): UByteArray
}
