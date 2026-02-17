package org.openecard.demo.core

import kotlinx.serialization.Serializable
import org.openecard.demo.PinStatus

@Serializable
object Start

@Serializable
object PIN

@Serializable
object CAN

@Serializable
object PUK

@Serializable
object NFC

@Serializable
object EacChat

@Serializable
object EacPin

@Serializable
object EGK

@Serializable
object Settings

@Serializable
data class PinResult(
	val pinStatusString: String,
) {
	constructor(pinStatus: PinStatus) : this(pinStatus.name)

	val pinStatus: PinStatus
		get() = PinStatus.valueOf(pinStatusString)
}

@Serializable
data class EacResult(
	val resultUrl: String,
)

@Serializable
data class EgkResult(
	val result: String,
)
