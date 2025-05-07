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
package org.openecard.common.util

import org.openecard.common.util.ExceptionUtils.matchPath
import org.testng.Assert
import org.testng.annotations.Test
import java.security.InvalidAlgorithmParameterException
import java.util.InvalidPropertiesFormatException

/**
 * Test for the ExceptionUtils class.
 *
 * @author Tobias Wich
 */
class ExceptionUtilsTest {
	@Test
	fun testMatchPath() {
		// create random exceptions, nest them and check if the path matches
		val root: Throwable = NullPointerException()
		var ex: Throwable = InvalidAlgorithmParameterException(root)
		ex = InvalidPropertiesFormatException(ex)
		val result =
			matchPath(
				ex,
				NullPointerException::class.java,
				InvalidAlgorithmParameterException::class.java,
			)
		Assert.assertNotNull(result)
		Assert.assertEquals(result, root)
	}
}
