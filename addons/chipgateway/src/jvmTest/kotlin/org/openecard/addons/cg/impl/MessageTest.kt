/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule
import org.openecard.ws.chipgateway.HelloRequestType
import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException

/**
 *
 * @author Tobias Wich
 */
class MessageTest {
	@Test
	@Throws(JsonProcessingException::class, IOException::class)
	fun testHelloRequest() {
		val mapper =
			ObjectMapper().apply {
				registerModule(JakartaXmlBindAnnotationModule())
			}

		val req =
			HelloRequestType().apply {
				sessionIdentifier = "1234abcd"
				challenge = byteArrayOf(0, 1, 2, 3)
				version = "1.2.3"
			}

		val result = mapper.writeValueAsString(req)
		val req1 = mapper.readValue(result, HelloRequestType::class.java)

		// load reference
		val inputRef =
			"""
			{
				"Challenge" : "00010203",
				 "Version" : "1.2.3",
				 "SessionIdentifier" : "1234abcd"
			}
			""".trimIndent()

		val reference = mapper.readValue(inputRef, HelloRequestType::class.java)

		Assert.assertEquals(reference.sessionIdentifier, req1.sessionIdentifier)
		Assert.assertEquals(reference.challenge, req1.challenge)
		Assert.assertEquals(reference.version, req1.version)
	}
}
