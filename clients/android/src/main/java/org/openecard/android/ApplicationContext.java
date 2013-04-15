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

package org.openecard.android;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.preference.PreferenceManager;
import de.bund.bsi.ecard.api._1.TerminateFramework;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.File;
import org.openecard.android.activities.DeviceOpenActivity;
import org.openecard.android.activities.IntentHandlerActivity;
import org.openecard.android.activities.NFCErrorActivity;
import org.openecard.android.activities.TerminalFactoryActivity;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.enums.EventType;
import org.openecard.common.ifd.AndroidTerminalFactory;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.control.ControlInterface;
import org.openecard.control.binding.intent.IntentBinding;
import org.openecard.control.binding.intent.handler.IntentTCTokenHandler;
import org.openecard.control.handler.ControlHandler;
import org.openecard.control.handler.ControlHandlers;
import org.openecard.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.event.EventManager;
import org.openecard.gui.UserConsent;
import org.openecard.gui.android.AndroidUserConsent;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.IFDProperties;
import org.openecard.ifd.scio.wrapper.IFDTerminalFactory;
import org.openecard.management.TinyManagement;
import org.openecard.plugins.manager.PluginManager;
import org.openecard.plugins.pinplugin.PINPlugin;
import org.openecard.recognition.CardRecognition;
import org.openecard.sal.TinySAL;
import org.openecard.sal.protocol.eac.EAC2ProtocolFactory;
import org.openecard.sal.protocol.eac.EACGenericProtocolFactory;
import org.openecard.sal.protocol.genericcryptography.GenericCryptoProtocolFactory;
import org.openecard.sal.protocol.pincompare.PINCompareProtocolFactory;
import org.openecard.scio.NFCFactory;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.openecard.ws.marshal.WsdefProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is instantiated when the process of this application is created.
 * Therefore the global application state is maintained here.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ApplicationContext extends Application implements EventCallback {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    private final I18n lang = I18n.getTranslation("android");

    private static final String SDCARD_OPENECARD = "/sdcard/.openecard/";
    private static final int NOTIFICATION_ID = 22;

    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    private EventManager em;
    private TinyManagement management;
    private byte[] contextHandle;
    private Dispatcher dispatcher;
    private boolean initialized;
    private boolean recognizeCard = true;
    private UserConsent gui;
    private AndroidTerminalFactory terminalFactory;
    private boolean usingNFC;
    private NotificationManager notificationManager;

    public boolean usingNFC() {
	return usingNFC;
    }

    public boolean isInitialized() {
	return initialized;
    }

    public CardRecognition getRecognition() {
	return recognition;
    }

    public CardStateMap getCardStates() {
	return cardStates;
    }

    public ClientEnv getEnv() {
	return env;
    }

    public UserConsent getGUI() {
	return gui;
    }

    /**
     * Shut down the whole client by shutting down components.
     */
    public void shutdown() {
	// destroy EventManager
	try {
	    if (em != null) {
		em.terminate();
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying EventManager.", ex);
	} finally {
	    em = null;
	    recognition = null;
	}
	// destroy Management
	try {
	    if (management != null) {
		TerminateFramework terminateFramework = new TerminateFramework();
		management.terminateFramework(terminateFramework);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying Management.", ex);
	} finally {
	    management = null;
	}
	// destroy SAL
	try {
	    if (sal != null) {
		Terminate terminate = new Terminate();
		sal.terminate(terminate);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying SAL.", ex);
	} finally {
	    sal = null;
	    cardStates = null;
	}
	// destroy IFD
	try {
	    if (ifd != null) {
		ReleaseContext releaseContext = new ReleaseContext();
		releaseContext.setContextHandle(contextHandle);
		ifd.releaseContext(releaseContext);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying IFD.", ex);
	} finally {
	    ifd = null;
	    contextHandle = null;
	}
	// destroy TerminalFactory
	try {
	    if (terminalFactory != null) {
		terminalFactory.stop();
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying TerminalFactory.", ex);
	} finally {
	    terminalFactory = null;
	}
	// destroy the remaining components
	env = null;

	Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	File f = new File(SDCARD_OPENECARD);
	Uri uri = Uri.fromFile(f);
	intent.setData(uri);
	sendBroadcast(intent);

	// let the DeviceOpenActivity be the only Activity on the ActivityStack on the next start
	Intent i = new Intent(this, DeviceOpenActivity.class);
	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
	i.putExtra(AndroidUtils.EXIT, true);
	startActivity(i);
    }

    /**
     * Initialize the client by setting properties for Android and starting up each module.
     */
    public void initialize() {
	if (initialized) {
	    return;	
	}

	notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 

	// load logging config
	AndroidUtils.initLogging(this);

	// read factory out of preferences
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	String noFactory = "";
	String factoryImpl = preferences.getString("org.openecard.ifd.scio.factory.impl", noFactory);

	// if there was no factory set, start the activity to set one and return
	if (factoryImpl.equals(noFactory)) {
	    Intent i = new Intent(this, TerminalFactoryActivity.class);
	    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    i.putExtra("firstStart", true);
	    this.startActivity(i);
	    return;
	}

	IFDProperties.setProperty("org.openecard.ifd.scio.factory.impl", factoryImpl);

	WsdefProperties.setProperty("org.openecard.ws.marshaller.impl", "org.openecard.ws.android.AndroidMarshaller");

	try {
	    terminalFactory = (AndroidTerminalFactory) IFDTerminalFactory.getInstance();
	} catch (IFDException e) {
	    //TODO log
	    System.exit(0);
	}

	usingNFC = terminalFactory instanceof NFCFactory;
	if (usingNFC) {
	    NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
	    NfcAdapter adapter = manager.getDefaultAdapter();
	    if (adapter == null || !adapter.isEnabled()) {
		Intent i = new Intent(this, NFCErrorActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		this.startActivity(i);
		return;
	    }
	}

	terminalFactory.start(this);

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
	em.registerAllEvents(this);

	// SAL
	sal = new TinySAL(env, cardStates);
	sal.setGUI(gui);
	sal.addProtocol(ECardConstants.Protocol.EAC2, new EAC2ProtocolFactory());
	sal.addProtocol(ECardConstants.Protocol.EAC_GENERIC, new EACGenericProtocolFactory());
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

	    IntentHandlerActivity.setHandlers(binding.getHandlers());
	} catch (Exception e) {
	    System.exit(0);
	}

	// Set up PluginManager
	PluginManager pm = new PluginManager(dispatcher, gui, recognition, cardStates, null);
	pm.addPlugin(new PINPlugin());

	initialized = true;
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_RECOGNIZED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		String cardType = ch.getRecognitionInfo().getCardType();
		String cardName = recognition.getTranslatedCardName(cardType);
		showNotification(lang.translationForKey("android.notification.card_recognized", cardName));
	    }
	} else if (eventType.equals(EventType.CARD_REMOVED)) {
	    showNotification(lang.translationForKey("android.notification.card_removed"));
	} else if (eventType.equals(EventType.TERMINAL_ADDED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		showNotification(lang.translationForKey("android.notification.terminal_added", ch.getIFDName()));
	    }
	} else if (eventType.equals(EventType.TERMINAL_REMOVED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		showNotification(lang.translationForKey("android.notification.terminal_removed", ch.getIFDName()));
	    }
	}
    }

    private void showNotification(String message) {
	long currentTime = System.currentTimeMillis();
	Notification notification = new Notification(android.R.drawable.stat_notify_sync, message, currentTime);
	notification.flags = Notification.FLAG_AUTO_CANCEL;
	notification.setLatestEventInfo(this, "Open eCard App", "", null);
	notificationManager.notify(NOTIFICATION_ID, notification);
	try {
	    // wait 2 secs before closing the notification, else it won't be seen on tablets
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    // ignore
	}
	notificationManager.cancel(NOTIFICATION_ID);
    }

}
