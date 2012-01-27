/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WaitTest {

    private IFD ifd;

    public WaitTest() {
        ifd = new IFD();
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