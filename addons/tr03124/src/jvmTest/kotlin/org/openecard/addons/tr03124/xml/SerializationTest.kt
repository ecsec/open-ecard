package org.openecard.addons.tr03124.xml

import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {
	val xml = XML

	@Test
	fun `process Result`() {
		val obj = Result("major", "minor", InternationalString("message", "en"))
		val str = XML.encodeToString(obj)
		println(str)
		assertEquals(
			"""
			<Result xmlns="urn:oasis:names:tc:dss:1.0:core:schema"><ResultMajor>major</ResultMajor><ResultMinor>minor</ResultMinor><ResultMessage xml:lang="en">message</ResultMessage></Result>
			""".trim(),
			str,
		)
	}

	@Test
	fun `process Eac1Input solo`() {
		val original = readXml("Eac1Input_solo")
		val obj = xml.decodeFromString<Eac1Input>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process Eac1Output solo`() {
		val original = readXml("Eac1Output_solo")
		val obj = xml.decodeFromString<Eac1Output>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process Eac2Input solo`() {
		val original = readXml("Eac2Input_solo")
		val obj = xml.decodeFromString<Eac2Input>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process Eac2Output solo`() {
		val original = readXml("Eac2Output_solo")
		val obj = xml.decodeFromString<Eac2Output>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac1Input`() {
		val original = readXml("Eac1Input")
		val obj = xml.decodeFromString<DidAuthenticateRequest<Eac1Input>>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac1Output`() {
		val original = readXml("Eac1Output")
		val obj = xml.decodeFromString<DidAuthenticateResponse<Eac1Output>>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac2Input`() {
		val original = readXml("Eac2Input")
		val obj = xml.decodeFromString<DidAuthenticateRequest<Eac2Input>>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac2Output`() {
		val original = readXml("Eac2Output")
		val obj = xml.decodeFromString<DidAuthenticateResponse<Eac2Output>>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process StartPaos`() {
		val original = readXml("StartPaos")
		val obj = xml.decodeFromString<StartPaos>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process StartPaosResponse`() {
		val original = readXml("StartPaosResponse")
		val obj = xml.decodeFromString<StartPaosResponse>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process TransmitRequest`() {
		val original = readXml("TransmitRequest")
		val obj = xml.decodeFromString<TransmitRequest>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Test
	fun `process TransmitResponse`() {
		val original = readXml("TransmitResponse")
		val obj = xml.decodeFromString<TransmitResponse>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac1Input SOAP`() {
		val original = readSoap("Eac1Input")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac1Output SOAP`() {
		val original = readSoap("Eac1Output")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac2Input SOAP`() {
		val original = readSoap("Eac2Input")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process Eac2Output SOAP`() {
		val original = readSoap("Eac2Output")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process StartPaos SOAP`() {
		val original = readSoap("StartPaos")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process StartPaosResponse SOAP`() {
		val original = readSoap("StartPaosResponse")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process TransmitRequest SOAP`() {
		val original = readSoap("TransmitRequest")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	@Ignore
	@Test
	fun `process TransmitResponse SOAP`() {
		val original = readSoap("TransmitResponse")
		val obj = xml.decodeFromString<Envelope>(original)
		// TODO: add assertions
		val ser = xml.encodeToString(obj)
		// TODO: verify the serialized value
	}

	fun readXml(file: String): String = readResource("/xml/$file.xml")

	fun readSoap(file: String): String = readResource("/xml/soap/$file.xml")

	fun readResource(file: String): String =
		checkNotNull(javaClass.getResourceAsStream(file))
			.use {
				it.readAllBytes()
			}.decodeToString()
}
