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

package org.openecard.client.android;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.File;
import java.io.InputStream;
import org.openecard.client.android.activities.MainActivity;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.binding.intent.IntentBinding;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.android.AndroidUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.ifd.scio.IFDProperties;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.scio.ResourceUnpacker;
import org.openecard.client.scio.RootHelper;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.ws.WsdefProperties;
import android.app.Application;
import java.io.IOException;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.control.binding.intent.handler.IntentTCTokenHandler;
import org.openecard.client.control.handler.ControlHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.openecard.client.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.sal.protocol.genericcryptography.GenericCryptoProtocolFactory;
import org.openecard.client.sal.protocol.pincompare.PINCompareProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is instantiated when the process of this application is created.
 * Therefore the global application state is maintained here.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ApplicationContext extends Application {

    Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    static {
	// TODO: load logging config
    }

    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    private EventManager em;
    private TinyManagement management;
    private byte[] contextHandle;
    private Dispatcher dispatcher = null;
    private boolean initialized = false;
    private boolean recognizeCard = true;
    private UserConsent gui;

    @Override
    public void onCreate() {
	super.onCreate();

	InputStream driverInputStream = getResources().openRawResource(R.raw.drivers);
	if (driverInputStream != null) {
	    File f = new File(getFilesDir() + "/drivers");
	    if (f.exists()) {
		deleteDir(f);
	    }
	    try {
		ResourceUnpacker.unpackResources(driverInputStream, this, getFilesDir());
	    } catch (IOException ex) {
		logger.error("Failed to load PCSC drivers.", ex);
		throw new RuntimeException(ex);
	    }
	} else {
	    throw new RuntimeException("Cannot get drivers resource.");
	}
    }

    public static boolean deleteDir(File dir) {
	// recursively remove all files and subdirectories
	if (dir.isDirectory()) {
	    String[] children = dir.list();
	    for (int i = 0; i < children.length; i++) {
		boolean success = deleteDir(new File(dir, children[i]));
		if (!success) {
		    return false;
		}
	    }
	}

	// The directory is now empty so delete it
	return dir.delete();
    }

    public void shutdown() {
	// shutdwon event manager
	em.terminate();

	// shutdown SAL
	Terminate terminate = new Terminate();
	sal.terminate(terminate);

	// shutdown IFD
	ReleaseContext releaseContext = new ReleaseContext();
	releaseContext.setContextHandle(contextHandle);
	ifd.releaseContext(releaseContext);
	RootHelper.killPCSCD();
    }

    public void initialize() {
	// IFDProperties.setProperty("org.openecard.ifd.scio.factory.impl", "org.openecard.client.scio.VarioFactory");
	IFDProperties.setProperty("org.openecard.ifd.scio.factory.impl", "org.openecard.client.scio.AndroidPCSCFactory");
	WsdefProperties.setProperty("org.openecard.client.ws.marshaller.impl",
	    "org.openecard.client.ws.android.AndroidMarshaller");
	RootHelper.startPCSCD(getFilesDir());
	//VarioCardTerminal.setApplicationContext(this);

	// Client environment
	env = new ClientEnv();

	// Management
	management = new TinyManagement(env);
	env.setManagement(management);

	// Dispatcher
	dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);

	// GUI
	gui = new AndroidUserConsent(this);

	// IFD
	ifd = new IFD();
	ifd.setDispatcher(dispatcher);
	ifd.setGUI(gui);
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	env.setIFD(ifd);

	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);
	if (establishContextResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
	    if (establishContextResponse.getContextHandle() != null) {
		contextHandle = establishContextResponse.getContextHandle();
	    } else {
		throw new RuntimeException("Cannot establish context");
	    }
	} else {
	    throw new RuntimeException("Cannot establish context");
	}

	if (recognizeCard) {
	    try {
		// TODO: reactivate remote tree repository as soon as it
		// supports the embedded TLSMarker
		// GetRecognitionTree client = (GetRecognitionTree)
		// WSClassLoader.getClientService(RecognitionProperties.getServiceName(),
		// RecognitionProperties.getServiceAddr());
		recognition = new CardRecognition(ifd, contextHandle);
	    } catch (Exception ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		// logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		initialized = false;
	    }
	}

	// EventManager
	em = new EventManager(recognition, env, contextHandle);
	env.setEventManager(em);

	// CardStateMap
	this.cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// SAL
	sal = new TinySAL(env, cardStates);
	sal.setGUI(gui);
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	sal.addProtocol(ECardConstants.Protocol.PIN_COMPARE, new PINCompareProtocolFactory());
	sal.addProtocol(ECardConstants.Protocol.GENERIC_CRYPTO, new GenericCryptoProtocolFactory());
	env.setSAL(sal);

	em.initialize();

	// Start up control interface
	try {
	    IntentBinding binding = new IntentBinding();
	    ControlHandlers handler = new ControlHandlers();
	    GenericTCTokenHandler genericTCTokenHandler = new GenericTCTokenHandler(cardStates, dispatcher, gui, recognition);
	    ControlHandler tcTokenHandler = new IntentTCTokenHandler(genericTCTokenHandler);
	    handler.addControlHandler(tcTokenHandler);
	    ControlInterface control = new ControlInterface(binding, handler);
	    control.start();

	    MainActivity.setHandlers(binding.getHandlers());
	} catch (Exception e) {
	    System.exit(0);
	}

    }

    public UserConsent getGUI() {
	return gui;
    }

    public CardRecognition getRecognition() {
	return recognition;
    }

    public Environment getEnv() {
	return env;
    }

}
