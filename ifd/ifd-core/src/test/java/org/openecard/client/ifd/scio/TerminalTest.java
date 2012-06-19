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
import java.math.BigInteger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.gui.swing.SwingUserConsent;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;


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

    @AfterTest
    public void kill() {
	if (ifd != null) {
	    ReleaseContext rCtx = new ReleaseContext();
	    rCtx.setContextHandle(ctxHandle);
	    ifd.releaseContext(rCtx);
	}
	ifd = null;
    }


    @Test(enabled=false)
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


    @Test(enabled=false)
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
