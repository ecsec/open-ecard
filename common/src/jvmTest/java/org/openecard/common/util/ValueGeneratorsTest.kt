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

import org.openecard.common.util.ValueGenerators.genBase64Session
import org.openecard.common.util.ValueGenerators.genHexSession
import org.openecard.common.util.ValueGenerators.generatePSK
import org.openecard.common.util.ValueGenerators.generateRandomHex
import org.openecard.common.util.ValueGenerators.generateUUID
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Dirk Petrautzki
 */
class ValueGeneratorsTest {
	@Test
	fun testGeneratePSK() {
		val psk = generatePSK()
		Assert.assertEquals(psk.length, 64)
	}

	@Test
	fun testGenHexSession() {
		var session = genHexSession()
		Assert.assertEquals(session.length, 32)
		session = genHexSession(64)
		Assert.assertEquals(session.length, 64)
	}

	@Test
	fun testGenBase64Session() {
		var session = genBase64Session()
		Assert.assertEquals(session.length, 22)
		session = genBase64Session(64)
		Assert.assertEquals(session.length, 43)
	}

	@Test
	fun testGenerateRandomHex() {
		val randomHex = generateRandomHex(40)
		Assert.assertEquals(randomHex.length, 40)
	}

	@Test
	fun testGenerateUUID() {
		val uuid = generateUUID()
		Assert.assertTrue(uuid.startsWith("urn:uuid:"))
		Assert.assertEquals(uuid.length, 45)
	}
}
