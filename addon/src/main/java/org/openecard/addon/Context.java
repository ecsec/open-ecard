/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon;

import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventManager;
import org.openecard.common.interfaces.ProtocolInfo;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Context {

    private AddonProperties _unnamed_AddonProperties_;
    private Dispatcher dispatcher;
    private UserConsent userConsent;

    private CardStateMap cardStates;
    private CardRecognition recognition;
    private EventManager eventManager;
    private ProtocolInfo protocolInfo;
    private EventHandler eventHandler;

    public Context(Dispatcher dispatcher, UserConsent userConsent, CardStateMap cardStates, CardRecognition recognition, EventManager eventManager, ProtocolInfo info, EventHandler eventHandler) {
	this.dispatcher = dispatcher;
	this.userConsent = userConsent;
	this.cardStates = cardStates;
	this.recognition = recognition;
	this.eventManager = eventManager;
	this.protocolInfo = info;
	this.eventHandler = eventHandler;
    }

    public Dispatcher getDispatcher() {
	return dispatcher;
    }

    public EventManager getEventManager() {
	return eventManager;
    }

    public UserConsent getUserConsent() {
	return userConsent;
    }

    public AddonProperties getAddonProperties() {
	throw new UnsupportedOperationException();
    }

    public AddonProperties getActionProperties() {
	throw new UnsupportedOperationException();
    }

    public CardStateMap getCardStates() {
	return cardStates;
    }

    public CardRecognition getRecognition() {
	return recognition;
    }

    public ProtocolInfo getProtocolInfo() {
	return protocolInfo;
    }

    public EventHandler getEventHandler() {
	return eventHandler;
    }

}
