/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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

import org.openecard.common.util.StringUtils.emptyToNull
import org.openecard.common.util.StringUtils.isNullOrEmpty
import org.openecard.common.util.StringUtils.nullToEmpty
import org.openecard.common.util.StringUtils.toByteArray
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class StringUtilsTest {
	@Test
	fun testToByteArray() {
		val hex = "00112233"
		val hex2 = "00             11        22 33"
		var array = toByteArray(hex)
		var expected = byteArrayOf(0x00, 0x11, 0x22, 0x33)
		Assert.assertEquals(expected, array)

		array = toByteArray(hex2, true)
		expected = byteArrayOf(0x00, 0x11, 0x22, 0x33)
		Assert.assertEquals(expected, array)

		array = toByteArray(hex, false)
		expected = byteArrayOf(0x00, 0x11, 0x22, 0x33)
		Assert.assertEquals(expected, array)
	}

	@Test
	fun testNullFunctions() {
		Assert.assertTrue(isNullOrEmpty(""))
		Assert.assertTrue(isNullOrEmpty(null))
		Assert.assertFalse(isNullOrEmpty(" "))
		Assert.assertFalse(isNullOrEmpty("foo"))

		Assert.assertNull(emptyToNull(""))
		Assert.assertNull(emptyToNull(null))
		Assert.assertEquals(emptyToNull(" "), " ")
		Assert.assertEquals(emptyToNull("foo"), "foo")

		Assert.assertEquals(nullToEmpty(null), "")
		Assert.assertEquals(nullToEmpty(""), "")
		Assert.assertEquals(nullToEmpty(" "), " ")
		Assert.assertEquals(nullToEmpty("foo"), "foo")
	}
}
