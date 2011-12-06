package org.openecard.client.applet;

import org.junit.Ignore;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Iterator;
import java.util.List;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import org.openecard.client.applet.EnvStub;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.event.EventManager;
import org.openecard.client.sal.MicroSAL;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecard.ws.IFD;
import static org.junit.Assert.*;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ECardAppletTest {
    
    private IFD ifd;
    private CardRecognition cr;
    private EnvStub env;
    private EventManager manager;
    private MicroSAL sal;
    private DummyCallback cb;
    
    private byte[] ctx;
    private String sessionId;
    
    private boolean ctxEstablished;
    private boolean salInitialized;
    private boolean crInitialized;
    
    public ECardAppletTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
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
            cr = new CardRecognition(ifd, ctx);
            crInitialized = true;
            System.out.println("done.");
        } catch (Exception ex) {
            crInitialized = false;
            System.out.println("failed.");
        }

        System.out.print("Create Environment... ");
        env = new EnvStub();
        System.out.println("done.");

        System.out.print("Set SessionId... ");
        sessionId = "0123456789";
        System.out.println("done.");

        System.out.print("Create EventManager... ");
        manager = new EventManager(cr, env, ctx);
        System.out.println("done.");
        
        System.out.print("Create SAL... ");
        sal = new MicroSAL(env);
        System.out.println("done.");
        
        System.out.print("Register SAL for all events... ");
        manager.registerAllEvents(sal);
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
