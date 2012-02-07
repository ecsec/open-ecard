package org.openecard.client.common.util;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author Johannes.Schmoelz
 */
public class NumUtilsTest {

    /**
     * Test of concatenate method, of class Helper.
     */
    @Test
    public void testConcatenate_byteArr_byteArr() {
        byte[] b1 = {(byte) 0x47, (byte) 0x11};
        byte[] b2 = {(byte) 0x08, (byte) 0x15};
        byte[] expResult = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x15};
        byte[] result = ByteUtils.concatenate(b1, b2);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of concatenate method, of class Helper.
     */
    @Test
    public void testConcatenate_byteArr_byte() {
        byte[] b1 = {(byte) 0x12, (byte) 0x34, (byte) 0x56};
        byte b2 = (byte) 0x78;
        byte[] expResult = {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};
        byte[] result = ByteUtils.concatenate(b1, b2);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of convertByteArrayToInt method, of class Helper.
     */
    @Test
    public void testArrayToInt() {
        // 0 = 0x00
        byte[] buffer = {0x00};
        int expResult = 0;
        int result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 255 = 0xFF
        buffer = new byte[]{(byte) 0xFF};
        expResult = 255;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 256 = 0x01, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00};
        expResult = 256;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 65535 = 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF};
        expResult = 65535;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 65536 = 0x01, 0x00, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00};
        expResult = 65536;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 16777215 = 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = 16777215;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 16777216 = 0x01, 0x00, 0x00, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        expResult = 16777216;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // 2147483647 = 0x7F, 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = 2147483647;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
        // -1 = 0xFF, 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = -1;
        result = ByteUtils.toInteger(buffer);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertPosIntToByteArray method, of class Helper.
     */
    @Test
    public void testIntToArray() {
        // 0 = 0x00
        int value = 0;
        byte[] expResult = {0x00};
        byte[] result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 255 = 0xFF
        value = 255;
        expResult = new byte[]{(byte) 0xFF};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 256 = 0x01, 0x00
        value = 256;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 65535 = 0xFF, 0xFF
        value = 65535;
        expResult = new byte[]{(byte) 0xFF, (byte) 0xFF};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 65536 = 0x01, 0x00, 0x00
        value = 65536;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 16777215 = 0xFF, 0xFF, 0xFF
        value = 16777215;
        expResult = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 16777216 = 0x01, 0x00, 0x00, 0x00
        value = 16777216;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
        // 2147483647 = 0x7F, 0xFF, 0xFF, 0xFF
        value = 2147483647;
        expResult = new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        result = IntegerUtils.toByteArray(value);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of convByteArrayToString method, of class Helper.
     */
    @Test
    public void testHexBytesToString() {
        byte[] buffer = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0xA5};
        String expResult = "471108A5";
        String result = ByteUtils.toHexString(buffer);
        assertEquals(expResult, result);
    }

    /**
     * Test of convStringToByteArray method, of class Helper.
     */
    @Test
    public void testStringToByteArray() {
        String s = "47110815";
        byte[] expResult = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x15};
        byte[] result = StringUtils.toByteArray(s);
        assertArrayEquals(expResult, result);
        s = "12345";
        expResult = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45};
        result = StringUtils.toByteArray(s);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testIntToByteArrayWithBits() {
        long num = 0x61;
        byte[] result = LongUtils.toByteArray(num, 5);
        assertTrue(result.length == 2);
        assertEquals(3, result[0]);
        assertEquals(1, result[1]);
    }

}
