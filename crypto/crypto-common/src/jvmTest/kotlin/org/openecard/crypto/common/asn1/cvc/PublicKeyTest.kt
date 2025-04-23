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
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import kotlin.Throws
import kotlin.test.assertEquals

/**
 *
 * @author Moritz Horsch
 */
class PublicKeyTest {
	private lateinit var chain: CardVerifiableCertificateChain

	@BeforeTest
	@Throws(Exception::class)
	fun setUp() {
		try {
			val cvca = loadTestFile("cert_cvca.cvcert")
			val dv = loadTestFile("cert_dv.cvcert")
			val at = loadTestFile("cert_at.cvcert")

			val certificates =
				listOf(
					CardVerifiableCertificate(cvca),
					CardVerifiableCertificate(dv),
					CardVerifiableCertificate(at),
				)
			chain = CardVerifiableCertificateChain(certificates)
		} catch (ex: Exception) {
			Assert.fail(ex.message)
		}
	}

	@Test
	@Throws(Exception::class)
	fun testKeyCVCA() {
		val pk = chain.cVCACertificates[0].getPublicKey()
		assertEquals(TAObjectIdentifier.id_TA_ECDSA_SHA_512, pk.objectIdentifier)
	}

	@Test
	@Throws(Exception::class)
	fun testKeyDV() {
		val pk = chain.dVCertificates[0].getPublicKey()
		assertEquals(TAObjectIdentifier.id_TA_ECDSA_SHA_512, pk.objectIdentifier)
	}

	@Test
	@Throws(Exception::class)
	fun testKeyTerminal() {
		val pk = chain.terminalCertificate!!.getPublicKey()
		assertEquals(TAObjectIdentifier.id_TA_ECDSA_SHA_512, pk.objectIdentifier)
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
