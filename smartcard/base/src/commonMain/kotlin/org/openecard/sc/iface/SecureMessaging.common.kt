package org.openecard.sc.iface

interface SecureMessaging {
	fun processRequest(requestApdu: ByteArray): ByteArray

	fun processResponse(responseApdu: ByteArray): ByteArray
}
