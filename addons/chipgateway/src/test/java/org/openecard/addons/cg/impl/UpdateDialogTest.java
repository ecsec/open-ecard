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
 ***************************************************************************/

package org.openecard.addons.cg.impl;

import java.net.MalformedURLException;
import org.openecard.gui.UserConsent;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.testng.annotations.Test;


/**
 *
 * @author René Lottes
 */
public class UpdateDialogTest {

    @Test(enabled = false)
    public void testUpdateDialog() throws MalformedURLException, InterruptedException {
	String dlUrl = "https://www.openecard.org";
	UserConsent gui =  new SwingUserConsent(new SwingDialogWrapper());

	final UpdateDialog ud = new UpdateDialog(gui, dlUrl, true);
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		ud.display();
	    }
	}).start();

	Thread.sleep(500);

	final UpdateDialog ud2 = new UpdateDialog(gui, dlUrl, false);
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		ud2.display();
	    }
	}).start();

	Thread.sleep(500000);
    }

}
