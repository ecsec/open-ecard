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
	)
	fun releaseContext()

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		NoMemory::class,
		// TODO: check if this can happen, or we simply get no readers
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
		// TODO: check if this can happen, or we simply get no readers
		NoReadersAvailable::class,
		NoService::class,
		InvalidValue::class,
		ReaderUnavailable::class,
		UnknownReader::class,
		Timeout::class,
		Cancelled::class,
	)
	fun getTerminal(name: String): Terminal?

// 	fun terminalWatcher(): TerminalWatcher
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
