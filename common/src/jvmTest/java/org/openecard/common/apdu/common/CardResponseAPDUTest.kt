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
 */
package org.openecard.common.apdu.common

import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 *
 * @author Moritz Horsch
 */
class CardResponseAPDUTest {
	@Test
	fun testGetSW1() {
		val apdu = byteArrayOf(0x63.toByte(), 0xC2.toByte())
		val instance = CardResponseAPDU(apdu)
		val expResult: Byte = 99
		val result = instance.sW1
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testGetSW2() {
		val apdu = byteArrayOf(0x63.toByte(), 0xC2.toByte())
		val instance = CardResponseAPDU(apdu)
		val expResult = 194.toByte()
		val result = instance.sW2
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testGetSW() {
		val apdu = byteArrayOf(0x63.toByte(), 0xC2.toByte())
		val instance = CardResponseAPDU(apdu)
		val expResult: Short = 25538
		val result = instance.sW
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testGetData() {
		val apdu = byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x90.toByte(), 0x00.toByte())

		assertEquals(
			CardResponseAPDU.getData(apdu),
			byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte()),
		)
	}

	@Test
	fun testGetTrailer() {
		val apdu = byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x90.toByte(), 0x00.toByte())

		assertEquals(CardResponseAPDU.getTrailer(apdu), byteArrayOf(0x90.toByte(), 0x00.toByte()))
	}
}
