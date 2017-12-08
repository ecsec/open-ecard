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

package org.openecard.android.async.tasks;

import android.nfc.Tag;
import android.os.AsyncTask;
import org.openecard.scio.NFCFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


/**
 * This async task forwards the tag, which is received, to the nfc factory.
 *
 * @author Mike Prechtl
 */
public class SetTagTask extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SetTagTask.class);

    private static final int STD_TIMEOUT = 5000;

    private final Tag tagFromIntent;

    public SetTagTask(Tag tag) {
	this.tagFromIntent = tag;
    }

    @Override
    protected Void doInBackground(Void... voids) {
	List<String> terminalNames = NFCFactory.getTerminalNames();
	if (!terminalNames.isEmpty()) {
	    // the corresponding device should support one nfc terminal (the integrated one)
	    NFCFactory.setNFCTag(tagFromIntent, STD_TIMEOUT);
	} else {
	    LOG.warn("No terminal connected...");
	}
	return null;
    }

}
