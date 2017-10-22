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
import android.os.AsyncTask;
import org.openecard.android.lib.AppConstants;
import org.openecard.android.lib.AppContext;
import org.openecard.android.lib.activities.EacActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class WaitForCardRecognizedTask extends AsyncTask<Void, Void, Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForCardRecognizedTask.class);

    private final EacActivity activity;

    public WaitForCardRecognizedTask(EacActivity activity) {
	this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
	Context ctx = activity.getApplicationContext();
	if (ctx instanceof AppContext) {
	    AppContext appContext = (AppContext) ctx;
	    boolean isCardAvailable = appContext.isCardAvailable();
	    boolean isNPAInserted = appContext.getCardType().equals(AppConstants.NPA_CARD_TYPE);
	    while (!isCardAvailable && !isNPAInserted) {
		try {
		    Thread.sleep(500);
		    isCardAvailable = appContext.isCardAvailable();
		    isNPAInserted = appContext.getCardType().equals(AppConstants.NPA_CARD_TYPE);
		} catch (InterruptedException ex) {
		    LOG.error("Waiting for card insertion interrupted.", ex);
		}
	    }
	    return Boolean.TRUE;
	}
	return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
	activity.cardRecognized();
    }

}
