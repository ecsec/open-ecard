/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
package org.openecard.common.apdu.common

import org.openecard.common.apdu.GeneralAuthenticate
import org.openecard.common.apdu.ReadBinary
import org.openecard.common.util.ByteUtils.concatenate
import org.openecard.common.util.IntegerUtils
import org.openecard.common.util.StringUtils.toByteArray
import org.testng.Assert
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Random

/**
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class CardCommandAPDUTest {
	var rnd: Random = Random()

	@Test
	@Throws(IOException::class)
	fun testLengthCommand() {
		var apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte())
		Assert.assertEquals(apdu.toByteArray(), byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte()))

		apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), fillBytes(1))
		Assert.assertEquals(apdu.lc, 1)

		apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), fillBytes(255))
		Assert.assertEquals(apdu.lc, 255)

		apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), fillBytes(256))
		Assert.assertEquals(apdu.lc, 256)

		apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), fillBytes(65535))
		Assert.assertEquals(apdu.lc, 65535)
	}

	@Test
	@Throws(IOException::class)
	fun testLengthExpected() {
		var apdu: CardCommandAPDU = ReadBinary()

		Assert.assertEquals(apdu.toByteArray(), byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte()))

		apdu.setLE(1)
		Assert.assertEquals(
			apdu.toByteArray(),
			byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), 0x01.toByte()),
		)

		apdu.setLE(255)
		Assert.assertEquals(
			apdu.toByteArray(),
			byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xFF.toByte()),
		)

		apdu.setLE(256)
		Assert.assertEquals(
			apdu.toByteArray(),
			byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), 0x00.toByte()),
		)

		apdu.setLE(257)
		Assert.assertEquals(
			apdu.toByteArray(),
			byteArrayOf(
				0x00.toByte(),
				0xB0.toByte(),
				0x00.toByte(),
				0xFF.toByte(),
				0x00.toByte(),
				0x01.toByte(),
				0x01.toByte(),
			),
		)

		apdu.setLE(65535)
		Assert.assertEquals(
			apdu.toByteArray(),
			byteArrayOf(
				0x00.toByte(),
				0xB0.toByte(),
				0x00.toByte(),
				0xFF.toByte(),
				0x00.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
			),
		)

		apdu = CardCommandAPDU(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0xFF.toByte(), fillBytes(65535))
		apdu.setLE(65535)
		Assert.assertEquals(apdu.lc, 65535)
		Assert.assertEquals(apdu.le, 65535)

		// test an expected length of 256 with an extended length command apdu
		// the expected LE is {0x01, 0x00}
		apdu.setLE(256)
		val length = apdu.toByteArray().size
		Assert.assertEquals(apdu.toByteArray()[length - 2].toInt(), 0x01)
		Assert.assertEquals(apdu.toByteArray()[length - 1].toInt(), 0x00)
	}

	@Test
	@Throws(IOException::class)
	fun testBodyParsing() {
		val apdu: CardCommandAPDU = ReadBinary()

		// Case 2. : |CLA|INS|P1|P2|LE|
		apdu.setBody(byteArrayOf(0xFF.toByte()))
		Assert.assertEquals(apdu.le, 255)
		Assert.assertEquals(apdu.lc, -1)
		Assert.assertEquals(apdu.data, ByteArray(0))

		// Case 2.1: |CLA|INS|P1|P2|EXTLE|
		apdu.setBody(byteArrayOf(0x00.toByte(), 0x01.toByte(), 0xFF.toByte()))
		Assert.assertEquals(apdu.lc, -1)
		Assert.assertEquals(apdu.le, 511)
		Assert.assertEquals(apdu.data, ByteArray(0))

		apdu.setBody(byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte()))
		Assert.assertEquals(apdu.lc, -1)
		Assert.assertEquals(apdu.le, 65536)
		Assert.assertEquals(apdu.encodeLeField(), byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte()))
		Assert.assertEquals(apdu.data, ByteArray(0))

		// Case 3. : |CLA|INS|P1|P2|LC|DATA|
		apdu.setBody(fillBytesWithLength(240))
		Assert.assertEquals(apdu.lc, 240)
		Assert.assertEquals(apdu.le, -1)

		// Case 3.1: |CLA|INS|P1|P2|EXTLC|DATA|
		apdu.setBody(fillBytesWithLength(366))
		Assert.assertEquals(apdu.lc, 366)
		Assert.assertEquals(apdu.le, -1)

		// Case 4. : |CLA|INS|P1|P2|LC|DATA|LE
		apdu.setBody(byteArrayOf(0x01.toByte(), 0x01.toByte(), 0xFF.toByte()))
		Assert.assertEquals(apdu.le, 255)
		Assert.assertEquals(apdu.lc, 1)

		// Case 4.1 : |CLA|INS|P1|P2|EXTLC|DATA|LE|
		apdu.setBody(concatenate(fillBytesWithLength(366), 0xF0.toByte())!!)
		Assert.assertEquals(apdu.le, 240)
		Assert.assertEquals(apdu.lc, 366)

		// Case 4.2 : |CLA|INS|P1|P2|LC|DATA|EXTLE|
		apdu.setBody(byteArrayOf(0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0xFF.toByte()))
		Assert.assertEquals(apdu.le, 511)
		Assert.assertEquals(apdu.lc, 1)

		// Case 4.3: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
		apdu.setBody(concatenate(fillBytesWithLength(366), byteArrayOf(0x00.toByte(), 0x01.toByte(), 0xFF.toByte()))!!)
		Assert.assertEquals(apdu.le, 511)
		Assert.assertEquals(apdu.lc, 366)
	}

	@Test
	fun testAPDUs() {
		val apdu: CardCommandAPDU = GeneralAuthenticate()
	}

	@Test
	fun testConstructors() {
		// test constructor CardCommandAPDU(byte[] commandAPDU) with Case 4.2 APDU
		var capdu =
			CardCommandAPDU(
				byteArrayOf(
					0x00.toByte(),
					0xAB.toByte(),
					0xBC.toByte(),
					0xDE.toByte(),
					0x01.toByte(),
					0x01.toByte(),
					0x00.toByte(),
					0x01.toByte(),
					0xFF.toByte(),
				),
			)
		Assert.assertEquals(0x00.toByte(), capdu.cla)
		Assert.assertEquals(0xAB.toByte(), capdu.ins)
		Assert.assertEquals(0xBC.toByte(), capdu.p1)
		Assert.assertEquals(0xDE.toByte(), capdu.p2)
		Assert.assertEquals(0x01.toByte().toInt(), capdu.lc)
		Assert.assertEquals(511, capdu.le)
		Assert.assertEquals(byteArrayOf(0x01.toByte()), capdu.data)
		Assert.assertEquals(byteArrayOf(0x00.toByte(), 0xAB.toByte(), 0xBC.toByte(), 0xDE.toByte()), capdu.header)

		// test constructor CardCommandAPDU(byte[] commandAPDU) with Case 1 APDU
		capdu = CardCommandAPDU(byteArrayOf(0x00.toByte(), 0xAB.toByte(), 0xBC.toByte(), 0xDE.toByte()))
		Assert.assertEquals(0x00.toByte(), capdu.cla)
		Assert.assertEquals(0xAB.toByte(), capdu.ins)
		Assert.assertEquals(0xBC.toByte(), capdu.p1)
		Assert.assertEquals(0xDE.toByte(), capdu.p2)
		Assert.assertEquals(-1, capdu.lc)
		Assert.assertEquals(-1, capdu.le)
		Assert.assertEquals(capdu.data, ByteArray(0))
		Assert.assertEquals(byteArrayOf(0x00.toByte(), 0xAB.toByte(), 0xBC.toByte(), 0xDE.toByte()), capdu.header)
	}

	@Test
	fun testGetBody() {
		val apdu =
			byteArrayOf(
				0x00.toByte(),
				0xAB.toByte(),
				0xBC.toByte(),
				0xDE.toByte(),
				0x01.toByte(),
				0x01.toByte(),
				0x00.toByte(),
				0x01.toByte(),
				0xFF.toByte(),
			)

		Assert.assertEquals(
			CardCommandAPDU.getBody(apdu),
			byteArrayOf(0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0xFF.toByte()),
		)
	}

	@Test
	fun testGetHeader() {
		val apdu =
			byteArrayOf(
				0x00.toByte(),
				0xAB.toByte(),
				0xBC.toByte(),
				0xDE.toByte(),
				0x01.toByte(),
				0x01.toByte(),
				0x00.toByte(),
				0x01.toByte(),
				0xFF.toByte(),
			)

		Assert.assertEquals(
			CardCommandAPDU.getHeader(apdu),
			byteArrayOf(0x00.toByte(), 0xAB.toByte(), 0xBC.toByte(), 0xDE.toByte()),
		)
	}

	@Test(expectedExceptions = [IllegalArgumentException::class])
	fun testGetHeader2() {
		val apdu = byteArrayOf(0x00.toByte(), 0xAB.toByte(), 0xBC.toByte())

		CardCommandAPDU.getHeader(apdu)
	}

	@Suppress("ktlint:standard:function-naming")
	@Test
	fun SMparsing() {
		val orig = "0C 84 00 00 0D 97 01 08 8E 08 74 F2 71 EF 73 64 20 02 00"
		val origBytes = toByteArray(orig, true)
		val apdu = CardCommandAPDU(origBytes)
		val copyBytes = apdu.toByteArray()
		Assert.assertEquals(copyBytes, origBytes)
	}

	@Throws(IOException::class)
	private fun fillBytesWithLength(i: Int): ByteArray {
		val baos = ByteArrayOutputStream()

		if (i > 255) {
			baos.write(0x00.toByte().toInt())
		}
		baos.write(IntegerUtils.toByteArray(i))

		for (j in 0..<i) {
			baos.write((rnd.nextInt() and 0xFF.toByte().toInt()).toByte().toInt())
		}
		return baos.toByteArray()
	}

	@Throws(IOException::class)
	private fun fillBytes(i: Int): ByteArray {
		val baos = ByteArrayOutputStream()

		for (j in 0..<i) {
			baos.write((rnd.nextInt() and 0xFF.toByte().toInt()).toByte().toInt())
		}
		return baos.toByteArray()
	}
}
