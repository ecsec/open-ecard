/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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
package org.openecard.sal.protocol.genericcryptography

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.DIDCreate
import iso.std.iso_iec._24727.tech.schema.DIDGet
import iso.std.iso_iec._24727.tech.schema.DIDList
import iso.std.iso_iec._24727.tech.schema.DIDListResponse
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType
import iso.std.iso_iec._24727.tech.schema.DIDScopeType
import iso.std.iso_iec._24727.tech.schema.DIDUpdate
import iso.std.iso_iec._24727.tech.schema.DSIRead
import iso.std.iso_iec._24727.tech.schema.Decipher
import iso.std.iso_iec._24727.tech.schema.Encipher
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.GetRandom
import iso.std.iso_iec._24727.tech.schema.Hash
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType
import iso.std.iso_iec._24727.tech.schema.Sign
import iso.std.iso_iec._24727.tech.schema.SignResponse
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate
import iso.std.iso_iec._24727.tech.schema.VerifySignature
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.interfaces.CIFProvider
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.sal.did.CryptoMarkerType
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.openecard.ifd.scio.IFD
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.sal.TinySAL
import org.openecard.transport.dispatcher.MessageDispatcher
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Dirk Petrautzki
 */
class GenericCryptographyProtocolTest {
	companion object {
		private const val TESTS_ENABLED = false
	}

	private var plaintext: String? = null
	private var env: ClientEnv? = null
	private val instance: TinySAL? = null
	var cardApplication: ByteArray = StringUtils.toByteArray("A000000167455349474E")
	var cardApplicationRoot: ByteArray = StringUtils.toByteArray("D2760001448000")
	private var ifd: IFD? = null

	@BeforeClass
	@Throws(IOException::class)
	private fun loadFile(resourcePath: String?) {
		val `in` = GenericCryptographyProtocolTest::class.java.classLoader.getResourceAsStream(resourcePath)
		val w = StringWriter()
		val r = BufferedReader(InputStreamReader(`in`, Charset.forName("utf-8")))
		var nextLine: String?
		while ((r.readLine().also { nextLine = it }) != null) {
			w.write(nextLine!!)
			w.write(String.format("%n")) // platform dependent newline character
		}

		plaintext = w.toString()
	}

	@BeforeMethod
	@Throws(Exception::class)
	fun setUp() {
		env = ClientEnv()
		val d: Dispatcher = MessageDispatcher(env!!)
		env!!.dispatcher = d
		ifd = IFD()
		ifd!!.setEnvironment(env!!)
		env!!.gui = SwingUserConsent(SwingDialogWrapper())
		env!!.ifd = ifd

		val ecr = env!!.ifd!!.establishContext(EstablishContext())
		val cr = CardRecognitionImpl(env!!)
		val listIFDs = ListIFDs()
		val cp: CIFProvider =
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
		env!!.cifProvider = cp

		listIFDs.contextHandle = ecr.contextHandle
		val listIFDsResponse = ifd!!.listIFDs(listIFDs)
		val recognitionInfo =
			cr.recognizeCard(ecr.contextHandle, listIFDsResponse.ifdName[0], BigInteger.ZERO)
		// TODO: make test work according to redesign
// 	SALStateCallback salCallback = new SALStateCallback(env, states);
//
// 	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
// 	connectionHandleType.setContextHandle(ecr.getContextHandle());
// 	connectionHandleType.setRecognitionInfo(recognitionInfo);
// 	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
// 	connectionHandleType.setSlotIndex(new BigInteger("0"));
//
// 	salCallback.signalEvent(EventType.CARD_RECOGNIZED, new IfdEventObject(connectionHandleType));
// 	instance = new TinySAL(env, states);
// 	env.setSAL(instance);
//
// 	// init AddonManager
// 	UserConsent uc = new SwingUserConsent(new SwingDialogWrapper());
// 	AddonManager manager = new AddonManager(env, uc, states, null);
// 	instance.setAddonManager(manager);
	}

	@Test(enabled = TESTS_ENABLED)
	@Throws(ParserConfigurationException::class)
	fun testDIDGet() {
		val cardApplicationPathType =
			CardApplicationPathType().apply {
				setCardApplication(cardApplication)
			}
		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}
		val cardApplicationPathResponse = instance!!.cardApplicationPath(cardApplicationPath)

		val cardAppPathResultSet = cardApplicationPathResponse.cardAppPathResultSet
		val parameters =
			CardApplicationConnect().apply {
				this.cardApplicationPath = cardAppPathResultSet.cardApplicationPathResult[0]
			}
		val result = instance.cardApplicationConnect(parameters)
		Assert.assertEquals(ECardConstants.Major.OK, result.result.resultMajor)
		val didList =
			DIDList().apply {
				connectionHandle = result.connectionHandle
			}
		val didQualifier =
			DIDQualifierType().apply {
				applicationIdentifier = cardApplication
				objectIdentifier = ECardConstants.Protocol.GENERIC_CRYPTO
				applicationFunction = "Compute-signature"
			}
		didList.filter = didQualifier
		val didListResponse = instance.didList(didList)

		Assert.assertTrue(didListResponse.didNameList.didName.isNotEmpty())

		val didGet =
			DIDGet().apply {
				connectionHandle = result.connectionHandle
				didName = didListResponse.didNameList.didName[0]
				didScope = DIDScopeType.LOCAL
			}
		val didGetResponse = instance.didGet(didGet)

		Assert.assertEquals(ECardConstants.Major.OK, didGetResponse.result.resultMajor)
		val cryptoMarker =
			CryptoMarkerType(
				(
					didGetResponse
						.didStructure
						.didMarker as iso.std.iso_iec._24727.tech.schema.CryptoMarkerType?
				)!!,
			)
		Assert.assertEquals(cryptoMarker.certificateRefs[0].dataSetName, "EF.C.CH.AUT")
	}

	/**
	 * Test for the Sign Step of the Generic Cryptography protocol. After we connected to the ESIGN application of the
	 * eGK, we use DIDList to get a List of DIDs that support the compute signature function. For each DID we let the
	 * card compute a signature. If the result is OK we're satisfied.
	 *
	 * @throws Exception
	 * when something in this test went unexpectedly wrong
	 */
	@Test(enabled = TESTS_ENABLED)
	@Throws(Exception::class)
	fun testSign() {
		val cardApplicationPathType =
			CardApplicationPathType().apply {
				setCardApplication(cardApplication)
			}
		val cardApplicationPath =
			CardApplicationPath().apply {
				setCardAppPathRequest(cardApplicationPathType)
			}
		val cardApplicationPathResponse = instance!!.cardApplicationPath(cardApplicationPath)
		checkResult<CardApplicationPathResponse>(cardApplicationPathResponse)

		val cardAppPathResultSet = cardApplicationPathResponse.cardAppPathResultSet
		val parameters =
			CardApplicationConnect().apply {
				setCardApplicationPath(cardAppPathResultSet.cardApplicationPathResult[0])
			}
		val result = instance.cardApplicationConnect(parameters)
		checkResult<CardApplicationConnectResponse>(result)

		Assert.assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor())
		val didQualifier =
			DIDQualifierType().apply {
				applicationIdentifier = cardApplication
				objectIdentifier = ECardConstants.Protocol.GENERIC_CRYPTO
				applicationFunction = "Compute-signature"
			}
		val didList =
			DIDList().apply {
				connectionHandle = result.connectionHandle
				filter = didQualifier
			}
		val didListResponse = instance.didList(didList)
		Assert.assertTrue(didListResponse.didNameList.didName.isNotEmpty())
		checkResult<DIDListResponse>(didListResponse)

		val didAuthenticationData = PinCompareDIDAuthenticateInputType()
		val didAuthenticate =
			DIDAuthenticate().apply {
				didName = "PIN.home"
				authenticationProtocolData = didAuthenticationData
				connectionHandle = result.connectionHandle
				connectionHandle.cardApplication = cardApplicationRoot
				authenticationProtocolData = didAuthenticationData
			}
		didAuthenticationData.protocol = ECardConstants.Protocol.PIN_COMPARE
		val didAuthenticateResult = instance.didAuthenticate(didAuthenticate)
		checkResult<DIDAuthenticateResponse>(didAuthenticateResult)

		Assert.assertEquals(
			didAuthenticateResult.authenticationProtocolData.protocol,
			ECardConstants.Protocol.PIN_COMPARE,
		)
		Assert.assertEquals(didAuthenticateResult.authenticationProtocolData.any.size, 0)
		Assert.assertEquals(ECardConstants.Major.OK, didAuthenticateResult.result.resultMajor)

		for (numOfDIDs in didListResponse.didNameList.didName.indices) {
			val didName = didListResponse.didNameList.didName[numOfDIDs]
			val didGet =
				DIDGet().apply {
					this.didName = didName
					didScope = DIDScopeType.LOCAL
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
				}
			val didGetResponse = instance.didGet(didGet)

			val cryptoMarker =
				CryptoMarkerType(
					(
						didGetResponse
							.didStructure
							.didMarker as iso.std.iso_iec._24727.tech.schema.CryptoMarkerType?
					)!!,
				)

			val message = StringUtils.toByteArray("616263646263646563646566646566676566676861")

			val algorithm = cryptoMarker.algorithmInfo!!.getAlgorithmIdentifier().getAlgorithm()
			if (algorithm == GenericCryptoUris.sigS_ISO9796_2rnd) {
				// TODO support for sign9796_2_DS2
				continue
			}

			val sign =
				Sign().apply {
					setMessage(message)
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
				}
			val signResponse = instance.sign(sign)
			checkResult<SignResponse>(signResponse)
			Assert.assertTrue(signResponse.signature != null)
		}
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDCreate() {
		// TODO write test as soon as implemented
		val resp = instance!!.didCreate(DIDCreate())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDUpdate() {
		// TODO write test as soon as implemented
		val resp = instance!!.didUpdate(DIDUpdate())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testEncipher() {
		// TODO write test as soon as implemented
		val resp = instance!!.encipher(Encipher())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	/**
	 * Test for the Decipher Step of the Generic Cryptography protocol. After we connected to the ESIGN application
	 * of the eGK, we use DIDList to get a List of DIDs that support the Decipher function. We then authenticate with
	 * PIN.home and read the contents of the DIDs certificate. With it's public key we encrypt the contents of
	 * plaintext.txt and finally let the card decrypt it through a call to Decipher. In the end we match the result with
	 * the original plaintext.
	 *
	 * @throws Exception when something in this test went unexpectedly wrong
	 */
	@Test(enabled = TESTS_ENABLED)
	@Throws(Exception::class)
	fun testDecipher() {
		val cardApplicationPathType =
			CardApplicationPathType().apply {
				setCardApplication(cardApplication)
			}
		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}
		val cardApplicationPathResponse = instance!!.cardApplicationPath(cardApplicationPath)
		checkResult<CardApplicationPathResponse>(cardApplicationPathResponse)
		val parameters = CardApplicationConnect()
		val cardAppPathResultSet = cardApplicationPathResponse.cardAppPathResultSet
		parameters.cardApplicationPath = cardAppPathResultSet.cardApplicationPathResult[0]

		val result = instance.cardApplicationConnect(parameters)
		checkResult<CardApplicationConnectResponse>(result)
		Assert.assertEquals(ECardConstants.Major.OK, result.result.resultMajor)

		val didQualifier =
			DIDQualifierType().apply {
				applicationIdentifier = cardApplication
				objectIdentifier = ECardConstants.Protocol.GENERIC_CRYPTO
				applicationFunction = "Decipher"
			}
		val didList =
			DIDList().apply {
				connectionHandle = result.connectionHandle
				filter = didQualifier
			}

		val didListResponse = instance.didList(didList)
		Assert.assertTrue(didListResponse.didNameList.didName.isNotEmpty())
		checkResult<DIDListResponse>(didListResponse)

		val didAuthenticationData = PinCompareDIDAuthenticateInputType()
		didAuthenticationData.setProtocol(ECardConstants.Protocol.PIN_COMPARE)
		val didAuthenticate =
			DIDAuthenticate().apply {
				didName = "PIN.home"
				authenticationProtocolData = didAuthenticationData
				connectionHandle = result.connectionHandle
				connectionHandle.cardApplication = cardApplicationRoot
				authenticationProtocolData = didAuthenticationData
			}
		val didAuthenticateResult = instance.didAuthenticate(didAuthenticate)
		checkResult<DIDAuthenticateResponse>(didAuthenticateResult)

		Assert.assertEquals(
			didAuthenticateResult.authenticationProtocolData.protocol,
			ECardConstants.Protocol.PIN_COMPARE,
		)
		Assert.assertEquals(didAuthenticateResult.authenticationProtocolData.any.size, 0)
		Assert.assertEquals(ECardConstants.Major.OK, didAuthenticateResult.result.resultMajor)

		val plaintextBytes = plaintext!!.toByteArray()

		for (numOfDIDs in didListResponse.didNameList.didName.indices) {
			val didName = didListResponse.didNameList.didName[numOfDIDs]
			val didGet =
				DIDGet().apply {
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
				}
			val didGetResponse = instance.didGet(didGet)

			val cryptoMarker =
				CryptoMarkerType(
					(
						didGetResponse
							.didStructure
							.didMarker as iso.std.iso_iec._24727.tech.schema.CryptoMarkerType?
					)!!,
				)

			val ciphertext = ByteArrayOutputStream()

			// read the certificate
			val dsiRead =
				DSIRead().apply {
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
					dsiName = cryptoMarker.certificateRefs[0].dataSetName
				}
			val dsiReadResponse = instance.dsiRead(dsiRead)
			Assert.assertEquals(ECardConstants.Major.OK, dsiReadResponse.result.resultMajor)
			Assert.assertTrue(dsiReadResponse.getDSIContent().size > 0)

			// convert the contents to a certificate
			val cert: Certificate? =
				CertificateFactory.getInstance("X.509").generateCertificate(
					ByteArrayInputStream(dsiReadResponse.dsiContent),
				) as X509Certificate?

			val cipher: Cipher?
			val blocksize: Int
			val algorithmUri = cryptoMarker.algorithmInfo!!.algorithmIdentifier.algorithm
			if (algorithmUri == GenericCryptoUris.RSA_ENCRYPTION) {
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
				cipher.init(Cipher.ENCRYPT_MODE, cert)
				blocksize = 245 // keysize/8-pkcspadding = (2048)/8-11
			} else if (algorithmUri == GenericCryptoUris.RSAES_OAEP) {
				cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", BouncyCastleProvider())
				cipher.init(Cipher.ENCRYPT_MODE, cert)
				blocksize = cipher.blockSize
			} else {
				logger.warn { "Skipping decipher for the unsupported algorithmOID: $algorithmUri" }
				continue
			}

			val rest = plaintextBytes.size % blocksize

			// encrypt block for block
			var offset = 0
			while (offset < plaintextBytes.size) {
				if ((offset + blocksize) > plaintextBytes.size) {
					ciphertext.write(cipher.doFinal(plaintextBytes, offset, rest))
				} else {
					ciphertext.write(cipher.doFinal(plaintextBytes, offset, blocksize))
				}
				offset += blocksize
			}

			var decipher =
				Decipher().apply {
					setCipherText(ciphertext.toByteArray())
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
				}
			var decipherResponse = instance.decipher(decipher)

			Assert.assertEquals(decipherResponse.plainText, plaintextBytes)

			// test invalid ciphertext length (not divisible through blocksize without rest)
			decipher =
				Decipher().apply {
					setCipherText(ByteUtils.concatenate(0x00.toByte(), ciphertext.toByteArray()))
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
				}
			decipherResponse = instance.decipher(decipher)
			val res = decipherResponse.result
			Assert.assertEquals(res.resultMajor, ECardConstants.Major.ERROR)
			Assert.assertEquals(res.resultMinor, ECardConstants.Minor.App.INCORRECT_PARM)
		}
	}

	@Test(enabled = TESTS_ENABLED)
	fun testGetRandom() {
		// TODO write test as soon as implemented
		val resp = instance!!.getRandom(GetRandom())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testHash() {
		// TODO write test as soon as implemented
		val resp = instance!!.hash(Hash())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	/**
	 * Test for the VerifySignature Step of the Generic Cryptography protocol. After we connected to the ESIGN
	 * application of the eGK, we use DIDList to get a List of DIDs that support the compute signature function. We
	 * then authenticate with PIN.home and let the card sign our message. Afterwards we call VerifySignature for that
	 * signature which should return OK.
	 *
	 * @throws Exception
	 * when something in this test went unexpectedly wrong
	 */
	@Test(enabled = TESTS_ENABLED)
	@Throws(Exception::class)
	fun testVerifySignature() {
		val cardApplicationPathType =
			CardApplicationPathType().apply {
				setCardApplication(cardApplication)
			}
		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cardApplicationPathType
			}

		val cardApplicationPathResponse = instance!!.cardApplicationPath(cardApplicationPath)
		checkResult<CardApplicationPathResponse>(cardApplicationPathResponse)
		val cardAppPathResultSet = cardApplicationPathResponse.cardAppPathResultSet
		val parameters =
			CardApplicationConnect().apply {
				setCardApplicationPath(cardAppPathResultSet.cardApplicationPathResult[0])
			}
		val result = instance.cardApplicationConnect(parameters)
		checkResult<CardApplicationConnectResponse>(result)

		Assert.assertEquals(ECardConstants.Major.OK, result.result.resultMajor)
		val didQualifier =
			DIDQualifierType().apply {
				applicationIdentifier = cardApplication
				objectIdentifier = ECardConstants.Protocol.GENERIC_CRYPTO
				applicationFunction = "Compute-signature"
			}
		val didList =
			DIDList().apply {
				connectionHandle = result.connectionHandle
				filter = didQualifier
			}
		val didListResponse = instance.didList(didList)
		Assert.assertTrue(didListResponse.didNameList.didName.isNotEmpty())
		checkResult<DIDListResponse>(didListResponse)

		val didAuthenticationData = PinCompareDIDAuthenticateInputType()
		didAuthenticationData.setProtocol(ECardConstants.Protocol.PIN_COMPARE)
		val didAuthenticate =
			DIDAuthenticate().apply {
				didName =
					"PIN.home"
				authenticationProtocolData = didAuthenticationData
				connectionHandle = result.connectionHandle
				connectionHandle.cardApplication = cardApplicationRoot
			}
		val didAuthenticateResult = instance.didAuthenticate(didAuthenticate)
		checkResult<DIDAuthenticateResponse>(didAuthenticateResult)

		Assert.assertEquals(
			didAuthenticateResult.authenticationProtocolData.protocol,
			ECardConstants.Protocol.PIN_COMPARE,
		)
		Assert.assertEquals(didAuthenticateResult.authenticationProtocolData.any.size, 0)
		Assert.assertEquals(ECardConstants.Major.OK, didAuthenticateResult.result.resultMajor)

		for (numOfDIDs in didListResponse.didNameList.didName.indices) {
			val didName = didListResponse.didNameList.didName[numOfDIDs]
			val didGet =
				DIDGet().apply {
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
					connectionHandle = result.connectionHandle
					connectionHandle.cardApplication = cardApplication
				}
			val didGetResponse = instance.didGet(didGet)

			val sign = Sign()
			var message: ByteArray? = byteArrayOf(0x01, 0x02, 0x03)
			val cryptoMarker =
				CryptoMarkerType(
					(
						didGetResponse
							.didStructure
							.didMarker as iso.std.iso_iec._24727.tech.schema.CryptoMarkerType?
					)!!,
				)

			val algorithmIdentifier = cryptoMarker.algorithmInfo!!.algorithmIdentifier.algorithm

			if (algorithmIdentifier == GenericCryptoUris.RSASSA_PSS_SHA256) {
				val messageDigest = MessageDigest.getInstance("SHA-256")
				message = messageDigest.digest(message)
			} else if (algorithmIdentifier == GenericCryptoUris.RSA_ENCRYPTION) {
				// do nothing
			} else {
				logger.warn { "Skipping decipher for the unsupported algorithmIdentifier: $algorithmIdentifier" }
				continue
			}

			sign.apply {
				setMessage(message)
				connectionHandle = result.connectionHandle
				connectionHandle.cardApplication = cardApplication
				setDIDName(didName)
				didScope = DIDScopeType.LOCAL
			}
			val signResponse = instance.sign(sign)
			Assert.assertEquals(ECardConstants.Major.OK, signResponse.result.resultMajor)
			checkResult<SignResponse>(signResponse)

			val signature = signResponse.signature

			val verifySignature =
				VerifySignature().apply {
					connectionHandle = sign.connectionHandle
					setDIDName(didName)
					didScope = DIDScopeType.LOCAL
					setMessage(message)
					setSignature(signature)
				}
			val verifySignatureResponse = instance.verifySignature(verifySignature)
			checkResult<VerifySignatureResponse>(verifySignatureResponse)
		}
	}

	@Test(enabled = TESTS_ENABLED)
	fun testVerifyCertificate() {
		// TODO write test as soon as implemented
		val resp = instance!!.verifyCertificate(VerifyCertificate())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testDIDAuthenticate() {
		// TODO write test as soon as implemented
		val resp = instance!!.didAuthenticate(DIDAuthenticate())
		Assert.assertEquals(resp.result.resultMajor, ECardConstants.Major.ERROR)
	}

	@Test(enabled = TESTS_ENABLED)
	fun testCardApplicationStartSession() {
		// TODO expected result /resultminor/sal#inappropriateProtocolForAction
	}
}
