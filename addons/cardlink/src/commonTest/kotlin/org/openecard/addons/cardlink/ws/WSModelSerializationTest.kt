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
 */

package org.openecard.addons.cardlink.ws

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*


private val logger = KotlinLogging.logger {}

/**
 * @author Mike Prechtl
 */
class WSModelSerializationTest {

	@OptIn(ExperimentalSerializationApi::class)
	@DataProvider(name = "egkPayloads")
	fun egkPayloads(): Array<Array<Any?>> {
		return arrayOf(
			arrayOf(
			 	RegisterEgk(
					cardSessionId = "foo",
					atr = "atr",
					gdo = "gdo",
					cvcCA = "cvcCA",
					cvcAuth = "cvcAuth",
					cardVersion = "cardVersion",
					x509AuthECC = "x509"
				),
				RegisterEgk.serializer().descriptor.serialName
			),
			arrayOf(
				SendApdu(
					cardSessionId = "foo",
					apdu = "apdu"
				),
				SendApdu.serializer().descriptor.serialName
			),
			arrayOf(
				SendApduResponse(
					cardSessionId = "foo",
					response = "resp"
				),
				SendApduResponse.serializer().descriptor.serialName
			),
			arrayOf(
				TasklistError(
					cardSessionId = "foo",
					status = 400,
					errormessage = "error-message"
				),
				TasklistError.serializer().descriptor.serialName
			),
			arrayOf(
				SendPhoneNumber(
					phoneNumber = "+49 123456"
				),
				SendPhoneNumber.serializer().descriptor.serialName
			),
			arrayOf(
				SendTan(
					tan = "123456"
				),
				SendTan.serializer().descriptor.serialName
			),
			arrayOf(
				ConfirmTan(
					null,
					null
				),
				ConfirmTan.serializer().descriptor.serialName
			),
			arrayOf(
				ConfirmTan(
					minor = MinorResultCode.TAN_EXPIRED,
					errorMessage = "Tan expired."
				),
				ConfirmTan.serializer().descriptor.serialName
			)
		)
	}

	@Test(dataProvider = "egkPayloads")
	fun testEgkPayloadSerialization(egkPayload: EgkPayload, serialName: String) {
		val jsonString = cardLinkJsonFormatter.encodeToString(egkPayload)
		logger.info { jsonString }
	}

	@Test(dataProvider = "egkPayloads")
	fun testEgkMessageSerialization(egkPayload: EgkPayload, serialName: String) {
		val egkMessage = EgkMessage(
			type = serialName,
			egkPayload
		)
		val jsonString = cardLinkJsonFormatter.encodeToString(egkMessage)
		// Try to get an instance of EGKPayload from the Base64-encoded payload
		egkMessage.getEgkPayload()

		logger.info { jsonString }
	}

	@Test(dataProvider = "egkPayloads")
	fun testEgkEnvelopeSerialization(egkPayload: EgkPayload, serialName: String) {
		val egkMessage = EgkMessage(
			type = serialName,
			egkPayload
		)
		val egkEnvelope = EgkEnvelope(
			cardSessionId = "foobar",
			correlationId = UUID.randomUUID().toString(),
			message = egkMessage
		)
		val jsonString = cardLinkJsonFormatter.encodeToString(egkEnvelope)
		logger.info { jsonString }
	}
}
