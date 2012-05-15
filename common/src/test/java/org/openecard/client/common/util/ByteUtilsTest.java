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

package org.openecard.client.common.util;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ByteUtilsTest {

    @Test
    public void testClone() {
	byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] clone = ByteUtils.clone(input);

	assertFalse(clone == input);
	assertArrayEquals(clone, input);
	/*
	 * test null as input
	 */
	assertNull(ByteUtils.clone(null));
    }

    @Test
    public void testConcatenate() {
	byte[] a = new byte[] { 0x00, 0x01, 0x02 }, b = new byte[] { 0x03, 0x04, 0x05 };
	byte c = 6, d = 7;
	byte[] result = ByteUtils.concatenate(a, b);
	byte[] expected = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	assertArrayEquals(expected, result);
	result = ByteUtils.concatenate(a, c);
	expected = new byte[] { 0x00, 0x01, 0x02, 0x06 };
	assertArrayEquals(expected, result);
	result = ByteUtils.concatenate(d, b);
	expected = new byte[] { 0x07, 0x03, 0x04, 0x05 };
	assertArrayEquals(expected, result);
	result = ByteUtils.concatenate(d, c);
	expected = new byte[] { 0x07, 0x06 };
	assertArrayEquals(expected, result);
	result = ByteUtils.concatenate(d, null);
	expected = new byte[] { 0x07 };
	assertArrayEquals(expected, result);
	result = ByteUtils.concatenate(null, d);
	expected = new byte[] { 0x07 };
	assertArrayEquals(expected, result);

    }

    @Test
    public void testCut() {
	assertNull(ByteUtils.cut(null, 0, 5));
    }

    @Test
    public void testCutLeadingNullByte() {
	byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] expected = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] result = ByteUtils.cutLeadingNullByte(input);
	assertArrayEquals(expected, result);

	/*
	 * test without leading null byte
	 */
	input = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
	result = ByteUtils.cutLeadingNullByte(input);
	assertArrayEquals(expected, result);

	/*
	 * test null as input
	 */
	assertNull(ByteUtils.cutLeadingNullByte(null));
    }

    @Test
    public void testCutLeadingNullBytes() {
	byte[] input = new byte[] { 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] expected = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] result = ByteUtils.cutLeadingNullBytes(input);
	assertArrayEquals(expected, result);
	assertNull(ByteUtils.cutLeadingNullBytes(null));
    }

    @Test
    public void testCopy() {
	byte[] input = new byte[] { 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] expected = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
	byte[] result = ByteUtils.copy(input, 2, 5);
	assertArrayEquals(expected, result);
	assertNull(ByteUtils.copy(null, 0, 23));
    }

    @Test
    public void testCompare() {
	byte a = 1, b = 1, c = 2;
	byte[] d = new byte[] { 1 }, e = new byte[] { 2 };
	assertTrue(ByteUtils.compare(a, b));
	assertFalse(ByteUtils.compare(a, c));
	assertTrue(ByteUtils.compare(a, d));
	assertFalse(ByteUtils.compare(a, e));
	assertFalse(ByteUtils.compare(e, a));
    }

    @Test
    public void testtohexString() {
	byte[] testData = new byte[20];

	for (int i = 0; i < testData.length; i++) {
	    testData[i] = (byte) i;
	}
	assertEquals(ByteUtils.toHexString(testData), "000102030405060708090A0B0C0D0E0F10111213");
	assertEquals(ByteUtils.toHexString(testData, true),
		"0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F 0x10 0x11 0x12 0x13 ");
	assertEquals(ByteUtils.toHexString(testData, true, true),
		"0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F \n0x10 0x11 0x12 0x13 ");
	assertEquals(ByteUtils.toHexString(testData, false, true), "000102030405060708090A0B0C0D0E0F\n10111213");
	assertNull(ByteUtils.toHexString(null));
    }

    @Test
    public void testToInteger() {
	byte[] input = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	int s = ByteUtils.toInteger(input);
	assertEquals(-1, s);

	input = new byte[] { (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	s = ByteUtils.toInteger(input);
	assertEquals(Integer.MAX_VALUE, s);

	input = new byte[] { (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	s = ByteUtils.toInteger(input);
	assertEquals(Integer.MIN_VALUE, s);

	input = new byte[5];
	try {
	    s = ByteUtils.toInteger(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	input = new byte[0];

	try {
	    s = ByteUtils.toInteger(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}
    }

    @Test
    public void testToLong() {
	byte[] input = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	long s = ByteUtils.toLong(input);
	assertEquals(-1, s);

	input = new byte[] { (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	s = ByteUtils.toLong(input);
	assertEquals(Long.MAX_VALUE, s);

	input = new byte[] { (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	s = ByteUtils.toLong(input);
	assertEquals(Long.MIN_VALUE, s);

	input = new byte[9];
	try {
	    s = ByteUtils.toLong(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	input = new byte[0];

	try {
	    s = ByteUtils.toLong(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}
    }

    @Test
    public void testToShort() {
	byte[] input = new byte[] { (byte) 0xFF, (byte) 0xFF };
	short s = ByteUtils.toShort(input);
	assertEquals(-1, s);

	input = new byte[] { (byte) 0x7F, (byte) 0xFF };
	s = ByteUtils.toShort(input);
	assertEquals(Short.MAX_VALUE, s);

	input = new byte[] { (byte) 0x80, (byte) 0x00 };
	s = ByteUtils.toShort(input);
	assertEquals(Short.MIN_VALUE, s);

	input = new byte[3];
	try {
	    s = ByteUtils.toShort(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	input = new byte[0];

	try {
	    s = ByteUtils.toShort(input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}
    }

    @Test
    public void testIsBitSet() {
	byte[] input = new byte[] { 0x00, 0x00, 0x08, 0x00, 0x00 };
	assertTrue(ByteUtils.isBitSet(20, input));
	assertFalse(ByteUtils.isBitSet(22, input));

	try {
	    ByteUtils.isBitSet(99, input);
	    fail("An IndexOutOfBoundsException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	try {
	    ByteUtils.isBitSet(-6, input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}
    }

    @Test
    public void testSetBit() {
	byte[] input = new byte[] { (byte) 0x80, 0x00, 0x00, 0x00, 0x01 };
	ByteUtils.setBit(20, input);
	assertTrue(ByteUtils.isBitSet(20, input));
	assertTrue(ByteUtils.isBitSet(39, input));
	assertTrue(ByteUtils.isBitSet(0, input));
	assertFalse(ByteUtils.isBitSet(22, input));

	try {
	    ByteUtils.setBit(99, input);
	    fail("An IndexOutOfBoundsException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	try {
	    ByteUtils.setBit(-1, input);
	    fail("An IllegalArgumentException should have been thrown.");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}

	try {
	    ByteUtils.setBit(5, null);
	    fail("An NullPointerException should have been thrown.");
	} catch (NullPointerException e) {
	    /* expected */
	}

    }

    @Test
    public void testcutLeadingNullBytes() {
	byte[] testData = new byte[20];

	for (int i = 0; i < testData.length - 9; i++) {
	    testData[i+9] = (byte) i;
	}
	assertEquals(ByteUtils.toHexString(ByteUtils.cutLeadingNullBytes(testData)), "0102030405060708090A");
    }

}
