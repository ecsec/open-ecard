/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.richclient;

import ch.qos.logback.core.joran.spi.JoranException;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.IOException;
import java.net.BindException;
import java.util.Timer;
import java.util.TimerTask;
import org.openecard.addon.AddonManager;
import org.openecard.common.AppVersion;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.control.binding.http.HttpBinding;
import org.openecard.common.event.EventDispatcherImpl;
import org.openecard.common.event.EventType;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.recognition.CardRecognitionImpl;
import org.openecard.richclient.gui.AppTray;
import org.openecard.richclient.gui.SettingsAndDefaultViewWrapper;
import org.openecard.mdlw.sal.MiddlewareSAL;
import org.openecard.mdlw.event.MwStateCallback;
import org.openecard.sal.SelectorSAL;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Hans-Martin Haase
 * @author René Lottes
 * @author Tobias Wich
 */
public final class RichClient {

    private static final Logger LOG;
    private static final I18n LANG;

    // Tray icon
    private AppTray tray;
    // Control interface
    private HttpBinding httpBinding;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private SelectorSAL sal;
    // AddonManager
    private AddonManager manager;
    // EventDispatcherImpl
    private EventDispatcherImpl eventDispatcher;
    // Card recognition
    private CardRecognitionImpl recognition;
    // card states
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

    static {
	try {
	    // load logger config from HOME if set
	    LogbackConfig.load();
	} catch (IOException | JoranException ex) {
	    System.err.println("Failed to load logback config from user config.");
	    ex.printStackTrace(System.err);
	    try {
		LogbackConfig.loadDefault();
	    } catch (JoranException ex2) {
		System.err.println("Failed to load logback default config.");
		ex.printStackTrace(System.err);
	    }
	}
	LOG = LoggerFactory.getLogger(RichClient.class.getName());
	LANG = I18n.getTranslation("richclient");
    }


    public static void main(String[] args) {
	RichClient client = new RichClient();
	client.setup();
    }

    public void setup() {
	GUIDefaults.initialize();

	String title = LANG.translationForKey("client.startup.failed.headline", AppVersion.getName());
	String message = null;
	// Set up GUI
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());

	try {
	    tray = new AppTray(this);
	    tray.beginSetup();

	    // Set up client environment
	    env = new ClientEnv();

	    // Set up the Dispatcher
	    MessageDispatcher dispatcher = new MessageDispatcher(env);
	    env.setDispatcher(dispatcher);

	    // Set up EventDispatcherImpl
	    eventDispatcher = new EventDispatcherImpl();
	    env.setEventDispatcher(eventDispatcher);

	    // Set up Management
	    TinyManagement management = new TinyManagement(env);
	    env.setManagement(management);

	    // Set up CardRecognitionImpl
	    recognition = new CardRecognitionImpl(env);
	    recognition.setGUI(gui);
	    env.setRecognition(recognition);

	    // Set up StateCallbacks
	    cardStates = new CardStateMap();
	    SALStateCallback salCallback = new SALStateCallback(env, cardStates);
	    eventDispatcher.add(salCallback);
	    MwStateCallback mwCallback = new MwStateCallback(env, cardStates);
	    eventDispatcher.add(mwCallback);


	    // Set up the IFD
	    ifd = new IFD();
	    ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	    ifd.setGUI(gui);
	    ifd.setEnvironment(env);
	    env.setIFD(ifd);

	    // Set up SAL
	    TinySAL mainSal = new TinySAL(env, cardStates);
	    mainSal.setGUI(gui);

	    sal = new SelectorSAL(mainSal, env);
	    env.setSAL(sal);
	    env.setCIFProvider(sal);

	    MiddlewareSAL mwSal = new MiddlewareSAL(env, cardStates);
	    mwSal.setGui(gui);
	    sal.addSpecializedSAL(mwSal);

	    // Start up control interface
	    SettingsAndDefaultViewWrapper guiWrapper = new SettingsAndDefaultViewWrapper();
	    try {
		manager = new AddonManager(env, gui, cardStates, guiWrapper);
		guiWrapper.setAddonManager(manager);
		mainSal.setAddonManager(manager);

		httpBinding = new HttpBinding(HttpBinding.DEFAULT_PORT);
		httpBinding.setAddonManager(manager);
		httpBinding.start();
	    } catch (BindException e) {
		message = LANG.translationForKey("client.startup.failed.portinuse", AppVersion.getName());
		throw e;
	    }

	    tray.endSetup(env, manager);

	    // Initialize the EventManager
	    eventDispatcher.add(tray.status(),
		    EventType.TERMINAL_ADDED, EventType.TERMINAL_REMOVED,
		    EventType.CARD_INSERTED, EventType.CARD_RECOGNIZED, EventType.CARD_REMOVED);

	    // start event dispatcher
	    eventDispatcher.start();

	    // initialize SAL
	    WSHelper.checkResult(sal.initialize(new Initialize()));

	    // Perform an EstablishContext to get a ContextHandle
	    try {
		EstablishContext establishContext = new EstablishContext();
		EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);
		WSHelper.checkResult(establishContextResponse);
		contextHandle = establishContextResponse.getContextHandle();
	    } catch (WSHelper.WSException ex) {
		message = LANG.translationForKey("client.startup.failed.nocontext");
		throw ex;
	    }

	    // perform GC to bring down originally allocated memory
	    new Timer().schedule(new GCTask(), 5000);

	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);

	    if (message == null || message.isEmpty()) {
		// Add exception message if no custom message is set
		message = e.getMessage();
	    }

	    // Show dialog to the user and shut down the client
	    String msg = String.format("%s%n%n%s", title, message);
	    gui.obtainMessageDialog().showMessageDialog(msg, AppVersion.getName(), DialogType.ERROR_MESSAGE);
	    teardown();
	}
    }

    private static class GCTask extends TimerTask {
	@Override
	public void run() {
	    System.gc();
	    System.runFinalization();
	    System.gc();
	    // repeat every 5 minutes
	    new Timer().schedule(new GCTask(), 5 * 60 * 1000);
	}
    }

    public void teardown() {
	try {
	    eventDispatcher.terminate();

	    // TODO: shutdown addon manager and related components?
	    manager.shutdown();

	    // shutdown control modules
	    if (httpBinding != null) {
		httpBinding.stop();
	    }

	    // shutdown SAL
	    Terminate terminate = new Terminate();
	    sal.terminate(terminate);

	    // shutdown IFD
	    ReleaseContext releaseContext = new ReleaseContext();
	    releaseContext.setContextHandle(contextHandle);
	    ifd.releaseContext(releaseContext);
	} catch (Exception ex) {
	    LOG.error("Failed to stop Richclient.", ex);
	}

	System.exit(0);
    }

}
