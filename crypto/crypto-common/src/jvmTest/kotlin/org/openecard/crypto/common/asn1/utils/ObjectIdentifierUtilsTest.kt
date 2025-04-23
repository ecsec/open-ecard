/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.crypto.common.asn1.utils

import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils.getValue
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils.toByteArray
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils.toString
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Moritz Horsch
 */
class ObjectIdentifierUtilsTest {
	@Test
	fun testToByteArray() {
		val oid = EACObjectIdentifier.id_PACE
		val expResult = byteArrayOf(0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04)
		val result = toByteArray(oid)
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testToString() {
		val oid = byteArrayOf(0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04)
		val expResult = "0.4.0.127.0.7.2.2.4"
		val result = toString(oid)
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testToString2() {
		val oid = byteArrayOf(0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04)
		val expResult = "0.4.0.127.0.7.2.2.4"
		val result = toString(oid)
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testGetValue() {
		val oid = EACObjectIdentifier.id_PACE
		val expResult = byteArrayOf(0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04)
		val result = getValue(oid)
		Assert.assertEquals(expResult, result)
	}

	@Test
	fun testLargeOidValues() {
		val oid = "1.2.3.128"
		Assert.assertEquals(toString(getValue(oid)), oid)
	}
}
