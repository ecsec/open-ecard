package org.openecard.sc.iface

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.iface.SecureMessagingException

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
		InternalSystemError::class,
	)
	fun transmit(apdu: CommandApdu): ResponseApdu

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
		InternalSystemError::class,
	)
	fun close()

	@Throws(InternalSystemError::class)
	fun setSecureMessaging(sm: SecureMessaging)

	@Throws(InternalSystemError::class)
	fun removeSecureMessaging()
}
