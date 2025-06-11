package org.openecard.sc.iface

interface Card {
	val terminalConnection: TerminalConnection
	val atr: Atr
	val protocol: CardProtocol
	val isContactless: Boolean
	val basicChannel: CardChannel

	/**
	 * Card Capabilities object.
	 * Implementations can use the ones from the `ATR`, or it can be set explicitly after reading `EF.ATR`.
	 */
	var capabilities: CardCapabilities?

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
