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

package org.openecard.client.control.binding.intent;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.control.binding.ControlBinding;
import org.openecard.client.control.binding.intent.handler.IntentTCTokenHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.openecard.client.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.recognition.CardRecognition;


/**
 * Implements an Intent binding for the control interface.
 * 
 * @author Dirk Petrautzki  <petrautzki@hs-coburg.de>
 */
public class IntentBinding extends ControlBinding {

    /** The port 24727 according to BSI-TR-03112 is set in the Android client Manifest */
    private CardStateMap cardStates;
    private Dispatcher dispatcher;
    private UserConsent gui;
    private CardRecognition reg;

    /**
     * Creates a new IntentBinding.
     * @param cardStates CardStateMap of the client
     * @param dispatcher dispatcher for sending messages
     * @param gui to show card insertion dialog
     * @param reg to get card information shown in insertion dialog
     */
    public IntentBinding(CardStateMap cardStates, Dispatcher dispatcher, UserConsent gui, CardRecognition reg) {
	this.cardStates = cardStates;
	this.dispatcher = dispatcher;
	this.gui = gui;
	this.reg = reg;
    }

    @Override
    public void start() throws Exception {

	// Add default handlers if none are given
	if (handlers == null || handlers.getControlHandlers().isEmpty()) {
	    handlers = new ControlHandlers();
	    handlers.addControlHandler(new IntentTCTokenHandler(new GenericTCTokenHandler(cardStates, dispatcher, gui,
		    reg)));
	}
    }

    public ControlHandlers getHandlers() {
	return handlers;
    }

    @Override
    public void stop() throws Exception {
	// nothing to do here
    }

}
