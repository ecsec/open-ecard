/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.apdu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.IntegerUtils;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardCommandAPDUTest {

    Random rnd = new Random();

    @Test
    public void testLengthCommand() throws IOException {
	CardCommandAPDU apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF);
	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF});

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

	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF});

	apdu.setLE(1);

	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x01});

	apdu.setLE(255);
	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0xFF});

	apdu.setLE(256);
	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00});

	apdu.setLE(257);
	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x01});

	apdu.setLE(65535);
	assertArrayEquals(apdu.toByteArray(), new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF});

	apdu = new CardCommandAPDU((byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xFF, fillBytes(65535));
	apdu.setLE(65535);
	assertEquals(apdu.getLC(), 65535);
	assertEquals(apdu.getLE(), 65535);
    }

    @Test
    public void testBodyParsing() throws IOException {
	CardCommandAPDU apdu = new ReadBinary();

	// Case 2. : |CLA|INS|P1|P2|LE|
	apdu.setBody(new byte[]{(byte) 0xFF});
	assertEquals(apdu.getLE(), 255);
	assertEquals(apdu.getLC(), -1);
	assertNull(apdu.getData());

	// Case 2.1: |CLA|INS|P1|P2|EXTLE|
	apdu.setBody(new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0xFF});
	assertEquals(apdu.getLC(), -1);
	assertEquals(apdu.getLE(), 511);
	assertNull(apdu.getData());

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
