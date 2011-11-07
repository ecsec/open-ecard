package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.iso7816.CIAInfo;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.IOException;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_CIAInfoTest {

    @Test
    public void readEF_CIAInfo() throws TLVException, IOException {
	TLV tlv = ReadHelper.readCIAFile("EF_CIAInfo.bin");
	CIAInfo ciaInfo = new CIAInfo(tlv);
    }

}
