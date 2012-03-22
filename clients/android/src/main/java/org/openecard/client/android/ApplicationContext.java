/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.android;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.Sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.OpenecardProperties;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.android.AndroidUserConsent;
import org.openecard.client.ifd.BluetoothConnection;
import org.openecard.client.ifd.SerialConnectionIFD;
import org.openecard.client.ifd.scio.IFDProperties;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.ws.WsdefProperties;
import org.openecard.ws.IFD;
import org.openecard.ws.Management;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.webkit.WebView;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;


/**
 * This class is instantiated when the process of this application is created.
 * Therefore the global application state is maintained here.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ApplicationContext extends Application {

    // load logging config
    static {
	File conf = new File(LogManager.openecardPath + File.separator + LogManager.openecardConfFileName);
	boolean success = false;
	if (conf.isFile()) {
	    try {
		success = LogManager.loadConfig(new FileInputStream(conf));
	    } catch (FileNotFoundException ex) {
		System.err.println("ERROR: Unable to read logging config file '" + conf.getAbsolutePath() + "'.");
	    }
	}
	if (!success) {
	    LogManager.loadOpeneCardDefaultConfig();
	}
    }


	private ClientEnv env;
	private TinySAL sal;
	private IFD ifd;
	private CardRecognition recognition;
	private EventManager em;
	private byte[] ctx;
	private boolean initialized = false;
	private boolean recognizeCard = true;
	private WebView webView;
	private static final Logger _logger = LogManager.getLogger(ApplicationContext.class.getName());
	  
	public ApplicationContext() throws Throwable {
	    _logger.setLevel(Level.WARNING);
	    ConsoleHandler handler = new ConsoleHandler();
	    handler.setLevel(_logger.getLevel());
	    _logger.addHandler(handler);
	    
		OpenecardProperties.setProperty("org.openecard.lang", Locale.getDefault().toString());
		

		IFDProperties.setProperty("org.openecard.ifd.scio.factory.impl",
				"org.openecard.client.scio.NFCFactory");
		WsdefProperties.setProperty("org.openecard.client.ws.marshaller.impl",
				"org.openecard.client.ws.android.AndroidMarshaller");

		
		/* not usable without nfc/ext apdus
		 this.ifd = new org.openecard.client.ifd.scio.IFD();
		ifd.addProtocol(ECardConstants.Protocol.PACE, new
		PACEProtocolFactory());
		*/
		this.ifd = new SerialConnectionIFD(new BluetoothConnection(
				BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
						"60:D8:19:C0:73:EF")));
		this.env = new ClientEnv();
		env.setIFD(ifd);
		EstablishContext ecRequest = new EstablishContext();
		EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
		if (ecResponse.getResult().getResultMajor()
				.equals(ECardConstants.Major.OK)) {
			if (ecResponse.getContextHandle() != null) {
				ctx = ecResponse.getContextHandle();
				initialized = true;
			}
		}

		ListIFDs listIFDs = new ListIFDs();
		listIFDs.setContextHandle(ctx);
		ListIFDsResponse listresp = ifd.listIFDs(listIFDs);
		System.out.println("Listresp: " + listresp.getIFDName().get(0));

		Connect c = new Connect();
		c.setContextHandle(ecResponse.getContextHandle());
		c.setExclusive(false);
		c.setIFDName("SCM Microsystems Inc. SCL011 Contactless Reader 0");
		c.setSlot(new BigInteger("0"));
		ConnectResponse cr = ifd.connect(c);

		ConnectionHandleType ch = new ConnectionHandleType();
		ch.setIFDName("SCM Microsystems Inc. SCL011 Contactless Reader 0");
		ch.setSlotIndex(new BigInteger("0"));
		ch.setSlotHandle(cr.getSlotHandle());
		ch.setContextHandle(ecResponse.getContextHandle());

		if (recognizeCard) {
			try {
				// Always use static Tree
				recognition = new CardRecognition(ifd, ctx);
			} catch (Exception ex) {
				ex.printStackTrace();
				// _logger.logp(Level.SEVERE, this.getClass().getName(),
				// "init()", ex.getMessage(), ex);
				recognition = null;
				initialized = false;
			}
		} else {
			recognition = null;
		}
		
		
		
		em = new EventManager(recognition, env, ctx, null);
		CardStateMap cardStates = new CardStateMap();
		SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
		sal = new TinySAL(env, cardStates);
		sal.setGUI(new AndroidUserConsent(this));
		sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
		em.registerAllEvents(salCallback);
		em.registerAllEvents(new ClientEventCallBack());
		env.setEventManager(em);
		env.setSAL(sal);
		// TODO: SAL only deals with cards, not raw terminals, this call is crap, we need a working event manager
		//salCallback.signalEvent(EventType.TERMINAL_ADDED, ch);
		// Event-Manager doesnt work with Bluetooth-IFD, Wait blocks communication
		// em.initialize();
		Management m = new TinyManagement(env);
		env.setManagement(m);

		Dispatcher d = new MessageDispatcher(env);
		env.setDispatcher(d);

	}

	public byte[] getCTX() {
		return ctx;
	}

	public ClientEnv getEnv() {
		return env;
	}

	public void setWebView(WebView mWebView) {
		this.webView = mWebView;

	}

	public WebView getWebView() {
		return this.webView;
	}
}
