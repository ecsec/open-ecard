package org.openecard.sc.iface

interface CardChannel {
	val card: Card
	val channelNumber: Int
	val isBasicChannel: Boolean
		get() = channelNumber == 0
	val isLogicalChannel: Boolean
		get() = !isBasicChannel

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ProtoMismatch::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
	)
	fun transmit(apdu: ByteArray): ByteArray

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ProtoMismatch::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
		LogicalChannelException::class,
	)
	fun close()

	fun pushSecureMessaging(sm: SecureMessaging)

	fun popSecureMessaging()

	fun cleanSecureMessaging()
}

@Throws(
	InsufficientBuffer::class,
	InvalidHandle::class,
	InvalidParameter::class,
	InvalidValue::class,
	NoService::class,
	NotTransacted::class,
	ProtoMismatch::class,
	ReaderUnavailable::class,
	CommError::class,
	ResetCard::class,
	RemovedCard::class,
)
fun CardChannel.transmit(apdu: CommandApdu): ResponseApdu = transmit(apdu.toBytes).toResponseApdu()
