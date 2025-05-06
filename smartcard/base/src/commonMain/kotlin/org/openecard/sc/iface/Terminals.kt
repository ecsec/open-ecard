package org.openecard.sc.iface

interface Terminals {
	val factory: TerminalFactory
	val isEstablished: Boolean
	val supportsControlCommand: Boolean

	fun establishContext()

	fun releaseContext()

	@Throws(InvalidHandle::class)
	fun list(): List<Terminal>

	@Throws(InvalidHandle::class)
	fun getTerminal(name: String): Terminal?

// 	fun terminalWatcher(): TerminalWatcher
}

fun <T : Terminals, R> T.withContext(block: (T) -> R): R {
	establishContext()
	try {
		return block.invoke(this)
	} finally {
		releaseContext()
	}
}

suspend fun <T : Terminals, R> T.withContextSuspend(block: suspend (T) -> R): R {
	establishContext()
	try {
		return block.invoke(this)
	} finally {
		releaseContext()
	}
}
