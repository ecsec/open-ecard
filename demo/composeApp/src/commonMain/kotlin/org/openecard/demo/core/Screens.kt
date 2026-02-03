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

//@Serializable
//object EAC

@Serializable
object EGK

@Serializable
object Success

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
	val url: String,
)

@Serializable
data class EgkResult(
	val result: String,
)

@Serializable
data class EAC(
	val tokenUrl: String,
)
