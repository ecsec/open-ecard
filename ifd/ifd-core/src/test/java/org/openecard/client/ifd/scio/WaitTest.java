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

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WaitTest {

    private IFD ifd;

    public WaitTest() {
        ifd = new IFD();
    }


    @Test(enabled=false)
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

        assertTrue(wr.getIFDEvent().get(0).getSlotStatus().get(0).isCardAvailable() == true, "Wait test failed");

        // add second terminal without smartcard
        Wait waitReq2 = new Wait();
        waitReq2.getIFDStatus().addAll(wr.getIFDEvent());
        waitReq2.setContextHandle(ctxHandle);
        WaitResponse wr2 = ifd.wait(waitReq2);

        assertTrue(wr2.getIFDEvent().get(1).getSlotStatus().get(0).isCardAvailable() == false, "Wait test failed");
        // insert a terminal
        Wait waitReq3 = new Wait();
        waitReq3.getIFDStatus().addAll(wr2.getIFDEvent());
        waitReq3.setContextHandle(ctxHandle);
        WaitResponse wr3 = ifd.wait(waitReq3);

        assertTrue(wr3.getIFDEvent().size() == 1, "Wait test failed");
    }

}