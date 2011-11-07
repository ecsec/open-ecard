package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.iso7816.EF_OD;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.IOException;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_ODTest {

    @Test
    public void readEF_OD() throws TLVException, IOException {
	TLV tlv = ReadHelper.readCIAFile("EF_OD.bin");
	EF_OD od = new EF_OD(tlv);
    }

}
