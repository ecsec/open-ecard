/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.crypto.common.asn1.eac.EFCardAccessTest
import org.testng.Assert
import java.io.ByteArrayOutputStream
import kotlin.test.Test

/**
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class CardVerifiableCertificateChainTest {
	private var certificates = ArrayList<CardVerifiableCertificate>()
	private val malformedCertificates = ArrayList<CardVerifiableCertificate>()
	private lateinit var cvca: ByteArray
	private lateinit var dv: ByteArray
	private lateinit var at: ByteArray
	private lateinit var malformedAT: ByteArray

	fun init() {
		try {
			cvca = loadTestFile("cert_cvca.cvcert")
			dv = loadTestFile("cert_dv.cvcert")
			at = loadTestFile("cert_at.cvcert")
			malformedAT = loadTestFile("cert_at_malformed.cvcert")

			certificates.add(CardVerifiableCertificate(cvca))
			certificates.add(CardVerifiableCertificate(dv))
			certificates.add(CardVerifiableCertificate(at))

			// 	    certificates.add(new CardVerifiableCertificate(malformedAT));
			malformedCertificates.add(CardVerifiableCertificate(cvca))
			malformedCertificates.add(CardVerifiableCertificate(dv))
			malformedCertificates.add(CardVerifiableCertificate(malformedAT))
		} catch (ex: Exception) {
			Assert.fail(ex.message)
		}
	}

	@Test(enabled = true)
	@Throws(Exception::class)
	fun testChain() {
		init()
		var chain = CardVerifiableCertificateChain(certificates)
		Assert.assertEquals(certificates[0], chain.cVCACertificates[0])
		Assert.assertEquals(certificates[1], chain.dVCertificates[0])
		Assert.assertEquals(certificates[2], chain.terminalCertificate)

        /*
         * test missing cvca certificate
         */
		certificates.removeAt(0)
		chain = CardVerifiableCertificateChain(certificates)
		Assert.assertEquals(certificates[0], chain.dVCertificates[0])
		Assert.assertEquals(certificates[1], chain.terminalCertificate)

		/*
		 * test add
		 */
		certificates = ArrayList<CardVerifiableCertificate>()
		certificates.add(CardVerifiableCertificate(cvca))
		certificates.add(CardVerifiableCertificate(dv))
		certificates.add(CardVerifiableCertificate(at))
		chain.addCertificate(CardVerifiableCertificate(cvca))

		Assert.assertTrue(certificates[0].compare(chain.cVCACertificates[0]))
		Assert.assertTrue(certificates[1].compare(chain.dVCertificates[0]))
		Assert.assertTrue(certificates[2].compare(chain.terminalCertificate!!))
	}

	@Throws(Exception::class)
	private fun loadTestFile(file: String): ByteArray {
		val path = "/$file"
		val ins = EFCardAccessTest::class.java.getResourceAsStream(path)
		val baos = ByteArrayOutputStream(ins!!.available())
		try {
			var b: Int
			while ((ins.read().also { b = it }) != -1) {
				baos.write(b.toByte().toInt())
			}
		} catch (e: Exception) {
			Assert.fail(e.message)
		}
		return baos.toByteArray()
	}
}
