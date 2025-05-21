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
package org.openecard.sc.tlv

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 * @author Tobias Wich
 */
@Suppress("ktlint:standard:property-naming")
class TagLengthValueTest {
	@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	@Test
	fun `parse historical bytes DO`() {
		val input = "67424146495345535266ff".hexToUByteArray()
		val (tlv) = input.toTlv(compactTlv = true)
		assertEquals(2, tlv.asList().size)
	}

	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun testCardVerifiableCertificate() {
		/** Certificate Body  */
		val TAG_BODY = 0x7F4EuL

		/** Certificate Profile Identifier  */
		val TAG_CPI = 0x5F29uL

		/** Certification Authority Reference  */
		val TAG_CAR = 0x42uL

		/** Certificate Holder Reference  */
		val TAG_CHR = 0x5F20uL

		val input =
			"7F218201427F4E81FB5F290100420E5A5A4456434141544130303030357F494F060A04007F0007020202020386410470C07FAA329E927D961F490F5430B395EECF3D2A538194D8B637DE0F8ACF60A9031816AC51B594097EB211FB8F55FAA8507D5800EF7B94E024F9630314116C755F200B5A5A444B423230303033557F4C12060904007F0007030102025305000301DF045F25060100000601085F2406010000070001655E732D060904007F00070301030280207C1901932DB75D08539F2D4A27C938F79E69E083C442C068B299D185BC8AFA78732D060904007F0007030103018020BFD2A6A2E4237948D7DCCF7975D71D40F15307AA59F580A48777CBEED093F54B5F3740618F584E4293F75DDE8977311694B69A3ED73BBE43FDAFEC11B7ECF054F84ACB1231615338CE8D6EC332480883E14E0664950F85134290DD716B7C153232BC96"
				.hexToUByteArray()

		val (tlv) = input.toTlv()

		val version =
			tlv.asConstructed
				?.findChildTags(TAG_BODY)
				?.first()
				?.asConstructed
				?.findChildTags(TAG_CPI)
				?.get(0)
				?.asPrimitive
				?.value[0]
		val car =
			tlv.asConstructed
				?.findChildTags(TAG_BODY)
				?.get(0)
				?.asConstructed
				?.findChildTags(TAG_CAR)
				?.get(0)
				?.asPrimitive
				?.value
				?.toByteArray()
				?.toString(Charsets.US_ASCII)
		val chr =
			tlv.asConstructed
				?.findChildTags(TAG_BODY)
				?.get(0)
				?.asConstructed
				?.findChildTags(TAG_CHR)
				?.get(0)
				?.asPrimitive
				?.value
				?.toByteArray()
				?.toString(Charsets.US_ASCII)

		assertEquals(0, version?.toInt())
		assertEquals("ZZDVCAATA00005", car)
		assertEquals("ZZDKB20003U", chr)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testShortClassZeroLength() {
		val input = ubyteArrayOf(0x00u, 0x00u)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(TagClass.UNIVERSAL, t.tag.tagClass)
		assertEquals(0u, t.tag.tagNum)
		assertTrue(t.tag.primitive)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testLongClassZeroLength() {
		val input = ubyteArrayOf(0xFFu, 0x81u, 0x01u, 0x00u)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(TagClass.PRIVATE, t.tag.tagClass)
		assertEquals(0x81u, t.tag.tagNum)
		assertFalse(t.tag.primitive)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testShortLength() {
		val input = ubyteArrayOf(0x00u, 0x01u, 0xFFu)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(1, t.valueLength)
		assertContentEquals(ubyteArrayOf(0xFFu), t.value.v)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testLongLength() {
		val input = ubyteArrayOf(0x00u, 0x81u, 0x01u, 0xFFu)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(1, t.valueLength)
		assertContentEquals(ubyteArrayOf(0xFFu), t.value.v)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testEOCLength() {
		val input = ubyteArrayOf(0x00u, 0x80u, 0xFFu, 0x00u, 0x00u)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(1, t.valueLength)
		assertContentEquals(ubyteArrayOf(0xFFu), t.value.v)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testEOCLengthZero() {
		val input = ubyteArrayOf(0x00u, 0x80u, 0x00u, 0x00u)

		val (t, rawLength) = TagLengthValue.fromBer(input)

		assertEquals(input.size, rawLength)
		assertEquals(0, t.valueLength)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testFromAndToBER() {
		val input = ubyteArrayOf(0x00u, 0x01u, 0xFFu)

		val (t, rawLength) = TagLengthValue.fromBer(input)
		val result = t.toBer()

		assertContentEquals(input, result)
	}

	@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	@Test
	fun testEvalFCP() {
		val input =
			(
				"62 25" +
					"82 01 78" +
					"83 02 3F 00" +
					"84 07 D2 76 00 01 44 80 00" +
					"85 02 B1 26" +
					"8A 01 05" +
					"8B 0A 00 0A 01 08 02 08 03 00 04 00" +
					"A0 00"
			).replace(" ", "").hexToUByteArray()

		val (t) = input.toTlv()

		// perform some checks
		assertNull(t.sibling)
		assertEquals(0, t.asConstructed?.findChildTags(0u)?.size)
		assertEquals(1, t.asConstructed?.findChildTags(0x8Bu)?.size)
		assertEquals(
			0x8BuL,
			t.asConstructed
				?.findChildTags(0x8Bu)
				?.get(0)
				?.tag
				?.tagNumWithClass,
		)
	}

	@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	@Test
	fun testEvalFCPTail() {
		val input = "A000".hexToUByteArray()

		val (t) = input.toTlv()

		assertContentEquals(
			input,
			t.toBer(),
		)
	}

	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun testCreateTLV() {
		val result =
			buildTlv(Tag.forTagNumWithClass(0x7Cu)) {
				primitive(Tag.forTagNumWithClass(0x81u), ubyteArrayOf(0x01u, 0x02u))
			}

		assertContentEquals("7C0481020102".hexToUByteArray(), result.toBer())
	}

	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun testCreateTLVChilds() {
		val result =
			buildTlv(Tag.forTagNumWithClass(0x7Cu)) {
				primitive(Tag.forTagNumWithClass(0x81u), ubyteArrayOf(0x01u, 0x02u))
				primitive(Tag.forTagNumWithClass(0x81u), ubyteArrayOf(0x03u, 0x04u))
			}

		assertContentEquals("7C088102010281020304".hexToUByteArray(), result.toBer())
	}
}
