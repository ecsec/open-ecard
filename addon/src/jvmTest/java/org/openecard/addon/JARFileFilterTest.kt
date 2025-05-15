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
package org.openecard.addon

import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

/**
 * This is the Test for the JARFileFilter.
 *
 * @author Dirk Petrautzki
 */
class JARFileFilterTest {
	/**
	 * Test if the filter accepts files with jar ending and rejects other files.
	 */
	@Test
	fun test() {
		val filter = JARFileFilter()
		val a = File("a.jar")
		val b = File("b.JAR")
		val c = File("c.exe")
		Assert.assertTrue(filter.accept(a))
		Assert.assertTrue(filter.accept(b))
		Assert.assertFalse(filter.accept(c))
	}
}
