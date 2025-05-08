package org.openecard.sc.iface

interface TerminalFactory {
	val name: String

	fun load(): Terminals
}
