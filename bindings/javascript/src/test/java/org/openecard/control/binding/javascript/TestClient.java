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

package org.openecard.control.binding.javascript;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import org.openecard.common.ClientEnv;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.event.EventManager;
import org.openecard.gui.UserConsent;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.recognition.CardRecognition;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a TestClient to test the JavaScriptBinding.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrauzki <petrautzki@hs-coburg.de>
 */
public final class TestClient {

    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

    // Service Access Layer (SAL)
    private TinySAL sal;
    // card states and dispatcher
    private CardStateMap cardStates;
    private Dispatcher dispatcher;
    private org.openecard.common.interfaces.EventManager eventManager;
    private CardRecognition recognition;
    private SwingUserConsent gui;

    public TestClient() {
	try {
	    setup();
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private void setup() throws Exception {
	// Set up client environment
	ClientEnv env = new ClientEnv();

	gui = new SwingUserConsent(new SwingDialogWrapper());

	// Set up the IFD
	IFD ifd = new IFD();
	env.setIFD(ifd);

	// Set up Management
	TinyManagement management = new TinyManagement(env);
	env.setManagement(management);

	// Set up the Dispatcher
	dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);

	byte[] contextHandle = ifd.establishContext(establishContext).getContextHandle();

	recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle);
	this.eventManager = em;
	env.setEventManager(em);

	// Set up SALStateCallback
	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// Set up SAL
	sal = new TinySAL(env, cardStates);
	env.setSAL(sal);

	// Initialize the EventManager
	em.initialize();
    }

    public CardStateMap getCardStates() {
	return this.cardStates;
    }

    public Dispatcher getDispatcher() {
	return this.dispatcher;
    }

    public org.openecard.common.interfaces.EventManager getEventManager() {
	return this.eventManager;
    }

    public UserConsent getGUI() {
	return gui;
    }

    public CardRecognition getCardRecognition() {
	return recognition;
    }

}
