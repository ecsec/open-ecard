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
package org.openecard.common.util

import org.openecard.common.util.ByteUtils.clone
import org.openecard.common.util.ByteUtils.compare
import org.openecard.common.util.ByteUtils.concatenate
import org.openecard.common.util.ByteUtils.copy
import org.openecard.common.util.ByteUtils.cutLeadingNullByte
import org.openecard.common.util.ByteUtils.cutLeadingNullBytes
import org.openecard.common.util.ByteUtils.isBitSet
import org.openecard.common.util.ByteUtils.setBit
import org.openecard.common.util.ByteUtils.toHexString
import org.openecard.common.util.ByteUtils.toInteger
import org.openecard.common.util.ByteUtils.toLong
import org.openecard.common.util.ByteUtils.toShort
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Johannes Schmoelz
 */
class ByteUtilsTest {
	@Test
	fun testClone() {
		val input = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
		val clone = clone(input)

		Assert.assertFalse(clone == input)
		Assert.assertEquals(clone, input)
        /*
         * test null as input
         */
		Assert.assertNull(clone(null))
	}

	@Test
	fun testConcatenate() {
		val a = byteArrayOf(0x00, 0x01, 0x02)
		val b = byteArrayOf(0x03, 0x04, 0x05)
		val c: Byte = 6
		val d: Byte = 7
		var result = concatenate(a, b)
		var expected = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
		Assert.assertEquals(expected, result)
		result = concatenate(a, c)
		expected = byteArrayOf(0x00, 0x01, 0x02, 0x06)
		Assert.assertEquals(expected, result)
		result = concatenate(d, b)
		expected = byteArrayOf(0x07, 0x03, 0x04, 0x05)
		Assert.assertEquals(expected, result)
		result = concatenate(d, c)
		expected = byteArrayOf(0x07, 0x06)
		Assert.assertEquals(expected, result)
		result = concatenate(d, null)
		expected = byteArrayOf(0x07)
		Assert.assertEquals(expected, result)
		result = concatenate(null, d)
		expected = byteArrayOf(0x07)
		Assert.assertEquals(expected, result)
	}

	@Test
	fun testCut() {
		Assert.assertNull(copy(null, 0, 5))
	}

	@Test
	fun testCutLeadingNullByte() {
		var input = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
		val expected = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
		var result = cutLeadingNullByte(input)
		Assert.assertEquals(expected, result)

		// test without leading null byte
		input = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
		result = cutLeadingNullByte(input)
		Assert.assertEquals(expected, result)

		// test null as input
		Assert.assertNull(cutLeadingNullByte(null))
	}

	@Test
	fun testCutLeadingNullBytes() {
		val input = byteArrayOf(0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
		val expected = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
		val result = cutLeadingNullBytes(input)
		Assert.assertEquals(expected, result)
		Assert.assertNull(cutLeadingNullBytes(null))
	}

	@Test
	fun testCopy() {
		val input = byteArrayOf(0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
		val expected = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
		val result = copy(input, 2, 5)
		Assert.assertEquals(expected, result)
		Assert.assertNull(copy(null, 0, 23))
	}

	@Test
	fun testCompare() {
		val a: Byte = 1
		val b: Byte = 1
		val c: Byte = 2
		val d = byteArrayOf(1)
		val e = byteArrayOf(2)
		Assert.assertTrue(compare(a, b))
		Assert.assertFalse(compare(a, c))
		Assert.assertTrue(compare(a, d))
		Assert.assertFalse(compare(a, e))
		Assert.assertFalse(compare(e, a))
	}

	@Test
	fun testtohexString() {
		val testData = ByteArray(20)

		for (i in testData.indices) {
			testData[i] = i.toByte()
		}
		Assert.assertEquals(toHexString(testData), "000102030405060708090A0B0C0D0E0F10111213")
		Assert.assertEquals(
			toHexString(testData, true),
			"0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F 0x10 0x11 0x12 0x13 ",
		)
		Assert.assertEquals(
			toHexString(testData, true, true),
			"0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F \n0x10 0x11 0x12 0x13 ",
		)
		Assert.assertEquals(toHexString(testData, false, true), "000102030405060708090A0B0C0D0E0F\n10111213")
		Assert.assertNull(toHexString(null))
	}

	@Test
	fun testToShort() {
		var input = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
		var s = toShort(input)
		Assert.assertEquals(-1, s.toInt())

		input = byteArrayOf(0x7F.toByte(), 0xFF.toByte())
		s = toShort(input)
		Assert.assertEquals(Short.MAX_VALUE, s)

		input = byteArrayOf(0x80.toByte(), 0x00.toByte())
		s = toShort(input)
		Assert.assertEquals(Short.MIN_VALUE, s)

		input = ByteArray(3)
		try {
			toShort(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}

		input = ByteArray(0)

		try {
			toShort(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}
	}

	@Test
	fun testToInteger() {
		var input = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
		var s = toInteger(input)
		Assert.assertEquals(-1, s)

		input = byteArrayOf(0x7F.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
		s = toInteger(input)
		Assert.assertEquals(Int.MAX_VALUE, s)

		input = byteArrayOf(0x80.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
		s = toInteger(input)
		Assert.assertEquals(Int.MIN_VALUE, s)

		input = ByteArray(5)
		try {
			toInteger(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}

		input = ByteArray(0)
		try {
			toInteger(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}
	}

	@Test
	fun testToLong() {
		var input =
			byteArrayOf(
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
			)
		var s = toLong(input)
		Assert.assertEquals(-1, s)

		input =
			byteArrayOf(
				0x7F.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
			)
		s = toLong(input)
		Assert.assertEquals(Long.MAX_VALUE, s)

		input =
			byteArrayOf(
				0x7F.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFE.toByte(),
			)
		s = toLong(input)
		Assert.assertEquals(Long.MAX_VALUE - 1, s)

		input =
			byteArrayOf(
				0x80.toByte(),
				0x00.toByte(),
				0x00.toByte(),
				0x00.toByte(),
				0x00.toByte(),
				0x00.toByte(),
				0x00.toByte(),
				0x00.toByte(),
			)
		s = toLong(input)
		Assert.assertEquals(Long.MIN_VALUE, s)

		input = ByteArray(9)
		try {
			toLong(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}

		input = ByteArray(0)
		try {
			toLong(input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}
	}

	@Test
	fun testLittleEndian() {
		var input = byteArrayOf(0xFF.toByte(), 0x00.toByte())
		val s = toShort(input, false)
		Assert.assertEquals(s.toInt(), 0x00FF)

		input = byteArrayOf(0xFF.toByte(), 0x11.toByte(), 0x00.toByte())
		val i = toInteger(input, false)
		Assert.assertEquals(i, 0x0011FF)

		input = byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(), 0x9A.toByte(), 0xBC.toByte())
		val l = toLong(input, false)
		Assert.assertEquals(l, 0xBC9A78563412L)
	}

	@Test
	fun testIsBitSet() {
		val input = byteArrayOf(0x00, 0x00, 0x08, 0x00, 0x00)
		Assert.assertTrue(isBitSet(20, input))
		Assert.assertFalse(isBitSet(22, input))

		try {
			isBitSet(99, input)
			Assert.fail("An IndexOutOfBoundsException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}

		try {
			isBitSet(-6, input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}
	}

	@Test
	fun testSetBit() {
		val input = byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x00, 0x01)
		setBit(20, input)
		Assert.assertTrue(isBitSet(20, input))
		Assert.assertTrue(isBitSet(39, input))
		Assert.assertTrue(isBitSet(0, input))
		Assert.assertFalse(isBitSet(22, input))

		try {
			setBit(99, input)
			Assert.fail("An IndexOutOfBoundsException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}

		try {
			setBit(-1, input)
			Assert.fail("An IllegalArgumentException should have been thrown.")
		} catch (e: IllegalArgumentException) {
			// expected
		}
	}

	@Test
	fun testcutLeadingNullBytes() {
		val testData = ByteArray(20)

		for (i in 0..<testData.size - 9) {
			testData[i + 9] = i.toByte()
		}
		Assert.assertEquals(toHexString(cutLeadingNullBytes(testData)), "0102030405060708090A")
	}
}
