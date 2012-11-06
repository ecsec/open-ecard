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

package org.openecard.client.control.binding.javascript;

import java.util.Map;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.control.binding.ControlBinding;
import org.openecard.client.control.binding.javascript.handler.JavaScriptStatusHandler;
import org.openecard.client.control.binding.javascript.handler.JavaScriptTCTokenHandler;
import org.openecard.client.control.binding.javascript.handler.JavaScriptWaitForChangeHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.openecard.client.control.module.status.EventHandler;
import org.openecard.client.control.module.status.GenericStatusHandler;
import org.openecard.client.control.module.status.GenericWaitForChangeHandler;
import org.openecard.client.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.recognition.CardRecognition;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class JavaScriptBinding extends ControlBinding {

    private JavaScriptService service;
    private CardStateMap cardStateMap;
    private Dispatcher dispatcher;
    private EventHandler eventHandler;
    private UserConsent gui;
    private CardRecognition reg;

    /**
     * Creates a new JavaScriptBinding.
     * @param cardStateMap CardStateMap of the client
     * @param dispatcher dispatcher for sending messages
     * @param eventHandler to wait for status changes
     * @param gui to show card insertion dialog
     * @param reg to get card information shown in insertion dialog
     */
    public JavaScriptBinding(CardStateMap cardStateMap, Dispatcher dispatcher, EventHandler eventHandler,
	    UserConsent gui, CardRecognition reg) {
	this.cardStateMap = cardStateMap;
	this.dispatcher = dispatcher;
	this.eventHandler = eventHandler;
	this.gui = gui;
	this.reg = reg;
    }

    public Object[] handle(String id, Map data) {
	return service.handle(id, data);
    }

    @Override
    public void start() throws Exception {
	// Add default handlers if none are given
	if (handlers == null || handlers.getControlHandlers().isEmpty()) {
	    handlers = new ControlHandlers();
	    handlers.addControlHandler(new JavaScriptTCTokenHandler(new GenericTCTokenHandler(cardStateMap, dispatcher,
		    gui, reg)));
	    handlers.addControlHandler(new JavaScriptStatusHandler(new GenericStatusHandler(cardStateMap, eventHandler)));
	    handlers.addControlHandler(new JavaScriptWaitForChangeHandler(new GenericWaitForChangeHandler(
		    eventHandler)));
	}

	service = new JavaScriptService(handlers);
	service.start();
    }

    @Override
    public void stop() throws Exception {
	service.interrupt();
    }

}
