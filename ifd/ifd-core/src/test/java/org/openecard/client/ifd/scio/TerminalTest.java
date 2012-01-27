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
import java.math.BigInteger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.gui.swing.SwingUserConsent;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TerminalTest {

    private IFD ifd = null;
    private byte[] ctxHandle;
    private String ifdName;
    private byte[] slotHandle;


    public void init() {
	ifd = new IFD();
	EstablishContext eCtx = new EstablishContext();
	ctxHandle = ifd.establishContext(eCtx).getContextHandle();

	ListIFDs listIFDs = new ListIFDs();
	listIFDs.setContextHandle(ctxHandle);
	ifdName = ifd.listIFDs(listIFDs).getIFDName().get(0);
    }

    @After
    public void kill() {
	if (ifd != null) {
	    ReleaseContext rCtx = new ReleaseContext();
	    rCtx.setContextHandle(ctxHandle);
	    ifd.releaseContext(rCtx);
	}
	ifd = null;
    }


    @Ignore
    @Test
    public void testTransmit() {
	init();

	Connect con = new Connect();
	con.setContextHandle(ctxHandle);
	con.setIFDName(ifdName);
	con.setSlot(BigInteger.ZERO);
	con.setExclusive(Boolean.FALSE);
	slotHandle = ifd.connect(con).getSlotHandle();

	Transmit t = new Transmit();
	InputAPDUInfoType apdu = new InputAPDUInfoType();
	apdu.getAcceptableStatusCode().add(new byte[] {(byte)0x90, (byte)0x00});
	apdu.setInputAPDU(new byte[] {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x0C});
	t.getInputAPDUInfo().add(apdu);
	t.setSlotHandle(slotHandle);

	TransmitResponse res = ifd.transmit(t);
	assertEquals(ECardConstants.Major.OK, res.getResult().getResultMajor());
    }

    @Ignore
    @Test
    public void testFeatures() {
        init();
        ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));

        Connect con = new Connect();
        con.setContextHandle(ctxHandle);
        con.setIFDName(ifdName);
        con.setSlot(BigInteger.ZERO);
	con.setExclusive(Boolean.FALSE);
	slotHandle = ifd.connect(con).getSlotHandle();

        GetIFDCapabilities cap = new GetIFDCapabilities();
        cap.setContextHandle(ctxHandle);
        cap.setIFDName(ifdName);
        GetIFDCapabilitiesResponse capR = ifd.getIFDCapabilities(cap);
    }

}
