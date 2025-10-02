package org.openecard.sc.iface

interface Card {
	val terminalConnection: TerminalConnection

	val protocol: CardProtocol
	val isContactless: Boolean
	val basicChannel: CardChannel

	var setCapabilities: CardCapabilities?

	/**
	 * Card Capabilities object.
	 * Implementations can use the ones from the `ATR`, or it can be set explicitly after reading `EF.ATR`.
	 */
	@Throws(CommError::class, RemovedCard::class)
	fun getCapabilities(): CardCapabilities?

	@Throws(CommError::class, RemovedCard::class)
	fun atr(): Atr

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
