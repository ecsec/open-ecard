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

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.webkit.WebView;
import iso.std.iso_iec._24727.tech.schema.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Locale;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.OpenecardProperties;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is instantiated when the process of this application is created.
 * Therefore the global application state is maintained here.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ApplicationContext extends Application {

    static {
	// TODO: load logging config
    }

    	private static final Logger _logger = LoggerFactory.getLogger(ApplicationContext.class);

	private ClientEnv env;
	private TinySAL sal;
	private IFD ifd;
	private CardRecognition recognition;
	private EventManager em;
	private byte[] ctx;
	private boolean initialized = false;
	private boolean recognizeCard = true;
	private WebView webView;
	  
	public ApplicationContext() throws Throwable {
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
