/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

package org.openecard.control.binding.http;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import java.io.InputStream;
import org.openecard.addon.AddonManager;
import org.openecard.common.ClientEnv;
import org.openecard.common.event.EventDispatcherImpl;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.recognition.CardRecognitionImpl;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a TestClient to test the HttpBinding.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
public final class TestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

    // Service Access Layer (SAL)
    private TinySAL sal;

    public TestClient() {
	try {
	    setup();
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
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

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);

	byte[] contextHandle = ifd.establishContext(establishContext).getContextHandle();

	final CardRecognitionImpl recognition = new CardRecognitionImpl(env);
	env.setRecognition(recognition);

	env.setCIFProvider(new CIFProvider() {
	    @Override
	    public CardInfoType getCardInfo(ConnectionHandleType type, String cardType) {
		return recognition.getCardInfo(cardType);
	    }
	    @Override
	    public boolean needsRecognition(byte[] atr) {
		return true;
	    }
            @Override
            public CardInfoType getCardInfo(String cardType) throws RuntimeException {
                return recognition.getCardInfo(cardType);
            }
            @Override
            public InputStream getCardImage(String cardType) {
                return recognition.getCardImage(cardType);
            }
	});

	// Set up EventManager
	EventDispatcher ed = new EventDispatcherImpl();
	env.setEventDispatcher(ed);

	// Set up SALStateCallback
	// TODO: fix tests
//	cardStates = new CardStateMap();
//	SALStateCallback salCallback = new SALStateCallback(env, cardStates);
//	ed.add(salCallback);
//
//	// Set up SAL
//	sal = new TinySAL(env, cardStates);
//	env.setSAL(sal);
//
//	// Set up GUI
//	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
//	sal.setGUI(gui);
//	ifd.setGUI(gui);
//
//	// Initialize the EventManager
//	ed.start();
//
//	AddonManager manager = new AddonManager(env, gui, null);
//	sal.setAddonManager(manager);
//
//	HttpBinding binding = new HttpBinding(24727);
//	binding.setAddonManager(manager);
//	binding.start();
    }

}
