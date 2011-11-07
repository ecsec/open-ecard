package org.openecard.client.common.tlv;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TagLengthValue;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.TagClass;
import org.openecard.client.common.util.Helper;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TagLengthValueTest {

    @Test
    public void testShortClassZeroLength() throws TLVException {
	byte[] input = new byte[] { 0x00, 0x00 };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(TagClass.UNIVERSAL, t.getTagClass());
	assertEquals(0, t.getTagNum());
	assertTrue(t.isPrimitive());
    }

    @Test
    public void testLongClassZeroLength() throws TLVException {
	byte[] input = new byte[] { (byte)0xFF, (byte)0x81, 0x01, 0x00 };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(TagClass.PRIVATE, t.getTagClass());
	assertEquals(0x81, t.getTagNum());
	assertFalse(t.isPrimitive());
    }

    @Test
    public void testShortLength() throws TLVException {
	byte[] input = new byte[] { 0x00, 0x01, (byte)0xFF };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(1, t.getValueLength());
	assertArrayEquals(new byte[] {(byte)0xFF}, t.getValue());
    }

    @Test
    public void testLongLength() throws TLVException {
	byte[] input = new byte[] { 0x00, (byte)0x81, 0x01, (byte)0xFF };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(1, t.getValueLength());
	assertArrayEquals(new byte[] {(byte)0xFF}, t.getValue());
    }

    @Test
    public void testEOCLength() throws TLVException {
	byte[] input = new byte[] { 0x00, (byte)0x80, (byte)0xFF, 0x00, 0x00 };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(1, t.getValueLength());
	assertArrayEquals(new byte[] {(byte)0xFF}, t.getValue());
    }

    @Test
    public void testEOCLengthZero() throws TLVException {
	byte[] input = new byte[] { 0x00, (byte)0x80, 0x00, 0x00 };

	TagLengthValue t = TagLengthValue.fromBER(input);

	assertEquals(input.length, t.getRawLength());
	assertEquals(0, t.getValueLength());
    }

    @Test
    public void testFromAndToBER() throws TLVException {
	byte[] input = new byte[] { 0x00, 0x01, (byte)0xFF };

	TagLengthValue t = TagLengthValue.fromBER(input);
	byte[] result = t.toBER();

	Arrays.equals(input, result);
    }

    @Test
    public void testEvalFCP() throws TLVException {
	String inputStr =
		"62 25" +
		  "82 01 78" +
		  "83 02 3F 00" +
		  "84 07 D2 76 00 01 44 80 00" +
		  "85 02 B1 26" +
		  "8A 01 05" +
		  "8B 0A 00 0A 01 08 02 08 03 00 04 00" +
		  "A0 00";
	byte[] input = Helper.convStringWithWSToByteArray(inputStr);

	TLV t = TLV.fromBER(input);

	// perform some checks
	assertNull(t.getNext());
	assertTrue(t.findChildTags(0).isEmpty());
	assertTrue(t.findChildTags(0x8B).size() == 1);
	assertTrue(t.findChildTags(0x8B).get(0).getTagNumWithClass() == 0x8B);
    }

}
