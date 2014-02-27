/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.util.concurrent.Callable;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Future performing nothing more than a wait in the IFD.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class WaitFuture implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(WaitFuture.class);

    private final EventManager evtManager;

    public WaitFuture(EventManager evtManager) {
	this.evtManager = evtManager;
    }


    @Override
    public Boolean call() throws Exception {
	Wait wait = new Wait();
	wait.setContextHandle(evtManager.ctx);
	WaitResponse waitResponse = evtManager.env.getIFD().wait(wait);
	try {
	    WSHelper.checkResult(waitResponse);
	    return ! waitResponse.getIFDEvent().isEmpty();
	} catch (WSException ex) {
	    logger.warn(ex.getMessage(), ex);
	    return false;
	}
    }

}
