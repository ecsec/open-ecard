package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.iso7816.PrivateKeyChoice;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.IOException;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_PrKDTest {

    @Test
    public void readEF_PrKD() throws TLVException, IOException {
	TLV tlv = ReadHelper.readCIAFile("EF_PrKD.bin");
	PrivateKeyChoice pkc = new PrivateKeyChoice(tlv);
    }

}
