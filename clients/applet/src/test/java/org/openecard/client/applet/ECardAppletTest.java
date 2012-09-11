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

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.*;
import java.util.Iterator;
import java.util.List;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


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

    private boolean ctxEstablished;
    private boolean salInitialized;
    private boolean crInitialized;


    @BeforeMethod
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
            cr = new CardRecognition(ifd, ctx);
            crInitialized = true;
            System.out.println("done.");
        } catch (Exception ex) {
            crInitialized = false;
            System.out.println("failed.");
        }

        System.out.print("Create Environment... ");
        env = new ClientEnv();
        System.out.println("done.");

        System.out.print("Create EventManager... ");
        manager = new EventManager(cr, env, ctx);
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
    
    @AfterMethod
    public void tearDown() {
        manager = null;
        sal = null;
        env = null;
        cr = null;
        ctx = null;
        ifd = null;
    }

    @Test(enabled=false)
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
