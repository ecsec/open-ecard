package org.openecard.sc.iface

interface Card {
	val terminalConnection: TerminalConnection
	val atr: Atr
	val protocol: CardProtocol
	val isContactless: Boolean
	val basicChannel: CardChannel

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
	fun openLogicalChannel(): CardChannel
}
