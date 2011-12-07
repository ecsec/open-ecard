package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander.Merkel
 */
public class WaitTest {
    private IFD ifd;

    public WaitTest() {
        ifd = new IFD();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testBlockingWait(){
        byte[] ctxHandle = ifd.establishContext(new EstablishContext()).getContextHandle();
        GetStatusResponse statusResp;
        GetStatus gs = new GetStatus();
        gs.setContextHandle(ctxHandle);
        statusResp = ifd.getStatus(gs);
        // wait until first terminal is added without smartcard
        Wait waitReq = new Wait();
        waitReq.getIFDStatus().addAll(statusResp.getIFDStatus());
        waitReq.setContextHandle(ctxHandle);
//        waitReq.setTimeOut(new BigInteger("1000"));
        WaitResponse wr = ifd.wait(waitReq);

        assertTrue("Wait test failed", wr.getIFDEvent().get(0).getSlotStatus().get(0).isCardAvailable() == true);

        // add second terminal without smartcard
        Wait waitReq2 = new Wait();
        waitReq2.getIFDStatus().addAll(wr.getIFDEvent());
        waitReq2.setContextHandle(ctxHandle);
        WaitResponse wr2 = ifd.wait(waitReq2);

        assertTrue("Wait test failed", wr2.getIFDEvent().get(1).getSlotStatus().get(0).isCardAvailable() == false);
        // insert a terminal
        Wait waitReq3 = new Wait();
        waitReq3.getIFDStatus().addAll(wr2.getIFDEvent());
        waitReq3.setContextHandle(ctxHandle);
        WaitResponse wr3 = ifd.wait(waitReq3);

        assertTrue("Wait test failed", wr3.getIFDEvent().size() == 1);
    }

}