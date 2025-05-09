/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

import org.openecard.common.util.DomainUtils.checkWildcardHostName
import org.testng.Assert
import org.testng.annotations.Test
import java.net.IDN

/**
 *
 * @author Tobias Wich
 */
class DomainUtilsTest {
	@Test
	fun compareWildcardDomains() {
		Assert.assertTrue(checkWildcardHostName("foö.bär.com", "foö.bär.com"))
		Assert.assertTrue(checkWildcardHostName("foö.bär.com", IDN.toASCII("foö.bär.com")))
		Assert.assertTrue(checkWildcardHostName("*.bär.com", "foö.bär.com"))
		Assert.assertFalse(checkWildcardHostName("*.bär.com", "bar.com"))
		Assert.assertFalse(checkWildcardHostName("bär.com", "foö.bar.com"))
		Assert.assertFalse(checkWildcardHostName("*.com", "foö.bar.com"))
	}
}
