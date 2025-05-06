/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import org.openecard.gui.UserConsent
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.testng.annotations.Test
import java.net.MalformedURLException

/**
 *
 * @author René Lottes
 */
class UpdateDialogTest {
	@Test(enabled = false)
	@Throws(MalformedURLException::class, InterruptedException::class)
	fun testUpdateDialog() {
		val dlUrl = "https://www.openecard.org"
		val gui: UserConsent = SwingUserConsent(SwingDialogWrapper())

		val ud = UpdateDialog(gui, dlUrl, true)
		Thread { ud.display() }.start()

		Thread.sleep(500)

		val ud2 = UpdateDialog(gui, dlUrl, false)
		Thread { ud2.display() }.start()

		Thread.sleep(500000)
	}
}
