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

package org.openecard.common.apdu.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.openecard.common.apdu.GeneralAuthenticate;
import org.openecard.common.apdu.ReadBinary;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.IntegerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 * @author Moritz Horsch
 */
public class CardCommandAPDUTest {

    Random rnd = new Random();

    @Test
    public void testLengthCommand() throws IOException {
	CardCommandAPDU apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF});

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(1));
	assertEquals(apdu.getLC(), 1);

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(255));
	assertEquals(apdu.getLC(), 255);

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(256));
	assertEquals(apdu.getLC(), 256);

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(65535));
	assertEquals(apdu.getLC(), 65535);
    }

    @Test
    public void testLengthExpected() throws IOException {
	CardCommandAPDU apdu = new ReadBinary();

	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF});

	apdu.setLE(1);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x01});

	apdu.setLE(255);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0xFF});

	apdu.setLE(256);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00});

	apdu.setLE(257);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x01});

	apdu.setLE(65535);
	assertEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF});

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(65535));
	apdu.setLE(65535);
	assertEquals(apdu.getLC(), 65535);
	assertEquals(apdu.getLE(), 65535);

	// test an expected length of 256 with an extended length command apdu
	// the expected LE is {0x01, 0x00}
	apdu.setLE(256);
	int length = apdu.toByteArray().length;
	assertEquals(apdu.toByteArray()[length-2], 0x01);
	assertEquals(apdu.toByteArray()[length-1], 0x00);
    }

    @Test
    public void testBodyParsing() throws IOException {
	CardCommandAPDU apdu = new ReadBinary();

	// Case 2. : |CLA|INS|P1|P2|LE|
	apdu.setBody(new byte[]{(byte) 0xFF});
	assertEquals(apdu.getLE(), 255);
	assertEquals(apdu.getLC(), -1);
	assertEquals(apdu.getData(), new byte[0]);

	// Case 2.1: |CLA|INS|P1|P2|EXTLE|
	apdu.setBody(new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0xFF});
	assertEquals(apdu.getLC(), -1);
	assertEquals(apdu.getLE(), 511);
	assertEquals(apdu.getData(), new byte[0]);

	// Case 3. : |CLA|INS|P1|P2|LC|DATA|
	apdu.setBody(fillBytesWithLength(240));
	assertEquals(apdu.getLC(), 240);
	assertEquals(apdu.getLE(), -1);

	// Case 3.1: |CLA|INS|P1|P2|EXTLC|DATA|
	apdu.setBody(fillBytesWithLength(366));
	assertEquals(apdu.getLC(), 366);
	assertEquals(apdu.getLE(), -1);

	// Case 4. : |CLA|INS|P1|P2|LC|DATA|LE
	apdu.setBody(new byte[]{(byte) 0x01, (byte) 0x01, (byte) 0xFF});
	assertEquals(apdu.getLE(), 255);
	assertEquals(apdu.getLC(), 1);

	// Case 4.1 : |CLA|INS|P1|P2|EXTLC|DATA|LE|
	apdu.setBody(ByteUtils.concatenate(fillBytesWithLength(366), (byte) 0xF0));
	assertEquals(apdu.getLE(), 240);
	assertEquals(apdu.getLC(), 366);

	// Case 4.2 : |CLA|INS|P1|P2|LC|DATA|EXTLE|
	apdu.setBody(new byte[]{(byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xFF});
	assertEquals(apdu.getLE(), 511);
	assertEquals(apdu.getLC(), 1);

	// Case 4.3: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
	apdu.setBody(ByteUtils.concatenate(fillBytesWithLength(366), new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0xFF}));
	assertEquals(apdu.getLE(), 511);
	assertEquals(apdu.getLC(), 366);
    }

    @Test
    public void testAPDUs() {
	CardCommandAPDU apdu = new GeneralAuthenticate();
    }

    @Test
    public void testConstructors() {
	// test constructor CardCommandAPDU(byte[] commandAPDU) with Case 4.2 APDU
	CardCommandAPDU capdu = new CardCommandAPDU(new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xFF});
	assertEquals((byte) 0x00, capdu.getCLA());
	assertEquals((byte) 0xAB, capdu.getINS());
	assertEquals((byte) 0xBC, capdu.getP1());
	assertEquals((byte) 0xDE, capdu.getP2());
	assertEquals((byte) 0x01, capdu.getLC());
	assertEquals(511, capdu.getLE());
	assertEquals(new byte[]{(byte) 0x01}, capdu.getData());
	assertEquals(new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE}, capdu.getHeader());

	// test constructor CardCommandAPDU(byte[] commandAPDU) with Case 1 APDU
	capdu = new CardCommandAPDU(new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE});
	assertEquals((byte) 0x00, capdu.getCLA());
	assertEquals((byte) 0xAB, capdu.getINS());
	assertEquals((byte) 0xBC, capdu.getP1());
	assertEquals((byte) 0xDE, capdu.getP2());
	assertEquals(-1, capdu.getLC());
	assertEquals(-1, capdu.getLE());
	assertEquals(capdu.getData(), new byte[0]);
	assertEquals(new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE}, capdu.getHeader());
    }

    @Test
    public void testGetBody() {
	byte[] apdu = new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xFF};

	Assert.assertEquals(CardCommandAPDU.getBody(apdu), new byte[]{(byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xFF});
    }

    @Test
    public void testGetHeader() {
	byte[] apdu = new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xFF};

	Assert.assertEquals(CardCommandAPDU.getHeader(apdu), new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC, (byte) 0xDE});
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetHeader2() {
	byte[] apdu = new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xBC};

	CardCommandAPDU.getHeader(apdu);
    }

    private byte[] fillBytesWithLength(int i) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	if (i > 255) {
	    baos.write((byte) 0x00);
	}
	baos.write(IntegerUtils.toByteArray(i));

	for (int j = 0; j < i; j++) {
	    baos.write((byte) (rnd.nextInt() & (byte) 0xFF));
	}
	return baos.toByteArray();
    }

    private byte[] fillBytes(int i) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	for (int j = 0; j < i; j++) {
	    baos.write((byte) (rnd.nextInt() & (byte) 0xFF));
	}
	return baos.toByteArray();
    }

}
