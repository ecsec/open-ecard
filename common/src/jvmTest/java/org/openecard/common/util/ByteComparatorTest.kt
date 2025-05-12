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
class ByteComparatorTest {
	@Test
	fun testCompare() {
		val comp = ByteComparator()
		val a = byteArrayOf(0x00, 0x01, 0x02)
		val b = byteArrayOf(0x03, 0x04, 0x05)
		val c = byteArrayOf(0x00, 0x01, 0x02)
		val d = byteArrayOf(0x00)

		Assert.assertTrue(0 > comp.compare(a, b))
		Assert.assertTrue(0 == comp.compare(a, c))
		Assert.assertTrue(0 < comp.compare(b, a))
		Assert.assertTrue(0 < comp.compare(a, d))
		Assert.assertTrue(0 == comp.compare(a, a))
		Assert.assertTrue(0 < comp.compare(a, null))
		Assert.assertTrue(0 > comp.compare(null, a))
	}
}
