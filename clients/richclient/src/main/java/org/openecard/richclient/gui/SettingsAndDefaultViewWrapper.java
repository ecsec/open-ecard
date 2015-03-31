/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.richclient.gui;

import org.openecard.addon.AddonManager;
import org.openecard.gui.about.AboutDialog;
import org.openecard.gui.definition.ViewController;
import org.openecard.richclient.gui.manage.ManagementDialog;


/**
 * 
 *
 * @author Hans-Martin Haase
 */
public class SettingsAndDefaultViewWrapper implements ViewController {

    private AddonManager manager;

    @Override
    public void showSettingsUI() {
	ManagementDialog.showDialog(manager);
    }

    @Override
    public void showDefaultViewUI() {
	AboutDialog.showDialog();
    }

    public void setAddonManager(AddonManager manager) {
	this.manager = manager;
    }

}
