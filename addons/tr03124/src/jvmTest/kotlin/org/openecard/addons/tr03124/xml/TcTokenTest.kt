package org.openecard.addons.tr03124.xml

import org.junit.jupiter.api.assertInstanceOf
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.parseTcToken
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toBindingType
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.toSecurityProtocolType
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

		val token = tokenStr.parseTcToken()
		assertEquals("https://eid-server.example.de/entrypoint", token.serverAddress)
		assertEquals("1A2BB129", token.sessionIdentifier)
		assertEquals("https://service.example.de/loggedin?7eb39f62", token.refreshAddress)
		assertEquals("https://service.example.de/ComError?7eb39f62", token.communicationErrorAddress)
		assertEquals(TcTokenXml.BindingType.PAOS, token.binding?.toBindingType())
		assertEquals(TcTokenXml.SecurityProtocolType.TLS_PSK, token.securityProtocol?.toSecurityProtocolType())
		assertContentEquals(hex("4BC1A0B5"), assertInstanceOf<TcTokenXml.PskParams>(token.securityParameters).psk.v)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `parse TCToken comm error`() {
		val tokenStr =
			"""
			<TCTokenType>
			  <ServerAddress />
			  <SessionIdentifier />
			  <RefreshAddress />
			  <CommunicationErrorAddress>https://eservice-idp-test.secunet.de:444/communicationerror</CommunicationErrorAddress>
			  <Binding />
			  <PathSecurity-Protocol />
			  <PathSecurity-Parameters>
			    <PSK/>
			  </PathSecurity-Parameters>
			</TCTokenType>
			""".trimIndent()

		val token = tokenStr.parseTcToken()
		assertEquals("https://eservice-idp-test.secunet.de:444/communicationerror", token.communicationErrorAddress)
	}
}
