package org.openecard.addons.tr03124.xml

import nl.adaptivity.xmlutil.serialization.XML
import org.junit.jupiter.api.assertInstanceOf
import org.openecard.utils.common.hex
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class SerializationTest {
	val xml = XML

	@Test
	fun `process Result`() {
		val obj = Result("major", "minor", InternationalString("message", "en"))
		val str = XML.encodeToString(obj)
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
		assertEac1InputData(obj)
		val ser = xml.encodeToString(obj)
		assertEac1InputXml(ser, "/iso:EAC1InputType")
	}

	fun assertEac1InputData(obj: Eac1Input) {
		assertEquals("urn:oid:1.3.162.15480.3.0.14", obj.protocol)
		assertEquals(7, obj.certificate.size)
		assertContentEquals(hex("7F218201487F4E8201005F29010042"), obj.certificate[0].v.sliceArray(0 until 15))
		assertContentEquals(
			hex("7F218201B67F4E82016E5F290100420E44454356434165494430303130327F4982011D06"),
			obj.certificate[6].v.sliceArray(0 until 36),
		)
		assertContentEquals(
			hex("3082027C060A04007F00070301030101A10E0C0C442D547275737420476D6248A21913"),
			obj.certificateDescription.v.sliceArray(0 until 35),
		)
		assertContentEquals(
			hex("7F4C12060904007F00070301020253050000000004"),
			obj.requiredCHAT?.v,
		)
		assertContentEquals(
			hex("7F4C12060904007F00070301020253050000000000"),
			obj.optionalCHAT?.v,
		)
		assertContentEquals(
			hex("67177315060904007F00070301040253083230323530373232"),
			obj.authenticatedAuxiliaryData?.v,
		)
		assertTrue(obj.acceptedEIDType.isEmpty())
	}

	fun assertEac1InputXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)
		dom.assertXpathEquals(
			"$pathPrefix/@Protocol",
			"urn:oid:1.3.162.15480.3.0.14",
		)
		dom.assertXpathEquals(
			"count($pathPrefix/iso:Certificate)",
			"7",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:Certificate[1]/text()",
			"7F218201487F4E8201005F290100421044454456".lowercase(),
		) {
			it.trim().slice(0 until 40)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:CertificateDescription/text()",
			"3082027C060A04007F00070301030101A10E0C0C".lowercase(),
		) {
			it.trim().slice(0 until 40)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:RequiredCHAT/text()",
			"7F4C12060904007F00070301020253050000000004".lowercase(),
		) {
			it.trim()
		}
		dom.assertXpathEquals(
			"count($pathPrefix/iso:AcceptedEIDType)",
			"0",
		)
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
		assertEac1InputData(obj.data)
		val ser = xml.encodeToString(obj)
		assertEac1InputXml(ser, "/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
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
		assertEac1InputData(assertInstanceOf<DidAuthenticateRequest<Eac1Input>>(obj.body.content).data)
		val ser = xml.encodeToString(obj)
		assertEac1InputXml(ser, "/soap:Envelope/soap:Body/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
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

	fun parseXml(data: String): Document {
		val documentBuilderFactory = DocumentBuilderFactory.newInstance()
		documentBuilderFactory.isNamespaceAware = true
		val docBuilder = documentBuilderFactory.newDocumentBuilder()
		return docBuilder.parse(InputSource(StringReader(data)))
	}

	fun Node.assertXpathEquals(
		xpath: String,
		expected: String,
		block: ((String) -> String) = { it },
	) {
		val xpathObj = XPathFactory.newInstance().newXPath()
		xpathObj.namespaceContext = nsCtx
		val result = xpathObj.evaluate(xpath, this)?.let { block(it) }
		assertEquals(expected, result)
	}

	val nsCtx =
		object : NamespaceContext {
			private val nsMap = mutableMapOf<String, String>()

			init {
				addNs("iso", "urn:iso:std:iso-iec:24727:tech:schema")
				addNs("soap", "http://schemas.xmlsoap.org/soap/envelope/")
			}

			private fun addNs(
				prefix: String,
				ns: String,
			) {
				nsMap[prefix] = ns
			}

			override fun getNamespaceURI(prefix: String): String? = nsMap[prefix]

			override fun getPrefix(namespaceURI: String): String? =
				getPrefixes(namespaceURI).let {
					if (it.hasNext()) {
						return it.next()
					} else {
						null
					}
				}

			override fun getPrefixes(namespaceURI: String): Iterator<String> =
				nsMap
					.filterValues {
						it == namespaceURI
					}.values
					.iterator()
		}
}
