package org.openecard.sc.iface

interface Card {
	val terminalConnection: TerminalConnection

	val protocol: CardProtocol
	val isContactless: Boolean
	val basicChannel: CardChannel

	var setCapabilities: CardCapabilities?

	/**
	 * Override contactless information.
	 * There are implementations such as PersoSim, which don't allow to properly determine the contactless status.
	 * This makes it possible to override the value, e.g. when we are dealing with a specific card type which is always
	 * contactless such as the nPA.
	 */
	var setContactless: Boolean?

	/**
	 * Card Capabilities object.
	 * Implementations can use the ones from the `ATR`, or it can be set explicitly after reading `EF.ATR`.
	 */
	@Throws(
		CommError::class,
		RemovedCard::class,
		InternalSystemError::class,
	)
	fun getCapabilities(): CardCapabilities? = atr().historicalBytes?.cardCapabilities ?: setCapabilities

	@Throws(
		CommError::class,
		RemovedCard::class,
		InternalSystemError::class,
	)
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
		InternalSystemError::class,
	)
	fun openLogicalChannel(): CardChannel
}
