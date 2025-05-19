package org.openecard.sc.iface

enum class CardProtocol {
	RAW,
	T0,
	T1,
	T15,
	TCL,
}

enum class PreferredCardProtocol {
	T0,
	T1,
	RAW,
	ANY,
}
