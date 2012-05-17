package org.openecard.client.common.apdu.common;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardResponseAPDUTest {

    public CardResponseAPDUTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testGetSW1() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	byte expResult = 99;
	byte result = instance.getSW1();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetSW2() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	byte expResult = (byte) 194;
	byte result = instance.getSW2();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetSW() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	short expResult = 25538;
	short result = instance.getSW();
	assertEquals(expResult, result);
    }
}
