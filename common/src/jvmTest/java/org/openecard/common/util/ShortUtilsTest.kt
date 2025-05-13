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

import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Dirk Petrautzki
 */
class ShortUtilsTest {
	@Test
	fun testToByteArray() {
		var result = ShortUtils.toByteArray(Short.MAX_VALUE)
		var expected = byteArrayOf(0x7F, 0xFF.toByte())
		Assert.assertEquals(expected, result)

		result = ShortUtils.toByteArray(Short.MAX_VALUE, true)
		expected = byteArrayOf(0x7F, 0xFF.toByte())
		Assert.assertEquals(expected, result)

		result = ShortUtils.toByteArray(0.toShort(), true)
		expected = byteArrayOf(0x00, 0x00)
		Assert.assertEquals(expected, result)

		result = ShortUtils.toByteArray(0.toShort(), false)
		expected = byteArrayOf(0x00)
		Assert.assertEquals(expected, result)

		// and a real life example
		result = ShortUtils.toByteArray(0x9000.toShort())
		expected = byteArrayOf(0x90.toByte(), 0x00)
		Assert.assertEquals(expected, result)
	}
}
