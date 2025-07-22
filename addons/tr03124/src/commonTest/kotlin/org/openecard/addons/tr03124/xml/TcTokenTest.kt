package org.openecard.addons.tr03124

import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.assertInstanceOf
import org.openecard.addons.tr03124.TcToken.Companion.toTcToken
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TcTokenTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `parse TCToken ordered`() {
		val tokenStr =
			"""
			<TCTokenType>
			  <SessionIdentifier>1A2BB129</SessionIdentifier>
			  <ServerAddress>https://eid-server.example.de/entrypoint</ServerAddress>
			  <RefreshAddress>https://service.example.de/loggedin?7eb39f62</RefreshAddress>
			  <CommunicationErrorAddress>https://service.example.de/ComError?7eb39f62</CommunicationErrorAddress>
			  <Binding>urn:liberty:paos:2006-08</Binding>
			  <PathSecurity-Protocol>urn:ietf:rfc:4279</PathSecurity-Protocol>
			  <PathSecurity-Parameters>
			    <PSK>4BC1A0B5</PSK>
			  </PathSecurity-Parameters>
			</TCTokenType>
			""".trimIndent()

		val token: TcToken = tokenStr.toTcToken()
		assertEquals("https://eid-server.example.de/entrypoint", token.serverAddress)
		assertEquals("1A2BB129", token.sessionIdentifier)
		assertEquals("https://service.example.de/loggedin?7eb39f62", token.refreshAddress)
		assertEquals("https://service.example.de/ComError?7eb39f62", token.communicationErrorAddress)
		assertEquals(TcToken.BindingType.PAOS, token.binding)
		assertEquals(TcToken.SecurityProtocolType.TLS_PSK, token.securityProtocol)
		assertContentEquals(hex("4BC1A0B5"), assertInstanceOf<TcToken.PskParams>(token.securityParameters).psk)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `serialize TCToken`() {
		val data =
			TcToken.Xml.encodeToString(
				TcToken(
					"server",
					"session",
					"refresh",
					null,
					TcToken.BindingType.PAOS,
					TcToken.SecurityProtocolType.TLS_PSK,
					TcToken.PskParams(hex("010203")),
				),
			)
		println(data)
		val token = data.toTcToken()
		assertContentEquals(hex("010203"), assertInstanceOf<TcToken.PskParams>(token.securityParameters).psk)
	}
}
