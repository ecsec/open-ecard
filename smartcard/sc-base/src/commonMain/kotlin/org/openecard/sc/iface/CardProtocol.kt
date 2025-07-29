package org.openecard.sc.iface

enum class CardProtocol {
	RAW,
	T0,
	T1,
	T15,
	TCL,
	;

	val isContact: Boolean get() {
		return this in setOf(T0, T1, T15)
	}
	val isContactLess: Boolean get() {
		return this in setOf(TCL)
	}
}

enum class PreferredCardProtocol {
	T0,
	T1,
	RAW,
	ANY,
}
