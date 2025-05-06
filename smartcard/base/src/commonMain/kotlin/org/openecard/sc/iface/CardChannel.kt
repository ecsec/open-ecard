package org.openecard.sc.iface

interface CardChannel : AutoCloseable {
	val card: Card
	val channelNumber: Int
	val isBasicChannel: Boolean
		get() = channelNumber == 0
	val isLogicalChannel: Boolean
		get() = !isBasicChannel

	fun transmit(apdu: ByteArray): ByteArray

	override fun close()

	fun pushSecureMessaging(sm: SecureMessaging)

	fun popSecureMessaging()

	fun cleanSecureMessaging()
}

fun CardChannel.transmit(apdu: CommandApdu): ResponseApdu = transmit(apdu.toBytes).toResponseApdu()
