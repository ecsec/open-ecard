/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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
package org.openecard.common.apdu

import org.openecard.common.util.StringUtils.toByteArray
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Test if the UPDATE RECORD commands are constructed as expected.
 *
 * @author Dirk Petrautzki
 */
class UpdateRecordTest {
	@Test
	fun test() {
		val updatingData = "UPDATE".toByteArray()
		var updateRecord = UpdateRecord(updatingData)
		var expected = toByteArray("00 DC 01 04 06 55 50 44 41 54 45", true)
		Assert.assertEquals(updateRecord.toByteArray(), expected)

		updateRecord = UpdateRecord(0x02.toByte(), updatingData)
		expected = toByteArray("00 DC 02 04 06 55 50 44 41 54 45", true)
		Assert.assertEquals(updateRecord.toByteArray(), expected)

		updateRecord = UpdateRecord(0x09.toByte(), 0x02.toByte(), updatingData)
		expected = toByteArray("00 DC 02 4C 06 55 50 44 41 54 45", true)
		Assert.assertEquals(updateRecord.toByteArray(), expected)

		updateRecord = UpdateRecord(0x02.toByte(), 12.toShort(), updatingData)
		expected = toByteArray("00 DD 02 04 09 54 0C 53 55 50 44 41 54 45", true)
		Assert.assertEquals(updateRecord.toByteArray(), expected)

		updateRecord = UpdateRecord(0x09.toByte(), 0x02.toByte(), 12.toShort(), updatingData)
		expected = toByteArray("00 DD 02 4C 09 54 0C 53 55 50 44 41 54 45", true)
		Assert.assertEquals(updateRecord.toByteArray(), expected)
	}
}
