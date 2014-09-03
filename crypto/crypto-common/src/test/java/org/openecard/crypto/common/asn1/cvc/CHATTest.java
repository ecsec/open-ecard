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

package org.openecard.crypto.common.asn1.cvc;

import java.util.Map;
import java.util.TreeMap;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.StringUtils;
import org.openecard.crypto.common.asn1.cvc.CHAT.DataGroup;
import org.openecard.crypto.common.asn1.cvc.CHAT.Role;
import org.openecard.crypto.common.asn1.cvc.CHAT.SpecialFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CHATTest {

    private CHAT chat;
    private byte[] chatBytes;


    @BeforeMethod
    public void setUp() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffb7");
	chat = new CHAT(chatBytes);
    }

    @Test
    public void testParse() throws TLVException {
	CHAT c = new CHAT(StringUtils.toByteArray("7F4C12060904007F0007030102025305000100FA04"));
	assertEquals(Role.AUTHENTICATION_TERMINAL, c.getRole());
	DataGroup[] data = DataGroup.values();
	SpecialFunction[] specialFunctions = SpecialFunction.values();

	// check writeAccess
	for (int i = 16; i < 21; i++) {
	    assertFalse(c.getWriteAccess().get(data[i]));
	}

	// check readAccess
	for (int i = 0; i < 21; i++) {
	    if (i == 1 || (i > 2 && i < 8) || i == 16) {
		assertTrue(c.getReadAccess().get(data[i]));
	    } else {
		assertFalse(c.getReadAccess().get(data[i]));
	    }
	}

	// check special functions
	for (int i = 0; i < 8; i++) {
	    if (i == SpecialFunction.RESTRICTED_IDENTIFICATION.ordinal()) {
		assertTrue(c.getSpecialFunctions().get(specialFunctions[i]));
	    } else {
		assertFalse(c.getSpecialFunctions().get(specialFunctions[i]));
	    }
	}
    }

    @Test
    public void testEncoding() throws TLVException {
	assertEquals(chatBytes, chat.toByteArray());
    }

    @Test(enabled = false)
    public void testtoString() throws TLVException {
	TreeMap<CHAT.DataGroup, Boolean> readAccess = chat.getReadAccess();

	for (Map.Entry<CHAT.DataGroup, Boolean> entry : readAccess.entrySet()) {
	    System.out.println(entry.getKey() + " " + entry.getValue());
	}
    }

    @Test
    public void testWriteAccess() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305000301ffb7");
	chat.setWriteAccess(CHAT.DataGroup.DG17, false);
	chat.setWriteAccess("DG18", false);
	assertEquals(chat.toByteArray(), chatBytes);
    }

    @Test
    public void testReadAccess() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301f0b7");
	chat.setReadAccess(CHAT.DataGroup.DG01, false);
	chat.setReadAccess(CHAT.DataGroup.DG02, false);
	chat.setReadAccess(CHAT.DataGroup.DG03.name(), false);
	chat.setReadAccess("DG04", false);
	assertEquals(chat.toByteArray(), chatBytes);
    }

    @Test
    public void testSpecialFunctions() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffAF");
	chat.setSpecialFunctions(CHAT.SpecialFunction.PRIVILEGED_TERMINAL, true);
	chat.setSpecialFunction("CAN_ALLOWED", false);
	assertEquals(chat.toByteArray(), chatBytes);
    }

}
