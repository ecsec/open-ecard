/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.addons.cardlink.ws

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


const val READY = "ready"
const val REGISTER_EGK = "registerEGK"
const val SEND_APDU = "sendAPDU"
const val SEND_APDU_RESPONSE = "sendAPDUResponse"

const val TASK_LIST_ERROR = "receiveTasklistError"

// Newly defined types
const val REQUEST_SMS_TAN = "requestSMSCode"
const val REQUEST_SMS_TAN_RESPONSE = "requestSMSCodeResponse"
const val CONFIRM_TAN = "confirmSMSCode"
const val CONFIRM_TAN_RESPONSE = "confirmSMSCodeResponse"

// additional types for the base specification
const val SESSION_INFO = "sessionInformation"
const val REGISTER_EGK_FINISH = "registerEgkFinish"


@Serializable(with = GematikMessageSerializer::class)
class GematikEnvelope {
	val payload: CardLinkPayload
	val correlationId: String?
	val cardSessionId: String?
}

@Serializable
@SerialName("TaskListErrorEnvelope")
class TaskListErrorEnvelope(
	override val payload: TasklistErrorPayload,
	override val correlationId: String? = null,
) : GematikMessage

@Serializable
@SerialName("CardEnvelope")
class CardEnvelope(
	override val payload: CardLinkPayload?,
	override val payloadType: String,
	override val correlationId: String,
) : GematikMessage

object GematikMessageSerializer : KSerializer<GematikMessage> {
	// Not really used, but must be implemented
	override val descriptor : SerialDescriptor = buildClassSerialDescriptor("GematikMessage") {
		element<String>("cardSessionId")
		element<String>("correlationId")
		element<CardLinkPayload>("payload")
		element<String>("payloadType")
	}

	override fun serialize(encoder: Encoder, value: GematikMessage) {
		val jsonElement = when (value) {
			is TaskListErrorEnvelope -> serializeTasklistError(value)
			is CardEnvelope -> serializeGematikMessage(value.payload, value.payloadType, value.cardSessionId, value.correlationId)
			else -> throw IllegalArgumentException("Unsupported Gematik message.")
		}
		encoder.encodeSerializableValue(JsonElement.serializer(), jsonElement)
	}

	@OptIn(ExperimentalEncodingApi::class)
	private fun serializeGematikMessage(payload: CardLinkPayload?, payloadType: String, cardSessionId: String?, correlationId: String?) : JsonElement {
		val base64EncodedPayload: String? = payload?.let {
			val payloadJsonStr = cardLinkJsonFormatter.encodeToString(payload)
			Base64.encode(payloadJsonStr.encodeToByteArray()).trimEnd('=')
		}
		val jsonPayload = buildJsonObject {
			put("type", payloadType)
			put("payload", base64EncodedPayload)
		}
		return buildJsonArray {
			add(jsonPayload)
			cardSessionId?.let { add(it) }
			correlationId?.let { add(it) }
		}
	}

	@OptIn(ExperimentalEncodingApi::class)
	private fun serializeTasklistError(taskListErrorEnvelope: TaskListErrorEnvelope) : JsonElement {
		val payloadJsonStr = cardLinkJsonFormatter.encodeToString(taskListErrorEnvelope.payload)
		val base64EncodedPayload = Base64.encode(payloadJsonStr.encodeToByteArray()).trimEnd('=')
		return buildJsonObject {
			put("type", TASK_LIST_ERROR)
			put("payload", base64EncodedPayload)
		}
	}

	override fun deserialize(decoder: Decoder): GematikMessage {
		when (val websocketMessage = decoder.decodeSerializableValue(JsonElement.serializer())) {
			is JsonObject -> {
				val payloadType = websocketMessage.jsonObject["type"]?.jsonPrimitive?.content
					?: throw IllegalArgumentException("Payload type of TaskListErrorMessage is missing.")
				val payload = websocketMessage.jsonObject["payload"]?.jsonPrimitive?.content
					?: throw IllegalArgumentException("Payload of TaskListErrorMessage is missing.")
				val typedJsonElement = toTypedJsonElement(payload, payloadType)

				return TaskListErrorEnvelope(
					cardLinkJsonFormatter.decodeFromJsonElement<TasklistErrorPayload>(typedJsonElement),
				)
			}
			is JsonArray -> {
				val gematikMessage = websocketMessage.jsonArray.getOrNull(0)?.jsonObject
					?: throw IllegalArgumentException("Web-Socket Gematik message does not contain an Egk message.")
				val cardSessionId = websocketMessage.jsonArray.getOrNull(1)?.jsonPrimitive?.content
				val correlationId = websocketMessage.jsonArray.getOrNull(2)?.jsonPrimitive?.content

				val payloadType = gematikMessage["type"]?.jsonPrimitive?.content
					?: throw IllegalArgumentException("Web-Socket Gematik message does not contain a type.")
				val payload = gematikMessage["payload"]?.jsonPrimitive?.content
					?: throw IllegalArgumentException("Web-Socket Gematik message does not contain a payload.")

				val typedJsonElement = toTypedJsonElement(payload, payloadType)
				val cardLinkPayload = cardLinkJsonFormatter.decodeFromJsonElement<CardLinkPayload>(typedJsonElement)

				return if (cardSessionId != null && correlationId != null) {
					CardEnvelope(cardLinkPayload, payloadType, cardSessionId, correlationId)
				} else if (correlationId != null) {
					PairEnvelope(cardLinkPayload, payloadType, correlationId)
				} else {
					ZeroEnvelope(cardLinkPayload, payloadType)
				}
			}
			else -> {
				throw IllegalStateException("Received malformed Web-Socket message.")
			}
		}
	}

	@OptIn(ExperimentalEncodingApi::class)
	fun toTypedJsonElement(base64EncodedPayload: String, payloadType: String) : JsonObject {
		val jsonPayload = String(Base64.decode(base64EncodedPayload))
		val jsonElement = Json.parseToJsonElement(jsonPayload)
		return JsonObject(jsonElement.jsonObject.toMutableMap().apply {
			put(Json.configuration.classDiscriminator, JsonPrimitive(payloadType))
		})
	}
}


typealias ByteArrayAsBase64 = @Serializable(ByteArrayAsBase64Serializer::class) ByteArray

@OptIn(ExperimentalEncodingApi::class)
object ByteArrayAsBase64Serializer : KSerializer<ByteArray> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArrayAsBase64Serializer", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ByteArray) {
		val base64Encoded = Base64.encode(value).trimEnd('=')
		encoder.encodeString(base64Encoded)
	}

	override fun deserialize(decoder: Decoder): ByteArray {
		return Base64.decode(decoder.decodeString())
	}
}

val module = SerializersModule {
	polymorphic(GematikMessage::class) {
		subclass(TaskListErrorEnvelope::class)
		subclass(ZeroEnvelope::class)
		subclass(PairEnvelope::class)
		subclass(CardEnvelope::class)
	}
	polymorphic(CardLinkPayload::class) {
		subclass(RegisterEgk::class)
		subclass(SendApdu::class)
		subclass(SendApduResponse::class)
		subclass(SendPhoneNumber::class)
		subclass(SendTan::class)
		subclass(ConfirmTan::class)
		subclass(ConfirmPhoneNumber::class)
		subclass(RegisterEgkFinish::class)
	}
}

val cardLinkJsonFormatter = Json { serializersModule = module; classDiscriminatorMode = ClassDiscriminatorMode.NONE }

sealed interface CardLinkPayload


@Serializable
@SerialName(SESSION_INFO)
data class SessionInformation(
	val webSocketId: String,
	val phoneRegistered: Boolean,
)

@Serializable
@SerialName(REGISTER_EGK)
data class RegisterEgk(
	val cardSessionId: String,
	val gdo: ByteArrayAsBase64,
	val cardVersion: ByteArrayAsBase64,
	val x509AuthRSA: ByteArrayAsBase64? = null,
	val x509AuthECC: ByteArrayAsBase64,
	val cvcAuth: ByteArrayAsBase64,
	val cvcCA: ByteArrayAsBase64,
	val atr: ByteArrayAsBase64,
) : CardLinkPayload

@Serializable
@SerialName(SEND_APDU)
data class SendApdu(
	val cardSessionId: String,
	val apdu: ByteArrayAsBase64,
) : CardLinkPayload

@Serializable
@SerialName(SEND_APDU_RESPONSE)
data class SendApduResponse(
	val cardSessionId: String,
	val response: ByteArrayAsBase64,
) : CardLinkPayload

@Serializable
@SerialName(TASK_LIST_ERROR)
data class TasklistErrorPayload(
	val cardSessionId: String,
	val status: Int,
	val tistatus: String? = null,
	val rootcause: String? = null,
	val errormessage: String? = null,
) : CardLinkPayload

@Serializable
@SerialName(REQUEST_SMS_TAN)
data class SendPhoneNumber(val phoneNumber: String) : CardLinkPayload

@Serializable
@SerialName(REQUEST_SMS_TAN_RESPONSE)
data class ConfirmPhoneNumber(
	var minor: MinorResultCode?,
	var errorMessage: String?,
) : CardLinkPayload

@Serializable
@SerialName(CONFIRM_TAN)
data class SendTan(val tan: String) : CardLinkPayload

@Serializable
@SerialName(CONFIRM_TAN_RESPONSE)
data class ConfirmTan(
	var minor: MinorResultCode?,
	var errorMessage: String?,
) : CardLinkPayload

@Serializable
enum class MinorResultCode {
	NUMBER_FROM_WRONG_COUNTRY,
	TAN_EXPIRED,
	TAN_INCORRECT,
	TAN_RETRY_LIMIT_EXCEEDED,
	UNKNOWN_ERROR,
}

@Serializable
@SerialName(REGISTER_EGK_FINISH)
data class RegisterEgkFinish(
	val removeCard: Boolean,
) : CardLinkPayload
