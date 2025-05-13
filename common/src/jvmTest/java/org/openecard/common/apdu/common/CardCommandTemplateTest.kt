/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.math.BigInteger

/**
 *
 * @author Tobias Wich
 */
class CardCommandTemplateTest {
	private lateinit var ctx: Map<String, Any>

	@BeforeClass
	fun init() {
		ctx =
			mutableMapOf(
				"val1" to "00ff",
				"val2" to "1234",
				"tlv" to TLVFunction(),
			)
	}

	@Test
	@Throws(APDUTemplateException::class)
	fun testNoExp() {
		val templateType = CardCallTemplateType()
		templateType.headerTemplate = "00a4020c"
		var t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C")

		templateType.dataTemplate = "00ff"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C0200FF")

		templateType.expectedLength = BigInteger.valueOf(0xff)
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C0200FFFF")
	}

	@Test
	@Throws(APDUTemplateException::class)
	fun testSymbolExp() {
		val templateType = CardCallTemplateType()
		templateType.headerTemplate = "00a4020c"
		templateType.dataTemplate = "{val1}"
		var t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C0200FF")

		templateType.dataTemplate = "ab{val1}"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C03AB00FF")

		templateType.dataTemplate = "{val1}ab"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C0300FFAB")

		templateType.dataTemplate = "ba{val1}ab"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C04BA00FFAB")

		templateType.dataTemplate = "ba{val1}ab{val2}cd"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C07BA00FFAB1234CD")

		templateType.dataTemplate = "ba{val1}{val2}cd"
		t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C06BA00FF1234CD")
	}

	@Test
	@Throws(APDUTemplateException::class)
	fun testSymbolFun() {
		val templateType = CardCallTemplateType()
		templateType.headerTemplate = "00a4020c"
		templateType.dataTemplate = "{tlv 0x01 val1}"
		val t = CardCommandTemplate(templateType)
		Assert.assertEquals(t.evaluate(ctx!!).toHexString(), "00A4020C04010200FF")
	}

	companion object {
		private val EMPTY_CTX = emptyMap<String, Any>()
	}
}
