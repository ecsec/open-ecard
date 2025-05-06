package org.openecard.sc.iface

import kotlin.time.Duration

interface Terminal {
	val terminals: Terminals
	val name: String

	@get:Throws(ReaderUnavailable::class, InvalidHandle::class)
	val isCardPresent: Boolean

	@get:Throws(ReaderUnavailable::class, InvalidHandle::class)
	val state: TerminalStateType

	fun connectTerminalOnly(): TerminalConnection

	fun connect(
		protocol: PreferredCardProtocol = PreferredCardProtocol.ANY,
		shareMode: ShareMode = ShareMode.SHARED,
	): TerminalConnection

	suspend fun waitForCardPresent(timeout: Duration)

	suspend fun waitForCardAbsent(timeout: Duration)
}
