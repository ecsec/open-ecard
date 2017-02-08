/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;
import org.openecard.common.WSHelper.WSException;
import org.openecard.plugins.pinplugin.gui.ChangePINDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action for changing the PIN.
 * 
 * @author Dirk Petrautzki
 */
public class ChangePINAction extends AbstractPINAction {

    private static final Logger LOG = LoggerFactory.getLogger(ChangePINAction.class);

    @Override
    public void execute() {
	// check if a german identity card is inserted, if not wait for it
	ConnectionHandleType cHandle = waitForCardType(GERMAN_IDENTITY_CARD);

	if (cHandle == null) {
	    LOG.debug("User cancelled card insertion.");
	    return;
	}

	cHandle = connectToRootApplication(cHandle);

	RecognizedState pinState = recognizeState(cHandle);
	boolean nativePace;
	try {
	    nativePace = genericPACESupport(cHandle);
	} catch (WSException e) {
	    LOG.error("Could not get capabilities from reader.");
	    return;
	}

	ChangePINDialog uc = new ChangePINDialog(gui, dispatcher, cHandle, pinState, !nativePace);
	uc.show();

	Disconnect d = new Disconnect();
	d.setSlotHandle(cHandle.getSlotHandle());
	dispatcher.safeDeliver(d);
    }

    @Override
    public void init(Context ctx) throws ActionInitializationException {
	this.dispatcher = ctx.getDispatcher();
	this.gui = ctx.getUserConsent();
	this.recognition = ctx.getRecognition();
	this.cardStates = ctx.getCardStates();
	this.evDispatcher = ctx.getEventDispatcher();
    }

    @Override
    public void destroy() {
	// ignore
    }

}
