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
package org.openecard.binding.tctoken

import generated.TCTokenType
import org.openecard.binding.tctoken.TCTokenHacks.fixPathSecurityParameters
import org.openecard.common.ECardConstants.BINDING_PAOS
import org.openecard.common.util.FileUtils
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.common.util.StringUtils
import org.testng.Assert
import org.testng.annotations.Test

/**
 * @author Moritz Horsch
 */
class TCTokenParserTest {
	@Test(enabled = true)
	@Throws(Exception::class)
	fun testParse() {
		val testFile = resolveResourceAsStream(javaClass, "TCToken.xml")

		val parser = TCTokenParser()
		val tokens: List<TCToken> = parser.parse(testFile!!)

		val t: TCTokenType = tokens.get(0)
		Assert.assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e2751234")
		Assert.assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443")
		Assert.assertEquals(
			t.getRefreshAddress().toString(),
			"https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A",
		)
		Assert.assertEquals(t.getBinding(), BINDING_PAOS)
	}

	@Test
	@Throws(Exception::class)
	fun testParseMalformed() {
		var data = FileUtils.toString(resolveResourceAsStream(javaClass, "TCToken-malformed.xml")!!)

		data = fixPathSecurityParameters(data)

		val parser = TCTokenParser()
		val tokens: List<TCToken> = parser.parse(data)

		val t: TCTokenType = tokens.get(0)
		Assert.assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e275")
		Assert.assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443")
		Assert.assertEquals(
			t.getRefreshAddress().toString(),
			"https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A",
		)
		Assert.assertEquals(t.getBinding(), BINDING_PAOS)
		Assert.assertEquals(
			t.getPathSecurityParameters().getPSK(),
			StringUtils.toByteArray("b7e9dd2ba2568c3c8d572aaadb3eebf7d4515e66d5fc2fd8e46626725a9abba2"),
		)
	}
}
