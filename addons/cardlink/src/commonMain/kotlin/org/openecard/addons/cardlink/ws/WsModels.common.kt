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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import java.util.*


const val REGISTER_EGK = "registerEGK"
const val TASK_LIST_ERROR = "receiveTasklistError"
const val SEND_APDU = "sendAPDU"
const val READY = "ready"
const val SEND_APDU_RESPONSE = "sendAPDUResponse"
/* Newly defined types */
const val REQUEST_SMS_TAN = "requestSmsTan"
const val CONFIRM_TAN = "confirmTan"
const val CONFIRM_TAN_RESPONSE = "confirmTanResponse"


@Serializable(with = EgkEnvelopeSerializer::class)
class EgkEnvelope(
	var cardSessionId: String,
	var correlationId: String?,
	var message: EgkMessage
)

object EgkEnvelopeSerializer : KSerializer<EgkEnvelope> {
	override val descriptor: SerialDescriptor = EgkEnvelope.serializer().descriptor

	override fun serialize(encoder: Encoder, value: EgkEnvelope) {
		val egkEnvelope = Json.encodeToJsonElement(value.message)
		val jsonArray = buildJsonArray {
			add(egkEnvelope)
			add(value.cardSessionId)
			value.correlationId?.let { add(it) }
		}
		encoder.encodeSerializableValue(JsonElement.serializer(), jsonArray)
	}

	override fun deserialize(decoder: Decoder): EgkEnvelope {
		val websocketMessage = decoder.decodeSerializableValue(JsonElement.serializer())

		val egkEnvelope = websocketMessage.jsonArray.getOrNull(0)?.jsonObject
			?: throw IllegalArgumentException("Web-Socket message does not contain an Egk message.")
		val cardSessionId = websocketMessage.jsonArray.getOrNull(1)?.jsonPrimitive?.content
			?: throw IllegalArgumentException("Web-Socket message does not contain a card session ID.")
		val correlationId = websocketMessage.jsonArray.getOrNull(2)?.jsonPrimitive?.content

		return EgkEnvelope(
			cardSessionId,
			correlationId,
			cardLinkJsonFormatter.decodeFromJsonElement<EgkMessage>(egkEnvelope)
		)
	}
}


@Serializable
class EgkMessage {

	private var type: String
	/* Ready Message does not contain a payload */
	private var payload: String?

	constructor(type: String) {
		this.type = type
		this.payload = null
	}

	constructor(type: String, payload: EgkPayload) {
		val jsonPayload = cardLinkJsonFormatter.encodeToString(payload)
		val base64EncodedPayload = Base64.getEncoder().encodeToString(jsonPayload.encodeToByteArray())

		this.type = type
		this.payload = base64EncodedPayload
	}

	fun getEgkPayload() : EgkPayload {
		if (payload != null) {
			val jsonPayload = String(Base64.getDecoder().decode(payload))
			val jsonElement = Json.parseToJsonElement(jsonPayload)
			val typedJsonElement = JsonObject(jsonElement.jsonObject.toMutableMap().apply {
				put(Json.configuration.classDiscriminator, JsonPrimitive(type))
			})
			return cardLinkJsonFormatter.decodeFromJsonElement<EgkPayload>(typedJsonElement)
		} else {
			throw IllegalArgumentException("Envelope Message does not have a payload.")
		}
	}
}

val module = SerializersModule {
	polymorphic(EgkPayload::class) {
		subclass(RegisterEgk::class)
		subclass(SendApdu::class)
		subclass(SendApduResponse::class)
		subclass(TasklistError::class)
		subclass(SendPhoneNumber::class)
		subclass(SendTan::class)
		subclass(ConfirmTan::class)
	}
}

val cardLinkJsonFormatter = Json { serializersModule = module; classDiscriminatorMode = ClassDiscriminatorMode.NONE }

interface EgkPayload

@Serializable
@SerialName(REGISTER_EGK)
data class RegisterEgk(
	val cardSessionId: String,
	val gdo: String,
	val cardVersion: String,
	val x509AuthRSA: String? = null,
	val x509AuthECC: String,
	val cvcAuth: String,
	val cvcCA: String,
	val atr: String,
) : EgkPayload

@Serializable
@SerialName(SEND_APDU)
data class SendApdu(
	val cardSessionId: String,
	val apdu: String,
) : EgkPayload

@Serializable
@SerialName(SEND_APDU_RESPONSE)
data class SendApduResponse(
	val cardSessionId: String,
	val response: String,
) : EgkPayload

@Serializable
@SerialName(TASK_LIST_ERROR)
data class TasklistError(
	val cardSessionId: String,
	val status: Int,
	val tistatus: String? = null,
	val rootcause: String? = null,
	val errormessage: String? = null,
) : EgkPayload

@Serializable
@SerialName(REQUEST_SMS_TAN)
data class SendPhoneNumber(val phoneNumber: String) : EgkPayload

@Serializable
@SerialName(CONFIRM_TAN)
data class SendTan(val tan: String) : EgkPayload

@Serializable
@SerialName(CONFIRM_TAN_RESPONSE)
data class ConfirmTan(
	var minor: MinorResultCode?,
	var errorMessage: String?,
) : EgkPayload

@Serializable
enum class MinorResultCode {
	NUMBER_FROM_WRONG_COUNTRY,
	TAN_EXPIRED,
	TAN_INCORRECT,
	TAN_RETRY_LIMIT_EXCEEDED,
	UNKNOWN_ERROR,
}
