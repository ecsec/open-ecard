package org.openecard.sc.iface

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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

	@Throws(
		CancellationException::class,
		// pcsc errors
		NoService::class,
		InvalidHandle::class,
		InvalidValue::class,
		InvalidParameter::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	suspend fun waitForCardPresent()

	@Throws(
		CancellationException::class,
		// pcsc errors
		NoService::class,
		InvalidHandle::class,
		InvalidValue::class,
		InvalidParameter::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	suspend fun waitForCardAbsent()
}

@Throws(
	TimeoutCancellationException::class,
	// pcsc errors
	NoService::class,
	InvalidHandle::class,
	InvalidValue::class,
	InvalidParameter::class,
	ReaderUnavailable::class,
	UnknownReader::class,
	Timeout::class,
	Cancelled::class,
)
fun Terminal.waitForCardPresent(
	timeout: Duration,
	dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
	waitForCard(timeout, dispatcher) { waitForCardPresent() }
}

@Throws(
	TimeoutCancellationException::class,
	// pcsc errors
	NoService::class,
	InvalidHandle::class,
	InvalidValue::class,
	InvalidParameter::class,
	ReaderUnavailable::class,
	UnknownReader::class,
	Timeout::class,
	Cancelled::class,
)
fun Terminal.waitForCardAbsent(
	timeout: Duration,
	dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
	waitForCard(timeout, dispatcher) { waitForCardAbsent() }
}

private inline fun waitForCard(
	timeout: Duration,
	dispatcher: CoroutineDispatcher,
	crossinline waitFun: suspend () -> Unit,
) {
	runBlocking {
		withContext(dispatcher) {
			withTimeout(timeout) {
				waitFun.invoke()
			}
		}
	}
}
