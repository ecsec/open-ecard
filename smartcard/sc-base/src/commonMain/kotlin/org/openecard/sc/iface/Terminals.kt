package org.openecard.sc.iface

import kotlin.coroutines.cancellation.CancellationException

interface Terminals {
	val factory: TerminalFactory

	val isEstablished: Boolean
	val supportsControlCommand: Boolean

	@Throws(
		InvalidParameter::class,
		InvalidValue::class,
		NoMemory::class,
		NoService::class,
		CommError::class,
		InternalSystemError::class,
	)
	fun establishContext()

	@Throws(
		NoService::class,
		InvalidHandle::class,
		CommError::class,
		InternalSystemError::class,
	)
	fun releaseContext()

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		NoMemory::class,
		NoReadersAvailable::class,
		NoService::class,
		InvalidValue::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	fun list(): List<Terminal>

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		NoMemory::class,
		NoReadersAvailable::class,
		NoService::class,
		InvalidValue::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	fun getTerminal(name: String): Terminal?

	/**
	 * Wait until a terminal is added or removed.
	 * The determination is made based on the provided list of terminals representing the current state.
	 *
	 * @throws ReaderUnsupported when the system does not support reader events.
	 */
	@Throws(
		CancellationException::class,
		// pcsc errors
		NoService::class,
		InvalidParameter::class,
		InvalidValue::class,
		InvalidHandle::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		ReaderUnsupported::class,
		Timeout::class,
	)
	suspend fun waitForTerminalChange(currentState: List<String>)
}

@Throws(
	InvalidHandle::class,
	InvalidParameter::class,
	InvalidValue::class,
	NoMemory::class,
	NoService::class,
	CommError::class,
	InternalSystemError::class,
)
fun <T : Terminals, R> T.withContext(block: (T) -> R): R {
	establishContext()
	try {
		return block.invoke(this)
	} finally {
		releaseContext()
	}
}

@Throws(
	InvalidHandle::class,
	InvalidParameter::class,
	InvalidValue::class,
	NoMemory::class,
	NoService::class,
	CommError::class,
	InternalSystemError::class,
	// coroutines
	CancellationException::class,
)
suspend fun <T : Terminals, R> T.withContextSuspend(block: suspend (T) -> R): R {
	establishContext()
	try {
		return block.invoke(this)
	} finally {
		releaseContext()
	}
}

/**
 * Wait until a terminal is added or removed.
 * The determination is made based on the provided list of terminals representing the current state.
 */
@Throws(
	CancellationException::class,
	// pcsc errors
	NoService::class,
	InvalidParameter::class,
	InvalidValue::class,
	InvalidHandle::class,
	ReaderUnavailable::class,
	UnknownReader::class,
	Timeout::class,
)
suspend fun <T : Terminals, R> T.waitForTerminalChange(currentState: List<Terminal>) {
	waitForTerminalChange(currentState.map { it.name })
}
