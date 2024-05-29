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
const val REQUEST_SMS_TAN = "requestSmsTan"
const val REQUEST_SMS_TAN_RESPONSE = "requestSmsTanResponse"
const val CONFIRM_TAN = "confirmTan"
const val CONFIRM_TAN_RESPONSE = "confirmTanResponse"

// additional types for the base specification
const val SESSION_INFO = "sessionInformation"
const val REGISTER_EGK_FINISH = "registerEgkFinish"


interface GematikMessage

open class TaskListErrorMessage(
	val payload: TasklistErrorPayload,
) : GematikMessage {
	val type = "receiveTasklistError"
}

@Serializable(with = EgkEnvelopeSerializer::class)
open class GematikEnvelope(
	var payload: CardLinkPayload?,
	var payloadType: String,
) : GematikMessage

class ZeroEnvelope(
	payload: CardLinkPayload?,
	payloadType: String,
) : GematikEnvelope(payload, payloadType)

open class PairEnvelope(
	payload: CardLinkPayload?,
	payloadType: String,
	val correlationId: String,
) : GematikEnvelope(payload, payloadType)

class CardEnvelope(
	payload: CardLinkPayload?,
	payloadType: String,
	correlationId: String,
	var cardSessionId: String,
) : PairEnvelope(payload, payloadType, correlationId)

object EgkEnvelopeSerializer : KSerializer<GematikEnvelope> {
	// Not really used, but must be implemented
	override val descriptor : SerialDescriptor = buildClassSerialDescriptor("EgkEnvelope") {
		element<String>("cardSessionId")
		element<String>("correlationId")
		element<CardLinkPayload>("payload")
		element<String>("payloadType")
	}

	@OptIn(ExperimentalEncodingApi::class)
	override fun serialize(encoder: Encoder, value: CardEnvelope) {
		val payloadJsonStr = cardLinkJsonFormatter.encodeToString(value.payload)
		val base64EncodedPayload = Base64.encode(payloadJsonStr.encodeToByteArray()).trimEnd('=')

		val jsonPayload = buildJsonObject {
			put("type", value.payloadType)
			put("payload", base64EncodedPayload)
		}

		val jsonArray = buildJsonArray {
			add(jsonPayload)
			add(value.cardSessionId)
			value.correlationId?.let { add(it) }
		}
		encoder.encodeSerializableValue(JsonElement.serializer(), jsonArray)
	}

	@OptIn(ExperimentalEncodingApi::class)
	override fun deserialize(decoder: Decoder): CardEnvelope {
		val websocketMessage = decoder.decodeSerializableValue(JsonElement.serializer())

		val egkMessage = websocketMessage.jsonArray.getOrNull(0)?.jsonObject
			?: throw IllegalArgumentException("Web-Socket message does not contain an Egk message.")
		val cardSessionId = websocketMessage.jsonArray.getOrNull(1)?.jsonPrimitive?.content
			?: throw IllegalArgumentException("Web-Socket message does not contain a card session ID.")
		val correlationId = websocketMessage.jsonArray.getOrNull(2)?.jsonPrimitive?.content

		val messageType = egkMessage["type"]?.jsonPrimitive?.content
			?: throw IllegalArgumentException("Web-Socket EGK message does not contain a type.")
		val messagePayload = egkMessage["payload"]?.jsonPrimitive?.content
			?: throw IllegalArgumentException("Web-Socket EGK message does not contain a payload.")

		val jsonPayload = String(Base64.decode(messagePayload))
		val jsonElement = Json.parseToJsonElement(jsonPayload)
		val typedJsonElement = JsonObject(jsonElement.jsonObject.toMutableMap().apply {
			put(Json.configuration.classDiscriminator, JsonPrimitive(messageType))
		})

		return CardEnvelope(
			cardSessionId,
			correlationId,
			cardLinkJsonFormatter.decodeFromJsonElement<CardLinkPayload>(typedJsonElement),
			messageType,
		)
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
	polymorphic(CardLinkPayload::class) {
		subclass(RegisterEgk::class)
		subclass(SendApdu::class)
		subclass(SendApduResponse::class)
		subclass(SendPhoneNumber::class)
		subclass(SendTan::class)
		subclass(ConfirmTan::class)
		subclass(ConfirmPhoneNumber::class)
	}
}

val cardLinkJsonFormatter = Json { serializersModule = module; classDiscriminatorMode = ClassDiscriminatorMode.NONE }

interface CardLinkPayload


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
)
