package org.openecard.addons.tr03124.xml

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class SerializationTest {
	val xml = eacXml

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
		assertEquals(7, obj.certificates.size)
		assertContentEquals(hex("7F218201487F4E8201005F29010042"), obj.certificates[0].v.sliceArray(0 until 15))
		assertContentEquals(
			hex("7F218201B67F4E82016E5F290100420E44454356434165494430303130327F4982011D06"),
			obj.certificates[6].v.sliceArray(0 until 36),
		)
		assertContentEquals(
			hex("3082027C060A04007F00070301030101A10E0C0C442D547275737420476D6248A21913"),
			obj.certificateDescription.v.sliceArray(0 until 35),
		)
		assertContentEquals(
			hex("7F4C12060904007F00070301020253050000000004"),
			obj.requiredChat?.v,
		)
		assertContentEquals(
			hex("7F4C12060904007F00070301020253050000000000"),
			obj.optionalChat?.v,
		)
		assertContentEquals(
			hex("67177315060904007F00070301040253083230323530373232"),
			obj.authenticatedAuxiliaryData?.v,
		)
		assertTrue(obj.acceptedEidType.isEmpty())
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
		assertEac1OutputData(obj)
		val ser = xml.encodeToString(obj)
		assertEac1OutputXml(ser, "/iso:EAC1OutputType")
	}

	fun assertEac1OutputData(obj: Eac1Output) {
		assertEquals("urn:oid:1.3.162.15480.3.0.14", obj.protocol)
		assertEquals(2, obj.certificationAuthorityReference.size)
		assertContentEquals(
			hex("7F4C12060904007F00070301020253050000000004"),
			obj.certificateHolderAuthorizationTemplate?.v,
		)
		assertEquals(
			"DECVCAeID00107",
			obj.certificationAuthorityReference[0].trim(),
		)
		assertEquals(
			"DECVCAeID00106",
			obj.certificationAuthorityReference[1].trim(),
		)
		assertContentEquals(
			hex("3181C1300D060804007F00070202020201023012060A04007F00070202030202020102"),
			obj.efCardAccess.v.sliceArray(0 until 35),
		)
		assertContentEquals(
			hex("360EE7BE88830E04170E578013DFC2CE953D36D133B460EF0E368E5E8C879CEE"),
			obj.idPICC.v.sliceArray(0 until 32),
		)
		assertContentEquals(
			hex("E93CD9503D1FFFA3"),
			obj.challenge.v.sliceArray(0 until 8),
		)
	}

	fun assertEac1OutputXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)
		dom.assertXpathEquals(
			"$pathPrefix/@Protocol",
			"urn:oid:1.3.162.15480.3.0.14",
		)
		dom.assertXpathEquals(
			"count($pathPrefix/iso:CertificationAuthorityReference)",
			"2",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:CertificationAuthorityReference[1]/text()",
			"DECVCAeID00107",
		) {
			it.trim()
		}

		dom.assertXpathEquals(
			"$pathPrefix/iso:CertificateHolderAuthorizationTemplate/text()",
			"7F4C12060904007F00070301020253050000000004".lowercase(),
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:EFCardAccess/text()",
			"3181C1300D060804007F00070202020201023012060A04007F".lowercase(),
		) {
			it.trim().slice(0 until 50)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:IDPICC/text()",
			"360EE7BE88830E04170E578013DFC2CE953D36D133B460EF0E368E5E8C879CEE".lowercase(),
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:Challenge/text()",
			"E93CD9503D1FFFA3".lowercase(),
		)
	}

	@Test
	fun `process Eac2Input solo`() {
		val original = readXml("Eac2Input_solo")
		val obj = xml.decodeFromString<Eac2Input>(original)
		assertEac2InputData(obj)
		val ser = xml.encodeToString(obj)
		assertEac2InputXml(ser, "/iso:EAC2InputType")
	}

	fun assertEac2InputData(obj: Eac2Input) {
		assertEquals("urn:oid:1.3.162.15480.3.0.14", obj.protocol)
		assertEquals(1, obj.certificates.size)
		assertContentEquals(
			hex("7F2181E77F4E81A05F290100420E44"),
			obj.certificates[0].v.sliceArray(0 until 15),
		)
		assertContentEquals(
			hex("048C74138C3CFEF38DDE206A5FF4244E79741E20"),
			obj.ephemeralPublicKey.v.sliceArray(0 until 20),
		)
		assertContentEquals(
			hex("84095FC69630B5937FB3C1FF4DE17414214ED48D3A72EFB36E"),
			obj.signature?.v?.sliceArray(0 until 25),
		)
	}

	fun assertEac2InputXml(
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
			"1",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:Certificate[1]/text()",
			"7F2181E77F4E81A05F290100420E444543564341".lowercase(),
		) {
			it.trim().slice(0 until 40)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:EphemeralPublicKey/text()",
			"048C74138C3CFEF38DDE206A5FF424".lowercase(),
		) {
			it.trim().slice(0 until 30)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:Signature/text()",
			"84095FC69630B5937FB3C1FF4DE17414214".lowercase(),
		) {
			it.trim().slice(0 until 35)
		}
	}

	@Test
	fun `process Eac2Output solo`() {
		val original = readXml("Eac2Output_solo")
		val obj = xml.decodeFromString<Eac2Output>(original)
		assertEac2OutputData(obj)
		val ser = xml.encodeToString(obj)
		assertEac2OutputXml(ser, "/iso:EAC2OutputType")
	}

	fun assertEac2OutputData(obj: Eac2Output) {
		assertEquals("urn:oid:1.3.162.15480.3.0.14", obj.protocol)
		assertContentEquals(
			hex("3082075206092A864886F70D010702"),
			obj.efCardSecurity?.v?.sliceArray(0 until 15),
		)
		assertContentEquals(
			hex("C22F01A10CED5BD3"),
			obj.authenticationToken?.v,
		)
		assertContentEquals(
			hex("5AC24DE83C7D10B2"),
			obj.nonce?.v,
		)
		assertTrue(obj.challenge?.v.isNullOrEmpty())
	}

	fun assertEac2OutputXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)
		dom.assertXpathEquals(
			"$pathPrefix/@Protocol",
			"urn:oid:1.3.162.15480.3.0.14",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:EFCardSecurity/text()",
			"3082075206092A864886F70D010702A082074330".lowercase(),
		) {
			it.trim().slice(0 until 40)
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:AuthenticationToken/text()",
			"C22F01A10CED5BD3".lowercase(),
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:Nonce/text()",
			"5AC24DE83C7D10B2".lowercase(),
		)
	}

	// @Ignore
	@Test
	fun `process Eac1Input`() {
		val original = readXml("Eac1Input")
		val obj = xml.decodeFromString<DidAuthenticateRequest>(original)
		assertEac1InputData(assertInstanceOf<Eac1Input>(obj.data))
		val ser = xml.encodeToString(obj)
		assertEac1InputXml(ser, "/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac1Output`() {
		val original = readXml("Eac1Output")
		val obj = xml.decodeFromString<DidAuthenticateResponse>(original)
		assertEac1OutputData(assertInstanceOf<Eac1Output>(obj.data))
		val ser = xml.encodeToString(obj)
		assertEac1OutputXml(ser, "/iso:DIDAuthenticateResponse/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac2Input`() {
		val original = readXml("Eac2Input")
		val obj = xml.decodeFromString<DidAuthenticateRequest>(original)
		assertEac2InputData(assertInstanceOf<Eac2Input>(obj.data))
		val ser = xml.encodeToString(obj)
		assertEac2InputXml(ser, "/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac2Output`() {
		val original = readXml("Eac2Output")
		val obj = xml.decodeFromString<DidAuthenticateResponse>(original)
		assertEac2OutputData(assertInstanceOf<Eac2Output>(obj.data))
		val ser = xml.encodeToString(obj)
		assertEac2OutputXml(ser, "/iso:DIDAuthenticateResponse/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process StartPaos`() {
		val original = readXml("StartPaos")
		val obj = xml.decodeFromString<StartPaos>(original)
		assertStartPaosData(obj)
		val ser = xml.encodeToString(obj)
		assertStartPaosXml(ser, "/iso:StartPAOS")
	}

	fun assertStartPaosData(obj: StartPaos) {
		assertTrue(obj.requestId.isNullOrEmpty())
		assertEquals(
			"15311BF20D4F646874F2B4724EF8CE310E8535DF6ED72FFBBD8BF3B35BCBEA65",
			obj.sessionIdentifier.trim(),
		)
		assertContentEquals(
			hex("B7D53A5A34C2F99F9CE380700501C22A"),
			obj.connectionHandle?.contextHandle?.v,
		)
		assertContentEquals(
			hex("340C46B396F7D392F3797E14B669A97F"),
			obj.connectionHandle?.slotHandle?.v,
		)
		assertEquals(
			"Open eCard App",
			obj.userAgent.name,
		)
		assertEquals(
			2,
			obj.userAgent.versionMajor,
		)
		assertEquals(
			3,
			obj.userAgent.versionMinor,
		)
		assertEquals(
			13,
			obj.userAgent.versionSubminor,
		)
		assertEquals(
			1,
			obj.supportedAPIVersions.first().major,
		)
		assertEquals(
			1,
			obj.supportedAPIVersions.first().minor,
		)
		assertEquals(
			5,
			obj.supportedAPIVersions.first().subminor,
		)
		assertEquals(
			"urn:oid:1.3.162.15480.3.0.14.2",
			obj.supportedDIDProtocols[0],
		)
	}

	fun assertStartPaosXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)
		dom.assertXpathEquals(
			"$pathPrefix/iso:SupportedDIDProtocols[1]",
			"urn:oid:1.3.162.15480.3.0.14.2",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:SessionIdentifier/text()",
			"15311BF20D4F646874F2B4724EF8CE310E8535DF6ED72FFBBD8BF3B35BCBEA65",
		) {
			it.trim()
		}
		dom.assertXpathEquals(
			"$pathPrefix/iso:ConnectionHandle/iso:ContextHandle/text()",
			"B7D53A5A34C2F99F9CE380700501C22A".lowercase(),
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:ConnectionHandle/iso:SlotHandle/text()",
			"340C46B396F7D392F3797E14B669A97F".lowercase(),
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:UserAgent[1]/iso:Name/text()",
			"Open eCard App",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:SupportedAPIVersions[1]/iso:Major/text()",
			"1",
		)
		dom.assertXpathEquals(
			"$pathPrefix/@Profile",
			"http://www.bsi.bund.de/ecard/api/1.1",
		)
		dom.assertXpathEquals(
			"count($pathPrefix/iso:SupportedDIDProtocols)",
			"4",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:SupportedDIDProtocols[1]/text()",
			"urn:oid:1.3.162.15480.3.0.14.2",
		)
	}

	@Test
	fun `process StartPaosResponse`() {
		val original = readXml("StartPaosResponse")
		val obj = xml.decodeFromString<StartPaosResponse>(original)
		assertStartPaosResponseData(obj)
		val ser = xml.encodeToString(obj)
		assertStartPaosResponseXml(ser, "/iso:StartPAOSResponse")
	}

	fun assertStartPaosResponseData(obj: StartPaosResponse) {
		assertTrue(obj.requestId.isNullOrEmpty())
		assertEquals(ECardConstants.Profile.ECARD_1_1, obj.profile)
		assertEquals(
			"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok",
			obj.result.major,
		)
	}

	fun assertStartPaosResponseXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)

		dom.assertXpathEquals(
			"$pathPrefix/dss:Result/dss:ResultMajor/text()",
			"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok",
		)
	}

	@Test
	fun `process TransmitRequest`() {
		val original = readXml("TransmitRequest")
		val obj = xml.decodeFromString<TransmitRequest>(original)
		assertTransmitRequestData(obj)
		val ser = xml.encodeToString(obj)
		assertTransmitRequestXml(ser, "/iso:Transmit")
	}

	fun assertTransmitRequestData(obj: TransmitRequest) {
		assertContentEquals(
			hex("340C46B396F7D392F3797E14B669A97F"),
			obj.slotHandle.v,
		)
		assertEquals(6, obj.inputAPDUInfo.size)
		assertContentEquals(
			hex("0CA4040C1D8711015F899DA6D69E628D44C8288E1A79ABD78E"),
			obj.inputAPDUInfo
				.first()
				.inputAPDU.v
				.sliceArray(0 until 25),
		)
	}

	fun assertTransmitRequestXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)

		dom.assertXpathEquals(
			"count($pathPrefix/iso:InputAPDUInfo)",
			"6",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:InputAPDUInfo[1]/iso:InputAPDU/text()",
			"0CA4040C1D8711015F899DA6D69E628D44C8288E1A79ABD78E".lowercase(),
		) {
			it.trim().slice(0 until 50)
		}
	}

	@Test
	fun `process TransmitResponse`() {
		val original = readXml("TransmitResponse")
		val obj = xml.decodeFromString<TransmitResponse>(original)
		assertTransmitResponseData(obj)
		val ser = xml.encodeToString(obj)
		assertTransmitResponseXml(ser, "/iso:TransmitResponse")
	}

	fun assertTransmitResponseData(obj: TransmitResponse) {
		assertEquals("http://www.bsi.bund.de/ecard/api/1.1", obj.profile)
		assertTrue(obj.requestId.isNullOrEmpty())
		assertEquals(
			"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok",
			obj.result.major,
		)
		assertEquals(6, obj.outputAPDU.size)
		assertContentEquals(
			hex("990290008E0801FDBBBFC8A3467A9000"),
			obj.outputAPDU[0].v,
		)
	}

	fun assertTransmitResponseXml(
		data: String,
		pathPrefix: String,
	) {
		val dom = parseXml(data)

		dom.assertXpathEquals(
			"$pathPrefix/@Profile",
			"http://www.bsi.bund.de/ecard/api/1.1",
		)
		dom.assertXpathEquals(
			"$pathPrefix/dss:Result/dss:ResultMajor/text()",
			"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok",
		)

		dom.assertXpathEquals(
			"count($pathPrefix/iso:OutputAPDU)",
			"6",
		)
		dom.assertXpathEquals(
			"$pathPrefix/iso:OutputAPDU[1]/text()",
			"990290008E0801FDBBBFC8A3467A9000".lowercase(),
		)
	}

	@Test
	fun `process Eac1Input SOAP`() {
		val original = readSoap("Eac1Input")
		val obj = xml.decodeFromString<Envelope>(original)
		assertEac1InputData(assertInstanceOf(assertInstanceOf<DidAuthenticateRequest>(obj.body.content).data))
		val ser = xml.encodeToString(obj)
		assertEac1InputXml(ser, "/soap:Envelope/soap:Body/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac1Output SOAP`() {
		val original = readSoap("Eac1Output")
		val obj = xml.decodeFromString<Envelope>(original)
		assertEac1OutputData(assertInstanceOf(assertInstanceOf<DidAuthenticateResponse>(obj.body.content).data))
		val ser = xml.encodeToString(obj)
		assertEac1OutputXml(ser, "/soap:Envelope/soap:Body/iso:DIDAuthenticateResponse/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac2Input SOAP`() {
		val original = readSoap("Eac2Input")
		val obj = xml.decodeFromString<Envelope>(original)
		assertEac2InputData(assertInstanceOf(assertInstanceOf<DidAuthenticateRequest>(obj.body.content).data))
		val ser = xml.encodeToString(obj)
		assertEac2InputXml(ser, "/soap:Envelope/soap:Body/iso:DIDAuthenticate/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process Eac2Output SOAP`() {
		val original = readSoap("Eac2Output")
		val obj = xml.decodeFromString<Envelope>(original)
		assertEac2OutputData(assertInstanceOf(assertInstanceOf<DidAuthenticateResponse>(obj.body.content).data))
		val ser = xml.encodeToString(obj)
		assertEac2OutputXml(ser, "/soap:Envelope/soap:Body/iso:DIDAuthenticateResponse/iso:AuthenticationProtocolData")
	}

	@Test
	fun `process StartPaos SOAP`() {
		val original = readSoap("StartPaos")
		val obj = xml.decodeFromString<Envelope>(original)
		assertStartPaosData(assertInstanceOf(assertInstanceOf<StartPaos>(obj.body.content)))
		val ser = xml.encodeToString(obj)
		assertStartPaosXml(ser, "/soap:Envelope/soap:Body/iso:StartPAOS")
	}

	@Test
	fun `process StartPaosResponse SOAP`() {
		val original = readSoap("StartPaosResponse")
		val obj = xml.decodeFromString<Envelope>(original)
		assertStartPaosResponseData(assertInstanceOf(assertInstanceOf<StartPaosResponse>(obj.body.content)))
		val ser = xml.encodeToString(obj)
		assertStartPaosResponseXml(ser, "/soap:Envelope/soap:Body/iso:StartPAOSResponse")
	}

	@Test
	fun `process TransmitRequest SOAP`() {
		val original = readSoap("TransmitRequest")
		val obj = xml.decodeFromString<Envelope>(original)
		assertTransmitRequestData(assertInstanceOf(assertInstanceOf<TransmitRequest>(obj.body.content)))
		val ser = xml.encodeToString(obj)
		assertTransmitRequestXml(ser, "/soap:Envelope/soap:Body/iso:Transmit")
	}

	@Test
	fun `process TransmitResponse SOAP`() {
		val original = readSoap("TransmitResponse")
		val obj = xml.decodeFromString<Envelope>(original)
		assertTransmitResponseData(assertInstanceOf(assertInstanceOf<TransmitResponse>(obj.body.content)))
		val ser = xml.encodeToString(obj)
		assertTransmitResponseXml(ser, "/soap:Envelope/soap:Body/iso:TransmitResponse")
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
				addNs("dss", "urn:oasis:names:tc:dss:1.0:core:schema")
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
