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

package org.openecard.client.crypto.common.asn1.cvc;

import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.StringUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
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
    public void testEncoding() throws TLVException {
	assertEquals(chatBytes, chat.toByteArray());
    }

    @Test(enabled=false)
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
	assertEquals(chatBytes, chat.toByteArray());
    }

    @Test
    public void testReadAccess() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301f0b7");
	chat.setReadAccess(CHAT.DataGroup.DG01, false);
	chat.setReadAccess(CHAT.DataGroup.DG02, false);
	chat.setReadAccess(CHAT.DataGroup.DG03.name(), false);
	chat.setReadAccess("DG04", false);
	assertEquals(chatBytes, chat.toByteArray());
    }

    @Test
    public void testSpecialFunctions() throws TLVException {
	chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffAF");
	chat.setSpecialFunctions(CHAT.SpecialFunction.PRIVILEGED_TERMINAL, true);
	chat.setSpecialFunctions("CAN_ALLOWED", false);
	assertEquals(chatBytes, chat.toByteArray());
    }

}
