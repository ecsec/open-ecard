/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
package org.openecard.common.tlv

import org.openecard.common.tlv.TLV.Companion.fromBER
import org.openecard.common.util.StringUtils.toByteArray
import org.testng.Assert
import org.testng.annotations.Test
import java.math.BigInteger

/**
 *
 * @author Tobias Wich
 */
@Suppress("ktlint:standard:property-naming")
class TagLengthValueTest {
	@Test
	fun testCardVerifiableCertificate() {
		/** Certificate Body  */
		val TAG_BODY = 0x7F4E

		/** Certificate Profile Identifier  */
		val TAG_CPI = 0x5F29

		/** Certification Authority Reference  */
		val TAG_CAR = 0x42

		/** Certificate Holder Reference  */
		val TAG_CHR = 0x5F20

		val input =
			BigInteger(
				"7F218201427F4E81FB5F290100420E5A5A4456434141544130303030357F494F060A04007F0007020202020386410470C07FAA329E927D961F490F5430B395EECF3D2A538194D8B637DE0F8ACF60A9031816AC51B594097EB211FB8F55FAA8507D5800EF7B94E024F9630314116C755F200B5A5A444B423230303033557F4C12060904007F0007030102025305000301DF045F25060100000601085F2406010000070001655E732D060904007F00070301030280207C1901932DB75D08539F2D4A27C938F79E69E083C442C068B299D185BC8AFA78732D060904007F0007030103018020BFD2A6A2E4237948D7DCCF7975D71D40F15307AA59F580A48777CBEED093F54B5F3740618F584E4293F75DDE8977311694B69A3ED73BBE43FDAFEC11B7ECF054F84ACB1231615338CE8D6EC332480883E14E0664950F85134290DD716B7C153232BC96",
				16,
			).toByteArray()
		val tlv = fromBER(input)

		val version = tlv.findChildTags(TAG_BODY.toLong())[0].findChildTags(TAG_CPI.toLong())[0].value[0]
		val CAR = String(tlv.findChildTags(TAG_BODY.toLong())[0].findChildTags(TAG_CAR.toLong())[0].value)
		val certificateHolderReference =
			String(
				tlv
					.findChildTags(TAG_BODY.toLong())[0]
					.findChildTags(
						TAG_CHR.toLong(),
					)[0]
					.value,
			)

		Assert.assertEquals(version.toInt(), 0)
		Assert.assertEquals(CAR, "ZZDVCAATA00005")
		Assert.assertEquals(certificateHolderReference, "ZZDKB20003U")
	}

	@Test
	fun testShortClassZeroLength() {
		val input = byteArrayOf(0x00, 0x00)

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(TagClass.UNIVERSAL, t.tagClass)
		Assert.assertEquals(0, t.tagNum)
		Assert.assertTrue(t.isPrimitive)
	}

	@Test
	fun testLongClassZeroLength() {
		val input = byteArrayOf(0xFF.toByte(), 0x81.toByte(), 0x01, 0x00)

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(TagClass.PRIVATE, t.tagClass)
		Assert.assertEquals(0x81, t.tagNum)
		Assert.assertFalse(t.isPrimitive)
	}

	@Test
	fun testShortLength() {
		val input = byteArrayOf(0x00, 0x01, 0xFF.toByte())

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(1, t.valueLength)
		Assert.assertEquals(byteArrayOf(0xFF.toByte()), t.value)
	}

	@Test
	fun testLongLength() {
		val input = byteArrayOf(0x00, 0x81.toByte(), 0x01, 0xFF.toByte())

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(1, t.valueLength)
		Assert.assertEquals(byteArrayOf(0xFF.toByte()), t.value)
	}

	@Test
	fun testEOCLength() {
		val input = byteArrayOf(0x00, 0x80.toByte(), 0xFF.toByte(), 0x00, 0x00)

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(1, t.valueLength)
		Assert.assertEquals(byteArrayOf(0xFF.toByte()), t.value)
	}

	@Test
	fun testEOCLengthZero() {
		val input = byteArrayOf(0x00, 0x80.toByte(), 0x00, 0x00)

		val t = TagLengthValue.fromBER(input)

		Assert.assertEquals(input.size, t.rawLength)
		Assert.assertEquals(0, t.valueLength)
	}

	@Test
	fun testFromAndToBER() {
		val input = byteArrayOf(0x00, 0x01, 0xFF.toByte())

		val t = TagLengthValue.fromBER(input)
		val result = t.toBER()

		input.contentEquals(result)
	}

	@OptIn(ExperimentalStdlibApi::class)
	@Test
	fun testEvalFCP() {
		val inputStr =
			"62 25" +
				"82 01 78" +
				"83 02 3F 00" +
				"84 07 D2 76 00 01 44 80 00" +
				"85 02 B1 26" +
				"8A 01 05" +
				"8B 0A 00 0A 01 08 02 08 03 00 04 00" +
				"A0 00"
		val input = toByteArray(inputStr, true)

		val t = fromBER(input)

		// perform some checks
		Assert.assertNull(t.next)
		Assert.assertTrue(t.findChildTags(0).isEmpty())
		Assert.assertTrue(t.findChildTags(0x8B).size == 1)
		Assert.assertTrue(t.findChildTags(0x8B)[0].tagNumWithClass == 0x8BL)
	}

	@OptIn(ExperimentalStdlibApi::class)
	@Test
	fun testEvalFCPTail() {
		val inputStr = "A0 00"
		val input = toByteArray(inputStr, true)

		val t = fromBER(input)

		Assert.assertEquals(
			t
				.toBER()
				.toHexString()
				.uppercase()
				.replace(" ", ""),
			inputStr.replace(" ", ""),
		)
	}

	@Test
	fun testCreateTLV() {
		val outer = TLV()
		outer.tagNumWithClass = 0x7C
		val inner = TLV()
		inner.tagNumWithClass = 0x81
		inner.value = byteArrayOf(0x01, 0x02)

		outer.child = inner

		val result = outer.toBER()

		Assert.assertEquals(byteArrayOf(0x7C.toByte(), 0x04, 0x81.toByte(), 0x02, 0x01, 0x02), result)
	}

	@Test
	fun testCreateTLVChilds() {
		val outer = TLV()
		outer.tagNumWithClass = 0x7C
		val inner1 = TLV()
		inner1.tagNumWithClass = 0x81
		inner1.value = byteArrayOf(0x01, 0x02)
		val inner2 = TLV()
		inner2.tagNumWithClass = 0x81
		inner2.value = byteArrayOf(0x03, 0x04)

		outer.child = inner1
		inner1.addToEnd(inner2)

		val result = outer.toBER()

		Assert.assertEquals(
			byteArrayOf(
				0x7C.toByte(),
				0x08,
				0x81.toByte(),
				0x02,
				0x01,
				0x02,
				0x81.toByte(),
				0x02,
				0x03,
				0x04,
			),
			result,
		)
	}
}
