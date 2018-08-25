/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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

import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.util.Promise;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class CardMonitor implements BackgroundTask, EventCallback {

    private static final Logger LOG = LoggerFactory.getLogger(CardMonitor.class);
    private static final I18n LANG_PIN = I18n.getTranslation("pinplugin");

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
	    LOG.debug("Waiting for card to be removed.");
	    cardRemoved.deref();
	    LOG.debug("Card has been removed.");
	    String title = LANG_PIN.translationForKey(ERROR_TITLE);
	    String desc = LANG_PIN.translationForKey(ERROR_CARD_REMOVED);
	    ErrorStep replacement = new ErrorStep(title, desc);
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    dynCtx.put(EACProtocol.PACE_EXCEPTION, WSHelper.createException(WSHelper.makeResultError(
		    ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Card has been removed.")));
	    return new StepActionResult(StepActionResultStatus.REPEAT, replacement);
	} catch (InterruptedException ex) {
	    LOG.debug("Card has not been removed.");
	    // terminate the current thread
	    throw ex;
	}
    }

    @Override
    public void signalEvent(EventType eventType, EventObject eventData) {
	cardRemoved.deliver(null);
    }

}
