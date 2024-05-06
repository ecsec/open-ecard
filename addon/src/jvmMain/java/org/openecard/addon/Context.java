/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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

import java.util.List;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.sal.SalStateView;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.event.EventDispatcherImpl;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.gui.UserConsent;
import org.openecard.gui.definition.ViewController;


/**
 * This class implements a context object used for the exchange of information with addons.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class Context {

    private final AddonProperties addonProperties;
    private final AddonManager manager;
    private final Environment env;
    private final String id;
    private final ViewController viewController;
    private final SalStateView salStateView;

    private UserConsent userConsent;
    private CardRecognition recognition;
    private EventHandler eventHandler;

    /**
     * Creates a new instance from the given parameters.
     *
     * @param manager {@link AddonManager} to use for this Context object.
     * @param env {@link Environment} to use for this Context object.
     * @param spec {@link AddonSpecification} for the generation of the {@link AddonProperties} object.
     * @param addView {@link ViewController} to use for this Context object.
     * @param salStateView {@link SalStateView} to use for this Context object.
     */
    public Context(AddonManager manager, Environment env, AddonSpecification spec, ViewController addView,
	    SalStateView salStateView) {
	this.manager = manager;
	this.env = env;
	this.salStateView = salStateView;
	this.id = spec.getId();
	addonProperties = new AddonProperties(spec);
	viewController = addView;
    }

    /**
     * Sets the CardRecognition implementation of this Context.
     *
     * @param cardRec The {@link CardRecognition} implementation to set.
     */
    public void setCardRecognition(CardRecognition cardRec) {
	recognition = cardRec;
    }

    /**
     * Sets the EventHandler of for this Context.
     *
     * @param eventHandler The {@link EventHandler} to set.
     */
    public void setEventHandle(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
    }

    /**
     * Sets the UserConsent of this Context.
     *
     * @param uConsent The {@link UserConsent} to set.
     */
    public void setUserConsent(UserConsent uConsent) {
	userConsent = uConsent;
    }

    /**
     * Get the AddonManager of this Context.
     *
     * @return The {@link AddonManager} of this context.
     */
    public AddonManager getManager() {
	return manager;
    }

    /**
     * Get the Dispatcher of this Context.
     *
     * @return The {@link Dispatcher} of this Context.
     */
    public Dispatcher getDispatcher() {
	return env.getDispatcher();
    }

    public List<byte[]> getIfdCtx() {
	return env.getIFDCtx();
    }

    /**
     * Get the EventManager of this Context.
     *
     * @return The {@link EventDispatcherImpl} of this Context.
     */
    public EventDispatcher getEventDispatcher() {
	return env.getEventDispatcher();
    }

    /**
     * Get the UserConsent of this Context.
     *
     * @return The {@link UserConsent} of this Context.
     */
    public UserConsent getUserConsent() {
	return userConsent;
    }

    /**
     * Get the AddonProperties of this Context.
     *
     * @return The {@link AddonProperties} of this Context.
     */
    public AddonProperties getAddonProperties() {
	return addonProperties;
    }

    /**
     * Get the ViewController object of this Context.
     *
     * @return The {@link ViewController} of this Context.
     */
    public ViewController getViewController() {
	return viewController;
    }

    /**
     * Get the CardRecognition implementation of this Context.
     *
     * @return The {@link CardRecognition} implementation of this Context.
     */
    public CardRecognition getRecognition() {
	return recognition;
    }

    /**
     * Get the EventHandler of this Context.
     *
     * @return The {@link EventHandler} of this Context.
     */
    public EventHandler getEventHandler() {
	return eventHandler;
    }

    /**
     * Get the ID of this Context object.
     *
     * @return The ID of the Context object.
     */
    public String getId() {
	return id;
    }

    public SalStateView getSalStateView() {
	return this.salStateView;
    }
}
