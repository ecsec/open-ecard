package org.openecard.client.common.util;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Johannes.Schmoelz
 */
public class HelperTest {

    /**
     * Test of concatenate method, of class Helper.
     */
    @Test
    public void testConcatenate_byteArr_byteArr() {
        byte[] b1 = {(byte) 0x47, (byte) 0x11};
        byte[] b2 = {(byte) 0x08, (byte) 0x15};
        byte[] expResult = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x15};
        byte[] result = Helper.concatenate(b1, b2);
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
        byte[] result = Helper.concatenate(b1, b2);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of convertByteArrayToInt method, of class Helper.
     */
    @Test
    public void testConvertByteArrayToInt() {
        // 0 = 0x00
        byte[] buffer = {0x00};
        int expResult = 0;
        int result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 255 = 0xFF
        buffer = new byte[]{(byte) 0xFF};
        expResult = 255;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 256 = 0x01, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00};
        expResult = 256;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 65535 = 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF};
        expResult = 65535;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 65536 = 0x01, 0x00, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00};
        expResult = 65536;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 16777215 = 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = 16777215;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 16777216 = 0x01, 0x00, 0x00, 0x00
        buffer = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        expResult = 16777216;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // 2147483647 = 0x7F, 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = 2147483647;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
        // -1 = 0xFF, 0xFF, 0xFF, 0xFF
        buffer = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        expResult = -1;
        result = Helper.convertByteArrayToInt(buffer);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertPosIntToByteArray method, of class Helper.
     */
    @Test
    public void testConvertPosIntToByteArray() {
        // 0 = 0x00
        int value = 0;
        byte[] expResult = {0x00};
        byte[] result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 255 = 0xFF
        value = 255;
        expResult = new byte[]{(byte) 0xFF};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 256 = 0x01, 0x00
        value = 256;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 65535 = 0xFF, 0xFF
        value = 65535;
        expResult = new byte[]{(byte) 0xFF, (byte) 0xFF};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 65536 = 0x01, 0x00, 0x00
        value = 65536;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 16777215 = 0xFF, 0xFF, 0xFF
        value = 16777215;
        expResult = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 16777216 = 0x01, 0x00, 0x00, 0x00
        value = 16777216;
        expResult = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
        // 2147483647 = 0x7F, 0xFF, 0xFF, 0xFF
        value = 2147483647;
        expResult = new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        result = Helper.convertPosIntToByteArray(value);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of convByteArrayToString method, of class Helper.
     */
    @Test
    public void testConvByteArrayToString() {
        byte[] buffer = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0xA5};
        String expResult = "471108A5";
        String result = Helper.convByteArrayToString(buffer);
        assertEquals(expResult, result);
    }

    /**
     * Test of convStringToByteArray method, of class Helper.
     */
    @Test
    public void testConvStringToByteArray() {
        String s = "47110815";
        byte[] expResult = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x15};
        byte[] result = Helper.convStringToByteArray(s);
        assertArrayEquals(expResult, result);
        s = "12345";
        expResult = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45};
        result = Helper.convStringToByteArray(s);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of dumpAPDU method, of class Helper.
     */
    @Test
    public void testDumpAPDU() {
        System.out.print("dumpAPDU: 47110815 == ");
        byte[] buffer = {(byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x15};
        Helper.dumpAPDU(buffer);
    }

    @Test
    public void testIntToByteArrayWithBits() {
        long num = 0x61;
        byte[] result = Helper.convertPosIntToByteArray(num, 5);
        assertTrue(result.length == 2);
        assertEquals(3, result[0]);
        assertEquals(1, result[1]);
    }

}
