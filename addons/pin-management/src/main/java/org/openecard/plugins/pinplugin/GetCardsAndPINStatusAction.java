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

package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.common.DynamicContext;
import org.openecard.common.WSHelper.WSException;
import org.openecard.plugins.pinplugin.gui.PINDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class GetCardsAndPINStatusAction extends AbstractPINAction {

    private static final Logger LOG = LoggerFactory.getLogger(GetCardsAndPINStatusAction.class);

    public static final String DYNCTX_INSTANCE_KEY = "GetCardsAndPINStatusAction";

    public static final String PIN_STATUS = "pin-status";
    public static final String PIN_CORRECT = "pin-correct";
    public static final String CAN_CORRECT = "can-correct";
    public static final String PUK_CORRECT = "puk-correct";


    @Override
    public void execute() {
	// init dyn ctx
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);

	try {
	    // check if a german identity card is inserted, if not wait for it
	    ConnectionHandleType cHandle = waitForCardType(GERMAN_IDENTITY_CARD);

	    if (cHandle == null) {
		LOG.debug("User cancelled card insertion.");
		return;
	    }

	    cHandle = connectToRootApplication(cHandle);

	    RecognizedState pinState = recognizeState(cHandle);
	    ctx.put(PIN_STATUS, pinState);

	    boolean nativePace;
	    try {
		nativePace = genericPACESupport(cHandle);
	    } catch (WSException e) {
		LOG.error("Could not get capabilities from reader.");
		return;
	    }

	    PINDialog uc = new PINDialog(gui, dispatcher, cHandle, pinState, !nativePace);
	    uc.show();

	    Disconnect d = new Disconnect();
	    d.setSlotHandle(cHandle.getSlotHandle());
	    dispatcher.safeDeliver(d);
	} finally {
	    ctx.clear();
	}
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	dispatcher = aCtx.getDispatcher();
	this.gui = aCtx.getUserConsent();
	this.recognition = aCtx.getRecognition();
	this.cardStates = aCtx.getCardStates();
	this.evDispatcher = aCtx.getEventDispatcher();
    }

    @Override
    public void destroy() {
	//ignore
    }

}
