/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
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

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.*;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.event.EventManager;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.recognition.RecognitionProperties;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.ws.WSClassLoader;
import org.openecard.ws.GetRecognitionTree;
import org.openecard.ws.IFD;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ECardAppletTest {
    
    private IFD ifd;
    private CardRecognition cr;
    private ClientEnv env;
    private EventManager manager;
    private TinySAL sal;
    private DummyCallback cb;

    private byte[] ctx;
    private String sessionId;

    private boolean ctxEstablished;
    private boolean salInitialized;
    private boolean crInitialized;


    @Before
    public void setUp() {
        System.out.print("Create IFD... ");
        ifd = new org.openecard.client.ifd.scio.IFD();
        System.out.println("done.");

        System.out.print("Establish context... ");
        ctxEstablished = establishContext();
        if (ctxEstablished) {
            System.out.println("done.");
        } else {
            System.out.println("failed.");
        }

        System.out.print("Create CardRecognition... ");
        try {
            GetRecognitionTree client = (GetRecognitionTree) WSClassLoader.getClientService(RecognitionProperties.getServiceName(), RecognitionProperties.getServiceAddr());
            cr = new CardRecognition(ifd, ctx, client);
            crInitialized = true;
            System.out.println("done.");
        } catch (Exception ex) {
            crInitialized = false;
            System.out.println("failed.");
        }

        System.out.print("Create Environment... ");
        env = new ClientEnv();
        System.out.println("done.");

        System.out.print("Set SessionId... ");
        sessionId = "0123456789";
        System.out.println("done.");

        System.out.print("Create EventManager... ");
        manager = new EventManager(cr, env, ctx, "1234567890");
        System.out.println("done.");

        System.out.print("Create SAL... ");
	CardStateMap cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(cr, cardStates);
        sal = new TinySAL(env, cardStates);
        System.out.println("done.");

        System.out.print("Register SAL for all events... ");
        manager.registerAllEvents(salCallback);
        System.out.println("done.");

        System.out.print("Create DummyCallback... ");
        cb = new DummyCallback();
        System.out.println("done");
        
        System.out.print("Register DummyCallback for all events... ");
        manager.registerAllEvents(cb);
        System.out.println("done.");
        
        System.out.print("Initialize SAL... ");
        salInitialized = initializeSAL();
        if (salInitialized) {
            System.out.println("done.");
        } else {
            System.out.println("failed.");
        }
    }
    
    @After
    public void tearDown() {
        manager = null;
        sal = null;
        sessionId = null;
        env = null;
        cr = null;
        ctx = null;
        ifd = null;
    }

    @Ignore
    @Test
    public void testEventSystem() {
        if (!ctxEstablished) {
            fail("Failed to establish Context.");
        }
        if (!salInitialized) {
            fail("Failed to initialize SAL");
        }
        if (!crInitialized) {
            fail("Failed to initialize CardRecognition.");
        }
        // get connection handles from SAL
        List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
        assertNotNull(cHandles);
        ConnectionHandleType cHandle;
        for (Iterator<ConnectionHandleType> iter = cHandles.iterator(); iter.hasNext(); ) {
            cHandle = iter.next();
            // send terminal added event for every connection handle
            cb.signalEvent(EventType.TERMINAL_ADDED, cHandle);
            if (cHandle.getRecognitionInfo() != null) {
                // send card inserted event for every connection handle containing a recognition info
                cb.signalEvent(EventType.CARD_INSERTED, cHandle);
            }
        }
        
    }

    private boolean establishContext() {
        EstablishContext req = new EstablishContext();
        EstablishContextResponse res = ifd.establishContext(req);
        if (res.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            ctx = res.getContextHandle();
            return true;
        }
        return false;
    }

    private boolean initializeSAL() {
        InitializeResponse initResponse = sal.initialize(new Initialize());
        if (initResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            return true;
        }
        return false;
    }

}
