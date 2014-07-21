/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.sal.CredentialManager;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventManager;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class Context {

    private final AddonProperties addonProperties;
    private final AddonManager manager;
    private final Dispatcher dispatcher;
    private final EventManager eventManager;
    private final String id;

    private CredentialManager credMan;
    private UserConsent userConsent;
    private CardStateMap cardStates;
    private CardRecognition recognition;
    private EventHandler eventHandler;

    public Context(AddonManager manager, Dispatcher dispatcher, EventManager eventManager, AddonSpecification spec) {
	this.manager = manager;
	this.dispatcher = dispatcher;
	this.eventManager = eventManager;
	this.id = spec.getId();
	addonProperties = new AddonProperties(spec);
    }

    public void setCardRecognition(CardRecognition cardRec) {
	recognition = cardRec;
    }

    public void setEventHandle(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
    }

    public void setCardStateMap(CardStateMap cardStates) {
	this.cardStates = cardStates;
	credMan = new CredentialManager(cardStates);
    }

    public void setUserConsent(UserConsent uConsent) {
	userConsent = uConsent;
    }

    public AddonManager getManager() {
	return manager;
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
	return addonProperties;
    }

    /**
     * Gets the card states representing the internal state of the SAL.
     *
     * @return Current card states.
     * @deprecated Because this element leaks SAL internals which are better accessed with the SAL functions. Will be
     *   removed in version 1.2.0.
     */
    @Deprecated
    public CardStateMap getCardStates() {
	return cardStates;
    }

    public CardRecognition getRecognition() {
	return recognition;
    }

    public EventHandler getEventHandler() {
	return eventHandler;
    }

    public CredentialManager getCredentialManager() {
	return credMan;
    }

    public String getId() {
	return id;
    }

}
