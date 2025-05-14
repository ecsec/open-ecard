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

import org.openecard.bouncycastle.cert.jcajce.JcaCertStore
import org.openecard.bouncycastle.cms.CMSException
import org.openecard.bouncycastle.cms.CMSProcessableByteArray
import org.openecard.bouncycastle.cms.CMSSignedData
import org.openecard.bouncycastle.cms.CMSSignedDataGenerator
import org.openecard.bouncycastle.cms.CMSSignerDigestMismatchException
import org.openecard.bouncycastle.cms.CMSTypedData
import org.openecard.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.openecard.bouncycastle.operator.OperatorCreationException
import org.openecard.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.openecard.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.openecard.common.util.StringUtils
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.SignatureException
import java.security.UnrecoverableKeyException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 *
 * @author Tobias Wich
 */
class SignatureTest {
	private val pass = "1"
	private val caSignedAlias = "cms sign cert (cg test intermediate ca)"
	private val selfSignedAlias = "self signed"

	private var trustStore: KeyStore? = null
	private var signStore: KeyStore? = null

	@BeforeClass
	@Throws(KeyStoreException::class, IOException::class, NoSuchAlgorithmException::class, CertificateException::class)
	fun init() {
		val trustStoreStream = SignatureTest::class.java.getResourceAsStream("/cms_root.jks")
		val signStoreStream = SignatureTest::class.java.getResourceAsStream("/cms_sign.jks")

		trustStore = KeyStore.getInstance("JKS")
		trustStore!!.load(trustStoreStream, null)

		signStore = KeyStore.getInstance("JKS")
		signStore!!.load(signStoreStream, pass.toCharArray())
	}

	@Throws(
		KeyStoreException::class,
		NoSuchAlgorithmException::class,
		UnrecoverableKeyException::class,
		InvalidKeyException::class,
		SignatureException::class,
		OperatorCreationException::class,
		CertificateEncodingException::class,
		CMSException::class,
	)
	private fun createSignature(
		sigAlg: String?,
		alias: String?,
		challenge: ByteArray?,
	): CMSSignedData {
		val privKey = signStore!!.getKey(alias, pass.toCharArray()) as PrivateKey?
		val cert = signStore!!.getCertificate(alias) as X509Certificate?
		val certChain = signStore!!.getCertificateChain(alias) as Array<Certificate?>
		val certs = JcaCertStore(listOf(*certChain))

		// 	Signature signature = Signature.getInstance("SHA256WithRSA");
// 	signature.initSign(privKey);
// 	signature.update(challenge);
// 	byte[] signedBytes = signature.sign();
		val msg: CMSTypedData = CMSProcessableByteArray(challenge)
		val signer = JcaContentSignerBuilder(sigAlg).build(privKey)
		val dgProv = JcaDigestCalculatorProviderBuilder().build()
		val gen = CMSSignedDataGenerator()
		gen.addSignerInfoGenerator(JcaSignerInfoGeneratorBuilder(dgProv).build(signer, cert))
		gen.addCertificates(certs)
		val sigData = gen.generate(msg, false)
		return sigData
	}

	@Test
	@Throws(
		KeyStoreException::class,
		NoSuchAlgorithmException::class,
		UnrecoverableKeyException::class,
		InvalidKeyException::class,
		SignatureException::class,
		OperatorCreationException::class,
		CertificateEncodingException::class,
		CMSException::class,
		IOException::class,
		CertificateException::class,
	)
	fun testValidSignature() {
		val challenge = "Hello World!".toByteArray()

		val sigData = createSignature("SHA256withECDSA", caSignedAlias, challenge)
		val sigBytes = sigData.encoded

		val validator = SignatureVerifier(trustStore!!, challenge)
		try {
			validator.validate(sigBytes)
		} catch (ex: SignatureInvalid) {
			Assert.fail("Invalid signature found, expected a valid one.")
		}
	}

	@Test
	@Throws(
		KeyStoreException::class,
		NoSuchAlgorithmException::class,
		UnrecoverableKeyException::class,
		InvalidKeyException::class,
		SignatureException::class,
		OperatorCreationException::class,
		CertificateEncodingException::class,
		CMSException::class,
		IOException::class,
		CertificateException::class,
	)
	fun testInvalidSignature() {
		val challenge = "Hello World!".toByteArray()

		val sigData = createSignature("SHA256withECDSA", caSignedAlias, challenge)
		val sigBytes = sigData.encoded

		challenge[0] = 1 // flip a bit in the challenge and see if it still verifies
		val validator = SignatureVerifier(trustStore!!, challenge)
		try {
			validator.validate(sigBytes)
			Assert.fail("Valid signature found, expected an invalid one.")
		} catch (ex: SignatureInvalid) {
		}
	}

	@Test
	@Throws(
		KeyStoreException::class,
		NoSuchAlgorithmException::class,
		UnrecoverableKeyException::class,
		InvalidKeyException::class,
		SignatureException::class,
		OperatorCreationException::class,
		CertificateEncodingException::class,
		CMSException::class,
		IOException::class,
		CertificateException::class,
		CMSSignerDigestMismatchException::class,
		InvalidAlgorithmParameterException::class,
	)
	fun testInvalidPath() {
		val challenge = "Hello World!".toByteArray()

		val sigData = createSignature("SHA256withRSA", selfSignedAlias, challenge)
		val sigBytes = sigData.encoded

		val validator = SignatureVerifier(trustStore!!, challenge)
		try {
			validator.validate(sigBytes)
			Assert.fail("Valid signature found, expected an invalid one.")
		} catch (ex: SignatureInvalid) {
		}
	}

	// disabled due to incompatible truststores
	@Test(enabled = false)
	@Throws(
		IOException::class,
		KeyStoreException::class,
		NoSuchAlgorithmException::class,
		CertificateException::class,
		SignatureInvalid::class,
	)
	fun testLTSignature() {
		System.setProperty("java.security.debug", "certpath")

		val challenge = StringUtils.toByteArray("F8CB44221F3471FC492294B5F49DF9C0")
		val sigStr =
			"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"
		val sig = sigStr.toByteArray()

		val verifier = SignatureVerifier(challenge)

		// TODO: obtain real signature with reachable OCSP and/or CRL, then enable revocation check
		// use revocation for this test
		verifier.isRevocationCheck = false

		verifier.validate(sig)
	}
}
