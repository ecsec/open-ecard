/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.IOException;
import java.net.BindException;
import java.util.List;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import org.openecard.addon.AddonManager;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.common.AppVersion;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.OpenecardProperties;
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
import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import org.openecard.sal.SelectorSAL;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.HttpUtils;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
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
	LOG.info("Starting {} {} ...", AppVersion.getName(), AppVersion.getVersion());

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

            // Set up MiddlewareConfig
	    MiddlewareConfigLoader mwConfigLoader = new MiddlewareConfigLoader();
            List<MiddlewareSALConfig> mwSALConfigs = mwConfigLoader.getMiddlewareSALConfigs();

	    // Set up CardRecognitionImpl
	    recognition = new CardRecognitionImpl(env);
	    recognition.setGUI(gui);
	    env.setRecognition(recognition);

	    // Set up StateCallbacks
	    cardStates = new CardStateMap();
	    SALStateCallback salCallback = new SALStateCallback(env, cardStates);
	    eventDispatcher.add(salCallback);


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

            // Set up Middleware SAL
	    MwStateCallback mwCallback = new MwStateCallback(env, cardStates, mwConfigLoader);
            for (MiddlewareSALConfig mwSALConfig : mwSALConfigs) {
		if (! mwSALConfig.isDisabled()) {
		    MiddlewareSAL mwSal = new MiddlewareSAL(env, cardStates, mwSALConfig, mwCallback);
		    mwSal.setGui(gui);
		    sal.addSpecializedSAL(mwSal);
		}
            }

	    // Start up control interface
	    SettingsAndDefaultViewWrapper guiWrapper = new SettingsAndDefaultViewWrapper();
	    try {
		manager = new AddonManager(env, gui, cardStates, guiWrapper);
		guiWrapper.setAddonManager(manager);
		mainSal.setAddonManager(manager);

		// initialize http binding
		int port = 24727;
		boolean dispatcherMode = false;
		WinReg.HKEY hk = WinReg.HKEY_LOCAL_MACHINE;
		String regPath = "SOFTWARE\\" + OpenecardProperties.getProperty("registry.app_name");
		if (Platform.isWindows()) {
		    LOG.debug("Checking if dispatcher mode should be used.");
		    try {
			if (regKeyExists(hk, regPath, "Dispatcher_Mode")) {
			    String value = Advapi32Util.registryGetStringValue(hk, regPath, "Dispatcher_Mode");
			    dispatcherMode = Boolean.valueOf(value);
			    // let socket chose its port
			    port = 0;
			}
		    } catch (Win32Exception ex) {
			LOG.warn("Failed to read 'Dispatcher_Mode' registry key. Using normal operation mode.", ex);
		    }
		}
		if (! dispatcherMode) {
		    try {
			port = Integer.parseInt(OpenecardProperties.getProperty("http-binding.port"));
		    } catch (NumberFormatException ex) {
			LOG.warn("Error in config file, HTTP binding port is malformed.");
		    }
		}

		// start HTTP server
		httpBinding = new HttpBinding(port);
		httpBinding.setAddonManager(manager);
		httpBinding.start();

		if (dispatcherMode) {
		    long waitTime = getRegInt(hk, regPath, "Retry_Wait_Time", 5000L);
		    long timeout = getRegInt(hk, regPath, "DP_Timeout", 3600000L);
		    // try to register with dispatcher service
		    LOG.debug("Trying to register HTTP binding port with dispatcher service.");
		    final int realPort = httpBinding.getPort();
		    final URL regUrl = new URL("http://127.0.0.1:24727/dp/register");
		    FutureTask ft = new FutureTask(new DispatcherRegistrator(regUrl, realPort, waitTime, timeout), 1);
		    Thread registerThread = new Thread(ft, "Register-Dispatcher-Service");
		    registerThread.setDaemon(true);
		    registerThread.start();
		    // wait until thread is finished
		    ft.get();
		}
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

	} catch (Exception ex) {
	    LOG.error(ex.getMessage(), ex);

	    if (message == null || message.isEmpty()) {
		// Add exception message if no custom message is set
		message = ex.getMessage();
	    }

	    // Show dialog to the user and shut down the client
	    String msg = String.format("%s%n%n%s", title, message);
	    gui.obtainMessageDialog().showMessageDialog(msg, AppVersion.getName(), DialogType.ERROR_MESSAGE);
	    teardown();
	} catch (Throwable ex) {
	    LOG.error("Unexpected error occurred. Exiting client.", ex);
	    System.exit(1);
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
	    if (eventDispatcher != null) {
		eventDispatcher.terminate();
	    }

	    // TODO: shutdown addon manager and related components?
	    if (manager != null) {
		manager.shutdown();
	    }

	    // shutdown control modules
	    if (httpBinding != null) {
		httpBinding.stop();
	    }

	    // shutdown SAL
	    if (sal != null) {
		Terminate terminate = new Terminate();
		sal.terminate(terminate);
	    }

	    // shutdown IFD
	    if (ifd != null && contextHandle != null) {
		ReleaseContext releaseContext = new ReleaseContext();
		releaseContext.setContextHandle(contextHandle);
		ifd.releaseContext(releaseContext);
	    }
	} catch (Exception ex) {
	    LOG.error("Failed to stop Richclient.", ex);
	}

	System.exit(0);
    }


    private static class DispatcherRegistrator implements Runnable {

	private final URL regUrl;
	private final int bindingPort;
	private final long waitTime;
	private final long timeout;

	public DispatcherRegistrator(URL regUrl, int bindingPort, long waitTime, long timeout) {
	    this.regUrl = regUrl;
	    this.bindingPort = bindingPort;
	    this.waitTime = waitTime;
	    this.timeout = timeout;
	}


	@Override
	public void run() {
	    long startTime = System.currentTimeMillis();
	    HttpRequestExecutor exec = new HttpRequestExecutor();
	    HttpContext httpCtx = new BasicHttpContext();

	    do {
		try {
		    int port = regUrl.getPort() == -1 ? regUrl.getDefaultPort() : regUrl.getPort();
		    Socket sock = new Socket(regUrl.getHost(), port);
		    StreamHttpClientConnection con = new StreamHttpClientConnection(sock.getInputStream(), sock.getOutputStream());
		    BasicHttpEntityEnclosingRequest req;
		    req = new BasicHttpEntityEnclosingRequest("POST", regUrl.getFile());
		    // prepare request
		    HttpRequestHelper.setDefaultHeader(req, regUrl);
		    ContentType reqContentType = ContentType.create("application/x-www-form-urlencoded", "UTF-8");
		    String bodyStr = String.format("Port=%d", bindingPort);
		    StringEntity bodyEnt = new StringEntity(bodyStr, reqContentType);
		    req.setEntity(bodyEnt);
		    req.setHeader(bodyEnt.getContentType());
		    req.setHeader("Content-Length", String.valueOf(bodyEnt.getContentLength()));

		    // send request
		    HttpUtils.dumpHttpRequest(LOG, req);
		    HttpResponse response = exec.execute(req, con, httpCtx);
		    HttpUtils.dumpHttpResponse(LOG, response, null);

		    int statusCode = response.getStatusLine().getStatusCode();
		    if (statusCode == 204) {
			return;
		    } else {
			String msg = "Execution of dispatcher registration is not successful (code={}), trying again ...";
			LOG.info(msg, statusCode);
		    }
		} catch (HttpException | IOException | UnsupportedCharsetException ex) {
		    LOG.error("Failed to send dispatcher registration reguest.", ex);
		}

		// terminate in case there is no time left
		long now = System.currentTimeMillis();
		if (now - startTime > timeout) {
		    throw new RuntimeException("Failed to register with dispatcher service in a timely manner.");
		}
		// wait a bit and try again
		try {
		    Thread.sleep(waitTime);
		} catch (InterruptedException ex) {
		    LOG.info("Dispatcher registration interrupted.");
		    return;
		}
	    } while (true);
	}
    };

    private static boolean regKeyExists(WinReg.HKEY hk, String key, String value) {
	return Advapi32Util.registryKeyExists(hk, key)
		&& Advapi32Util.registryValueExists(hk, key, value);
    }

    private static Long getRegInt(WinReg.HKEY hk, String key, String value, @Nullable Long defaultValue) {
	try {
	    if (regKeyExists(hk, key, value)) {
		return (long) Advapi32Util.registryGetIntValue(hk, key, value);
	    }
	} catch (Win32Exception ex) {
	    LOG.debug("Registry key {}\\{} does not exist or has wrong type.", key, value);
	}
	return defaultValue;
    }

}
