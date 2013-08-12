/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import java.lang.reflect.InvocationTargetException;
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.plugins.pinplugin.gui.UnblockPINDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action for unblocking the PIN.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class UnblockPINAction extends AbstractPINAction {

    private static final Logger logger = LoggerFactory.getLogger(UnblockPINAction.class);

    @Override
    public void execute() {
	try {
	    // check if a german identity card is inserted, if not wait for it
	    ConnectionHandleType cHandle = waitForCardType(GERMAN_IDENTITY_CARD);

	    if (cHandle == null) {
		logger.debug("User cancelled card insertion.");
		return;
	    }

	    cHandle = connectToRootApplication(cHandle);

	    RecognizedState pinState = recognizeState(cHandle);
	    boolean nativePace;
	    try {
		nativePace = genericPACESupport(cHandle);
	    } catch (WSException e) {
		logger.error("Could not get capabilities from reader.");
		return;
	    }
	    UnblockPINDialog uc = new UnblockPINDialog(gui, dispatcher, cHandle, pinState, !nativePace);
	    uc.show();

	    Disconnect d = new Disconnect();
	    d.setSlotHandle(cHandle.getSlotHandle());
	    dispatcher.deliver(d);
	} catch (InvocationTargetException e) {
	    logger.error("Failed to execute Action.", e);
	} catch (DispatcherException e) {
	    logger.error("Failed to execute Action.", e);
	}
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	this.dispatcher = aCtx.getDispatcher();
	this.gui = aCtx.getUserConsent();
	this.recognition = aCtx.getRecognition();
	this.cardStates = aCtx.getCardStates();
    }

    @Override
    public void destroy() {
	// ignore
    }

}
