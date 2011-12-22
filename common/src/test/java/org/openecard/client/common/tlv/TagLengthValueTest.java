package org.openecard.client.common.tlv;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Test;
import org.openecard.client.common.util.Helper;
import static org.junit.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TagLengthValueTest {

    @Test
    public void testCardVerifiableCertificate() throws TLVException {
	/** Certificate Body */
	int TAG_BODY = 0x7F4E;
	/** Certificate Profile Identifier */
	int TAG_CPI = 0x5F29;
	/** Certification Authority Reference */
	int TAG_CAR = 0x42;
	/** Certificate Holder Reference */
	int TAG_CHR = 0x5F20;

	byte[] input = new BigInteger("7F218201427F4E81FB5F290100420E5A5A4456434141544130303030357F494F060A04007F0007020202020386410470C07FAA329E927D961F490F5430B395EECF3D2A538194D8B637DE0F8ACF60A9031816AC51B594097EB211FB8F55FAA8507D5800EF7B94E024F9630314116C755F200B5A5A444B423230303033557F4C12060904007F0007030102025305000301DF045F25060100000601085F2406010000070001655E732D060904007F00070301030280207C1901932DB75D08539F2D4A27C938F79E69E083C442C068B299D185BC8AFA78732D060904007F0007030103018020BFD2A6A2E4237948D7DCCF7975D71D40F15307AA59F580A48777CBEED093F54B5F3740618F584E4293F75DDE8977311694B69A3ED73BBE43FDAFEC11B7ECF054F84ACB1231615338CE8D6EC332480883E14E0664950F85134290DD716B7C153232BC96", 16).toByteArray();
	TLV tlv = TLV.fromBER(input);

	byte version = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CPI).get(0).getValue()[0];
	String CAR = new String(tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CAR).get(0).getValue());
	String certificateHolderReference = new String(tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CHR).get(0).getValue());

	assertEquals(version, 0);
	assertEquals(CAR, "ZZDVCAATA00005");
	assertEquals(certificateHolderReference, "ZZDKB20003U");
    }


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
