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
object Defaults

@Serializable
object Config

@Serializable
data class PinResult(
	val statusString: String? = null,
	val errorMessage: String? = null,
) {
	val status: PinStatus?
		get() = statusString?.let { PinStatus.valueOf(it) }
}

@Serializable
data class EacResult(
	val resultUrl: String,
)

@Serializable
data class EgkResult(
	val result: String,
)
