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
 ***************************************************************************/

package org.openecard.event;

import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.util.IFDStatusDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventRunner implements Callable<Void> {

    private static final Logger _logger = LoggerFactory.getLogger(EventRunner.class);

    private final EventManager evtManager;

    private List<IFDStatusType> oldStati;
    private Future wait;

    public EventRunner(EventManager evtManager) {
	this.evtManager = evtManager;
	this.oldStati = new ArrayList<IFDStatusType>();
    }


    @Override
    public Void call() throws Exception {
	try {
	    while (true) {
		wait = evtManager.threadPool.submit(new WaitFuture(evtManager));
		try {
		    List<IFDStatusType> newStati = evtManager.ifdStatus();
		    IFDStatusDiff diff = new IFDStatusDiff(oldStati);
		    diff.diff(newStati, true);
		    if (diff.hasChanges()) {
			evtManager.sendEvents(oldStati, diff.result());
		    }
		    oldStati = newStati;
		} catch (WSException ex) {
		    _logger.warn("GetStatus returned with error.", ex);
		}
		// wait for change if it hasn't already happened
		wait.get();
	    }
	} finally {
	    if (wait != null) {
		wait.cancel(true);
	    }
	}
    }

}
