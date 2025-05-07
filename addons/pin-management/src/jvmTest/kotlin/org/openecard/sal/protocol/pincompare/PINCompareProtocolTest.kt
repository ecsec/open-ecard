/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
package org.openecard.sal.protocol.pincompare

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.DIDGet
import iso.std.iso_iec._24727.tech.schema.DIDScopeType
import iso.std.iso_iec._24727.tech.schema.Encipher
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType
import org.mockito.Mockito
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.common.anytype.pin.PINCompareMarkerType
import org.openecard.common.interfaces.CIFProvider
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.gui.UserConsent
import org.openecard.ifd.scio.IFD
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.sal.TinySAL
import org.openecard.transport.dispatcher.MessageDispatcher
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.jvm.java

/**
 *
 * @author Dirk Petrautzki
 */
class PINCompareProtocolTest {
	companion object {
		private const val TESTS_ENABLED = false
	}

	private lateinit var env: ClientEnv
	private lateinit var instance: TinySAL
	var appIdentifierRoot: ByteArray = StringUtils.toByteArray("D2760001448000")
	var appIdentifierEsign: ByteArray = StringUtils.toByteArray("A000000167455349474E")

	@BeforeMethod
	@Throws(Exception::class)
	fun setUp() {
		val uc = Mockito.mock<UserConsent?>(UserConsent::class.java)

		env = ClientEnv()
		env.gui = uc
		val d: Dispatcher = MessageDispatcher(env)
		env.dispatcher = d
		val ifd = IFD()
		ifd.setEnvironment(env)
		env.ifd = ifd

		val ecr = env.ifd!!.establishContext(EstablishContext())
		val cr = CardRecognitionImpl(env)
		val listIFDs = ListIFDs()
		val cp =
			object : CIFProvider {
				override fun getCardInfo(
					type: ConnectionHandleType?,
					cardType: String,
				): CardInfoType? = cr.getCardInfo(cardType)

				override fun needsRecognition(atr: ByteArray): Boolean = true

				@Throws(RuntimeException::class)
				override fun getCardInfo(cardType: String): CardInfoType? = cr.getCardInfo(cardType)

				override fun getCardImage(cardType: String): InputStream? = cr.getCardImage(cardType)
			}
		env.cifProvider = cp

		// TODO: make test work according to redesign
// 		listIFDs.contextHandle = ecr.contextHandle
// 		val listIFDsResponse = ifd.listIFDs(listIFDs)
// 		val recognitionInfo =
// 			cr.recognizeCard(ecr.contextHandle, listIFDsResponse.ifdName[0], BigInteger.ZERO)
// 	SALStateCallback salCallback = new SALStateCallback(env, states);
// 	Connect c = new Connect();
// 	c.setContextHandle(ecr.getContextHandle());
// 	c.setIFDName(listIFDsResponse.getIFDName().get(0));
// 	c.setSlot(BigInteger.ZERO);
// 	ConnectResponse connectResponse = env.getIFD().connect(c);
//
// 	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
// 	connectionHandleType.setContextHandle(ecr.getContextHandle());
// 	connectionHandleType.setRecognitionInfo(recognitionInfo);
// 	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
// 	connectionHandleType.setSlotIndex(BigInteger.ZERO);
// 	connectionHandleType.setSlotHandle(connectResponse.getSlotHandle());
// 	salCallback.signalEvent(EventType.CARD_RECOGNIZED, new IfdEventObject(connectionHandleType));
// 	instance = new TinySAL(env, states);
//
// 	// init AddonManager
// 	AddonManager manager = new AddonManager(env, uc, states, null);
// 	instance.setAddonManager(manager);
	}

	@Test(enabled = TESTS_ENABLED)
	@Throws(ParserConfigurationException::class)
	fun testDIDAuthenticate() {
		var cardApplicationPathType = CardApplicationPathType().apply { cardApplication = appIdentifierRoot }

		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}

		val cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath)
		cardApplicationPathType =
			cardApplicationPathResponse
				.cardAppPathResultSet
				.cardApplicationPathResult[0]

		val cardApplicationConnect =
			CardApplicationConnect().apply {
				this.cardApplicationPath = cardApplicationPathType
			}

		val result1 = instance.cardApplicationConnect(cardApplicationConnect)

		/**
		 * Test with a pin set.
		 */
		val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
		val d = factory.newDocumentBuilder().newDocument()
		val elemPin =
			d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Pin").apply {
				textContent = "123456"
			}
		var didAuthenticationData =
			DIDAuthenticationDataType().apply {
				any.add(elemPin)
				protocol = ECardConstants.Protocol.PIN_COMPARE
			}

		var parameters =
			DIDAuthenticate().apply {
				didName = "PIN.home"
				authenticationProtocolData = didAuthenticationData
				connectionHandle = result1.connectionHandle
				authenticationProtocolData = didAuthenticationData
			}

		var result = instance.didAuthenticate(parameters)

		Assert.assertEquals(result.getAuthenticationProtocolData().getProtocol(), ECardConstants.Protocol.PIN_COMPARE)
		Assert.assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor())
		Assert.assertEquals(result.getAuthenticationProtocolData().getAny().size, 0)

		/**
		 * Test without a pin set.
		 */
		didAuthenticationData =
			DIDAuthenticationDataType().apply {
				protocol = ECardConstants.Protocol.PIN_COMPARE
			}

		parameters =
			DIDAuthenticate().apply {
				didName = "PIN.home"
				authenticationProtocolData = didAuthenticationData
				connectionHandle = result1.connectionHandle
				authenticationProtocolData = didAuthenticationData
			}

		result = instance.didAuthenticate(parameters)

		Assert.assertEquals(result.getAuthenticationProtocolData().getProtocol(), ECardConstants.Protocol.PIN_COMPARE)
		Assert.assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor())
		Assert.assertEquals(result.getAuthenticationProtocolData().getAny().size, 0)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDCreate() {
		// TODO
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDUpdate() {
		// TODO
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDGet() {
		var cardApplicationPathType =
			CardApplicationPathType().apply {
				cardApplication = appIdentifierRoot
			}

		var cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}

		var cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath)

		cardApplicationPathType =
			cardApplicationPathResponse
				.cardAppPathResultSet
				.cardApplicationPathResult[0]

		var cardApplicationConnect = CardApplicationConnect().apply { this.cardApplicationPath = cardApplicationPathType }
		var result1 = instance.cardApplicationConnect(cardApplicationConnect)

		var didGet =
			DIDGet().apply {
				didName = "PIN.home"
				connectionHandle = result1.connectionHandle
			}
		var result = instance.didGet(didGet)

		Assert.assertEquals(result.getResult().getResultMajor(), "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")
		Assert.assertEquals(result.getDIDStructure().getDIDName(), "PIN.home")
		Assert.assertEquals(result.didStructure.didMarker.javaClass, PinCompareMarkerType::class.java)
		var pinCompareMarkerType =
			PINCompareMarkerType(
				result.didStructure.didMarker as PinCompareMarkerType,
			)
		Assert.assertEquals(ByteUtils.toHexString(pinCompareMarkerType.pinRef.keyRef), "02")

		// test with given correct scope
		didGet =
			DIDGet().apply {
				didName = "PIN.home"
				didScope = DIDScopeType.GLOBAL
				connectionHandle = result1.connectionHandle
			}
		result = instance.didGet(didGet)

		Assert.assertEquals(result.result.resultMajor, ECardConstants.Major.OK)
		Assert.assertEquals(result.didStructure.didName, "PIN.home")
		Assert.assertEquals(result.didStructure.didMarker.javaClass, PinCompareMarkerType::class.java)

		pinCompareMarkerType = PINCompareMarkerType(result.didStructure.didMarker as PinCompareMarkerType)
		Assert.assertEquals(ByteUtils.toHexString(pinCompareMarkerType.pinRef.keyRef), "02")

		cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}

		cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath)
		cardApplicationPathType =
			cardApplicationPathResponse
				.cardAppPathResultSet
				.cardApplicationPathResult[0]

		cardApplicationConnect =
			CardApplicationConnect().apply {
				this.cardApplicationPath = cardApplicationPathType
			}

		result1 = instance.cardApplicationConnect(cardApplicationConnect)

		Assert.assertEquals(result1.getResult().getResultMajor(), ECardConstants.Major.OK)

		didGet =
			DIDGet().apply {
				didName = "PIN.home"
				didScope = DIDScopeType.LOCAL
				connectionHandle = result1.connectionHandle
			}
		result = instance.didGet(didGet)

		Assert.assertEquals(result.getResult().getResultMajor(), ECardConstants.Major.ERROR)
		Assert.assertEquals(result.getResult().getResultMinor(), ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND)
	}

    /*
     * [TR-03112-7] The following functions are not supported with this protocol
     * and, when called up, relay an error message to this effect
     * /resultminor/sal#inappropriateProtocolForAction:
     * CardApplicationStartSession, Encipher, Decipher, GetRandom, Hash, Sign,
     * VerifySignature, VerifyCertificate
     */

	/**
	 * This Test ensures that all functions unsupported by this protocol relay the correct error message when
	 * called.
	 */
	@Test(enabled = TESTS_ENABLED)
	fun testUnsupportedFunctions() {
		var cardApplicationPathType =
			CardApplicationPathType().apply {
				cardApplication = appIdentifierRoot
			}

		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}

		val cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath)
		cardApplicationPathType =
			cardApplicationPathResponse
				.cardAppPathResultSet
				.cardApplicationPathResult[0]

		val cardApplicationConnect =
			CardApplicationConnect().apply {
				this.cardApplicationPath = cardApplicationPathType
			}

		val result1 = instance.cardApplicationConnect(cardApplicationConnect)

		val encipher =
			Encipher().apply {
				didName = "PIN.home"
				plainText = byteArrayOf(0x0, 0x0, 0x0)
				connectionHandle = result1.connectionHandle
			}
		val encipherResponse = instance.encipher(encipher)
		Assert.assertEquals(encipherResponse.getResult().getResultMajor(), ECardConstants.Major.ERROR)
		Assert.assertEquals(
			encipherResponse
				.getResult()
				.getResultMinor(),
			ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION,
		)
		// TODO remaining unsupported functions
	}
}
