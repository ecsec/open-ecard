/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import org.openecard.crypto.tls.proxy.ProxySettingsLoader.Companion.parseExclusionHosts
import org.testng.Assert
import kotlin.test.Test

/**
 *
 * @author Tobias Wich
 */
class HostTest {
	@Test
	fun testPatternCreation() {
		Assert.assertEquals(parseExclusionHosts("").size, 0)
		Assert.assertEquals(parseExclusionHosts("foo;").size, 1)
		Assert.assertEquals(parseExclusionHosts("foo;bar;").size, 2)
		Assert.assertEquals(parseExclusionHosts("foo;bar;;").size, 2)
		Assert.assertEquals(parseExclusionHosts(";foo;bar;").size, 2)
	}

	@Test
	fun testPatternMatch() {
		var p = parseExclusionHosts("*.example.com")[0]
		Assert.assertTrue(p.matcher("foo.example.com").matches())
		Assert.assertTrue(p.matcher("foo.example.com:80").matches())
		Assert.assertFalse(p.matcher("example.com").matches())
		Assert.assertFalse(p.matcher("example.com:80").matches())
		p = parseExclusionHosts("*.example.com:80")[0]
		Assert.assertFalse(p.matcher("foo.example.com").matches())
		Assert.assertFalse(p.matcher("foo.example.com:443").matches())
		Assert.assertTrue(p.matcher("foo.example.com:80").matches())
		p = parseExclusionHosts("*.example.com:*")[0]
		Assert.assertTrue(p.matcher("foo.example.com:443").matches())
		Assert.assertTrue(p.matcher("foo.example.com:80").matches())
		p = parseExclusionHosts("*")[0]
		Assert.assertTrue(p.matcher("foo.example.com:443").matches())
	}
}
