/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 *
 * @author Dirk Petrautzki <petrauttzki></petrauttzki>@hs-coburg.de>
 * @author Johannes.Schmoelz
 */
class IntegerUtilsTest {
	@Test
	fun testToByteArray() {
		var expected = byteArrayOf(0x7F.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
		assertEquals(expected, IntegerUtils.toByteArray(Int.MAX_VALUE))

		expected = byteArrayOf(0x00)
		assertEquals(expected, IntegerUtils.toByteArray(0))

		expected = byteArrayOf(0x00, 0x00, 0x00, 0x00)
		assertEquals(expected, IntegerUtils.toByteArray(0, true))

		expected = byteArrayOf(0x00)
		assertEquals(expected, IntegerUtils.toByteArray(0, false))

		expected = byteArrayOf(0x7F.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
		assertEquals(expected, IntegerUtils.toByteArray(Int.MAX_VALUE, true))

		expected = byteArrayOf(0x7F.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
		assertEquals(expected, IntegerUtils.toByteArray(Int.MAX_VALUE, true))

		expected = byteArrayOf(0x01, 0x00, 0x00, 0x00)
		assertEquals(expected, IntegerUtils.toByteArray(8, 1))

		expected = byteArrayOf(0x03, 0x01)
		assertEquals(expected, IntegerUtils.toByteArray(0x61, 5))
	}
}
