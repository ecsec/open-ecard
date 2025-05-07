package org.openecard.sc.iface

import kotlin.time.Duration

interface Terminal {
	val terminals: Terminals
	val name: String

	@Throws(
		NoService::class,
		InvalidHandle::class,
		InvalidValue::class,
		InvalidParameter::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	fun isCardPresent(): Boolean

	@Throws(
		NoService::class,
		InvalidHandle::class,
		InvalidValue::class,
		InvalidParameter::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	fun getState(): TerminalStateType

	fun connectTerminalOnly(): TerminalConnection

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoMemory::class,
		NoService::class,
		ReaderUnavailable::class,
		CommError::class,
		InternalSystemError::class,
		RemovedCard::class,
		ResetCard::class,
		NoSmartcard::class,
		ProtoMismatch::class,
		SharingViolation::class,
		UnknownReader::class,
		UnsupportedFeature::class,
		UnpoweredCard::class,
		UnresponsiveCard::class,
	)
	fun connect(
		protocol: PreferredCardProtocol = PreferredCardProtocol.ANY,
		shareMode: ShareMode = ShareMode.SHARED,
	): TerminalConnection

	// TODO: errors
	suspend fun waitForCardPresent(timeout: Duration)

	// TODO: errors
	suspend fun waitForCardAbsent(timeout: Duration)
}
