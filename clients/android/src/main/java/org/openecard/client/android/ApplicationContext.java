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

import android.app.Application;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.event.EventManager;
import org.openecard.client.ifd.scio.IFDProperties;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.ws.WsdefProperties;
import org.openecard.ws.IFD;

/**
 * This class is instantiated when the process of this application is created. 
 * Therefore the global application state is maintained here.
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ApplicationContext extends Application {

	private ClientEnv env;
	private TinySAL sal;
	private IFD ifd;
	private CardRecognition recognition;
	private EventManager em;
	private byte[] ctx;
	private boolean initialized = false;
	private boolean recognizeCard = true;

	public ApplicationContext() {

		IFDProperties.setProperty("org.openecard.ifd.scio.factory.impl", "org.openecard.client.scio.NFCFactory");
		WsdefProperties.setProperty("org.openecard.client.ws.marshaller.impl", "org.openecard.client.ws.android.AndroidMarshaller");
		this.ifd = new org.openecard.client.ifd.scio.IFD();
		this.env = new ClientEnv();
		env.setIFD(ifd);
		EstablishContext ecRequest = new EstablishContext();
		EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
		if (ecResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
			if (ecResponse.getContextHandle() != null) {
				ctx = ecResponse.getContextHandle();
				initialized = true;
			}
		}

		ListIFDs listIFDs = new ListIFDs();
		listIFDs.setContextHandle(ctx);
		ListIFDsResponse listresp = ifd.listIFDs(listIFDs);
		System.out.println("Listresp: " + listresp.getIFDName().get(0));

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

		System.out.println(recognition==null);
		// TODO: revisit session id parameter, might better be set from outside, but perhaps it doesn't matter at all
		em = new EventManager(recognition, env, ctx, ValueGenerators.generateUUID());

		em.registerAllEvents(new ClientEventCallBack());
		env.setEventManager(em);
		em.initialize();
	}
	
	public byte[] getCTX() {
		return ctx;
	}

	public ClientEnv getEnv() {
		return env;
	}

}
