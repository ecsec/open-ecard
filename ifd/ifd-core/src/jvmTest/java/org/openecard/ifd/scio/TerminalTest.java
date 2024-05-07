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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.math.BigInteger;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Tobias Wich
 */
public class TerminalTest {

    private IFD ifd = null;
    private byte[] ctxHandle;
    private String ifdName;
    private byte[] slotHandle;


    public void init() {
	ClientEnv env = new ClientEnv();
	env.setGUI(new SwingUserConsent(new SwingDialogWrapper()));

	ifd = new IFD();
	ifd.setEnvironment(env);

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
