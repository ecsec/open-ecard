/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.lib.async.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import org.openecard.android.lib.intent.binding.IntentBinding;
import org.openecard.common.util.Promise;
import org.openecard.gui.android.eac.EacGuiImpl;
import org.openecard.gui.android.eac.EacGuiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Mike Prechtl
 */
public class WaitForEacGuiTask extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForEacGuiTask.class);

    private final IntentBinding binding;

    public WaitForEacGuiTask(IntentBinding binding) {
	this.binding = binding;
    }

    @Override
    protected Void doInBackground(Void... voids) {
	Promise<EacGuiImpl> eacGui = EacGuiService.getServiceImpl();
	while (eacGui == null || eacGui.derefNonblocking() == null) {
	    LOG.debug("Wait for Eac Gui...");
	    try {
		Thread.sleep(500);
	    } catch (InterruptedException ex) {
		LOG.error("Waiting for card insertion interrupted.", ex);
	    }
	    eacGui = EacGuiService.getServiceImpl();
	}
	LOG.info("Bind Eac Gui Service...");
	Context ctx = binding.getEacActivity().getApplicationContext();
	ServiceConnection con = binding.getEacActivity().getServiceConnection();
	ctx.bindService(new Intent(ctx, EacGuiService.class), con, Context.BIND_AUTO_CREATE);
	return null;
    }
}
