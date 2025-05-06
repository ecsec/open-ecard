package org.openecard.sc.iface

data class TerminalCapabilities(
	val hasDisplay: Boolean,
	val protocols: List<String>,
	val canVerifyPin: Boolean,
	val canChangePin: Boolean,
	val canGenericPace: Boolean,
	val canGermanEidPace: Boolean,
	val canDestroyChannelPace: Boolean,
	val canQesPace: Boolean,
	val biometricTypes: List<Int>,
)
