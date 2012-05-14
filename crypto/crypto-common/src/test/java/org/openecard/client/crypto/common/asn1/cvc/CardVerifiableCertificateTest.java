package org.openecard.client.crypto.common.asn1.cvc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.StringUtils;

/**
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class CardVerifiableCertificateTest {

    CardVerifiableCertificate cvc;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws TLVException {
	byte[] c = StringUtils.toByteArray("7F218201487F4E8201005F290100420E5A5A4456434141544230303030377F494F060A04007F0007020202020386410453107B1FA3767A3A36532A7CA1AE2BF2B3D08B6508CE03FECD9397CB107318519442980E9F17239A976FB1800A5515BC4AF61B013F7C5454A22A86D0CE18FADA5F20105A5A41546D74475465737430303030307F4C12060904007F0007030102025305300301FFB75F25060100010001035F2406010001000300655E732D060904007F00070301030280208FAC553CB79699D13E724E864BEBDD818DD550F7C34FC170ECDE2598A03F9EAC732D060904007F0007030103018020B48DA6DC54E8440F41EB20358CE8F640F45838D68B3E39812600047DBC5BB93B5F37405EE54A76BA698C098750E5E559F79CE2463E3F812083BB3815F4A7322C117C007C9D23958E99EC9542924BEF910A8C4C6462FB4D33B0F50F6B946F3A641C0DB1");
	cvc = new CardVerifiableCertificate(c);
    }

    @After
    public void tearDown() {
    }
}
