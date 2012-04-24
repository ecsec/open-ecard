package org.openecard.client.crypto.common.asn1.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ObjectIdentifierUtilsTest {

    public ObjectIdentifierUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testToByteArray() throws Exception {
	String oid = EACObjectIdentifier.id_PACE;
	byte[] expResult = new byte[]{0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	byte[] result = ObjectIdentifierUtils.toByteArray(oid);
	assertArrayEquals(expResult, result);
    }

    @Test
    public void testToString() throws Exception {
	byte[] oid = new byte[]{0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	String expResult = "0.4.0.127.0.7.2.2.4";
	String result = ObjectIdentifierUtils.toString(oid);
	assertEquals(expResult, result);
    }

    @Test
    public void testToString2() throws Exception {
	byte[] oid = new byte[]{0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	String expResult = "0.4.0.127.0.7.2.2.4";
	String result = ObjectIdentifierUtils.toString(oid);
	assertEquals(expResult, result);
    }

    @Test
    public void testGetValue() {
	String oid = EACObjectIdentifier.id_PACE;
	byte[] expResult = new byte[]{0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	byte[] result = ObjectIdentifierUtils.getValue(oid);
	assertArrayEquals(expResult, result);
    }

}
