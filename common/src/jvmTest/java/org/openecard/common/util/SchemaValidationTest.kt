/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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
package org.openecard.common.util

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import jakarta.xml.bind.JAXBContext
import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.interfaces.DocumentValidatorException
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.testng.annotations.Test
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 *
 * @author Hans-Martin Haase
 */
class SchemaValidationTest {
	private val builder: DocumentBuilder
	private val validator: DocumentSchemaValidator

	init {
		// instantiate w3 stuff
		val tmpW3Factory = DocumentBuilderFactory.newInstance()
		tmpW3Factory.isNamespaceAware = true
		tmpW3Factory.isIgnoringComments = true
		tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

		builder = tmpW3Factory.newDocumentBuilder()

		validator = JAXPSchemaValidator.load("Management.xsd")
	}

	@Test
	fun testDIDAuthEac1InputOk() {
		val dataStream =
			resolveResourceAsStream(
				SchemaValidationTest::class.java,
				"DID_EAC1Input.xml",
			)
		val didAuth = builder.parse(dataStream)
		validator.validate(didAuth)
	}

	@Test
	fun testDIDAuthEac2InputOk() {
		val dataStream =
			resolveResourceAsStream(
				SchemaValidationTest::class.java,
				"DIDAuthenticate.xml",
			)
		val didAuth = builder.parse(dataStream)
		validator.validate(didAuth)
	}

	@Test(expectedExceptions = [DocumentValidatorException::class])
	fun testDIDAuthNok() {
		val jc = JAXBContext.newInstance(DIDAuthenticate::class.java)
		val unmarshaller = jc.createUnmarshaller()
		val dataStream =
			resolveResourceAsStream(
				SchemaValidationTest::class.java,
				"DIDAuthenticate.xml",
			)
		val didAuth2 = unmarshaller.unmarshal(dataStream) as DIDAuthenticate
		val authData = didAuth2.authenticationProtocolData
		val d = builder.newDocument()
		val sigElem = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature")
		sigElem.textContent = "1254786930AAD4A8"
		authData.any.add(sigElem)
		didAuth2.authenticationProtocolData = authData
		val target = builder.newDocument()
		jc.createMarshaller().marshal(didAuth2, target)
		validator.validate(target)
	}

	@Test(expectedExceptions = [DocumentValidatorException::class])
	fun testDIDAuthEac1InputNoCert() {
		val dataStream =
			resolveResourceAsStream(
				SchemaValidationTest::class.java,
				"DIDAuthenticate_EACInput1_nocert.xml",
			)
		val didAuth = builder.parse(dataStream)
		validator.validate(didAuth)
	}

	@Test
	fun testInitFrameworkOk() {
		val dataStream =
			resolveResourceAsStream(
				SchemaValidationTest::class.java,
				"InitializeFramework.xml",
			)
		val initFrame = builder.parse(dataStream)
		validator.validate(initFrame)
	}
}
