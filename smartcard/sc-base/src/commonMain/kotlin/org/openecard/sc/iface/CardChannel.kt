package org.openecard.sc.iface

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu

interface CardChannel {
	val card: Card
	val channelNumber: Int
	val isBasicChannel: Boolean
		get() = channelNumber == 0
	val isLogicalChannel: Boolean
		get() = !isBasicChannel

	@OptIn(ExperimentalUnsignedTypes::class)
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
		SecureMessagingException::class,
	)
	fun transmit(apdu: UByteArray): UByteArray

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

@OptIn(ExperimentalUnsignedTypes::class)
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
	SecureMessagingException::class,
)
fun CardChannel.transmit(apdu: CommandApdu): ResponseApdu = transmit(apdu.toBytes).toResponseApdu()
