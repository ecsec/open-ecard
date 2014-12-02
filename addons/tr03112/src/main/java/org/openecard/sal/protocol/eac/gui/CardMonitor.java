/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import org.openecard.common.I18n;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.util.Promise;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class CardMonitor implements BackgroundTask, EventCallback {

    private static final Logger logger = LoggerFactory.getLogger(CardMonitor.class);
    private static final I18n langPin = I18n.getTranslation("pinplugin");

    // Translation constants
    private static final String ERROR_CARD_REMOVED = "action.error.card.removed";
    private static final String ERROR_TITLE = "action.error.title";

    private final Promise<Void> cardRemoved;

    public CardMonitor() {
	this.cardRemoved = new Promise<>();
    }

    @Override
    public StepActionResult call() throws Exception {
	try {
	    logger.debug("Waiting for card to be removed.");
	    cardRemoved.deref();
	    logger.debug("Card has been removed.");
	    String title = langPin.translationForKey(ERROR_TITLE);
	    String desc = langPin.translationForKey(ERROR_CARD_REMOVED);
	    ErrorStep replacement = new ErrorStep(title, desc);
	    return new StepActionResult(StepActionResultStatus.REPEAT, replacement);
	} catch (InterruptedException ex) {
	    logger.debug("Card has not been removed.");
	    // terminate the current thread
	    throw ex;
	}
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	cardRemoved.deliver(null);
    }

}
