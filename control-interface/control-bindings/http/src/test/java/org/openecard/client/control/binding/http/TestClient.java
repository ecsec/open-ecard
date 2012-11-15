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
package org.openecard.client.control.binding.http;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.module.status.EventHandler;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a TestClient to test the HTTPBinding.
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class TestClient {

    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
    // Service Access Layer (SAL)
    private TinySAL sal;
    // card states
    private CardStateMap cardStates;

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

	// Set up the IFD
	IFD ifd = new IFD();
	env.setIFD(ifd);

	// Set up Management
	TinyManagement management = new TinyManagement(env);
	env.setManagement(management);

	// Set up the Dispatcher
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);

	byte[] contextHandle = ifd.establishContext(establishContext).getContextHandle();

	CardRecognition recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle);
	env.setEventManager(em);

	// Set up SALStateCallback
	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// Set up SAL
	sal = new TinySAL(env, cardStates);
	env.setSAL(sal);

	// Set up GUI
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	sal.setGUI(gui);
	ifd.setGUI(gui);

	// Initialize the EventManager
	em.initialize();

	HTTPBinding binding = 
		new HTTPBinding(HTTPBinding.DEFAULT_PORT, cardStates, env.getDispatcher(), new EventHandler(em), gui, recognition);
	ControlInterface control = new ControlInterface(binding);
	control.start();

    }

}
