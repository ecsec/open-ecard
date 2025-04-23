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

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.testng.Assert
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
	fun egkPayloads(): Array<Array<Any?>> =
		arrayOf(
			arrayOf(
				RegisterEgk(
					cardSessionId = "foo",
					atr = "atr".encodeToByteArray(),
					gdo = "gdo".encodeToByteArray(),
					cvcCA = "cvcCA".encodeToByteArray(),
					cvcAuth = "cvcAuth".encodeToByteArray(),
					cardVersion = "cardVersion".encodeToByteArray(),
					x509AuthECC = "x509".encodeToByteArray(),
				),
				RegisterEgk.serializer().descriptor.serialName,
			),
			arrayOf(
				SendApdu(
					cardSessionId = "foo",
					apdu = "apdu".encodeToByteArray(),
				),
				SendApdu.serializer().descriptor.serialName,
			),
			arrayOf(
				SendApduResponse(
					cardSessionId = "foo",
					response = "resp".encodeToByteArray(),
				),
				SendApduResponse.serializer().descriptor.serialName,
			),
			arrayOf(
				SendPhoneNumber(
					phoneNumber = "+49 123456",
				),
				SendPhoneNumber.serializer().descriptor.serialName,
			),
			arrayOf(
				SendTan(
					smsCode = "123456",
					tan = "123456",
				),
				SendTan.serializer().descriptor.serialName,
			),
			arrayOf(
				ConfirmTan(
					resultCode = ResultCode.SUCCESS,
					errorMessage = null,
				),
				ConfirmTan.serializer().descriptor.serialName,
			),
			arrayOf(
				ConfirmTan(
					resultCode = ResultCode.TAN_EXPIRED,
					errorMessage = "Tan expired.",
				),
				ConfirmTan.serializer().descriptor.serialName,
			),
			arrayOf(
				ConfirmPhoneNumber(
					resultCode = ResultCode.SUCCESS,
					errorMessage = null,
				),
				ConfirmPhoneNumber.serializer().descriptor.serialName,
			),
		)

	@Test(dataProvider = "egkPayloads")
	fun testEgkPayloadSerialization(
		egkPayload: CardLinkPayload,
		serialName: String,
	) {
		val jsonString = cardLinkJsonFormatter.encodeToString(egkPayload)
		logger.info { jsonString }
	}

	@Test(dataProvider = "egkPayloads")
	fun testEgkEnvelopeSerialization(
		egkPayload: CardLinkPayload,
		serialName: String,
	) {
		val egkEnvelope =
			GematikEnvelope(
				cardSessionId = "foobar",
				correlationId = UUID.randomUUID().toString(),
				payload = egkPayload,
			)
		val jsonString = cardLinkJsonFormatter.encodeToString(egkEnvelope)
		logger.info { jsonString }

		// we will deserialize the string back to the class
		val egkEnvelopeDecoded = cardLinkJsonFormatter.decodeFromString<GematikEnvelope>(jsonString)
		Assert.assertEquals(egkEnvelopeDecoded.payload?.javaClass?.name, egkPayload.javaClass.name)
	}
}
