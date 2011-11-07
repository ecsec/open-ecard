package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.iso7816.AuthenticationObjectChoice;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.IOException;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_AODTest {

    @Test
    public void readEF_AOD() throws TLVException, IOException {
	TLV tlv = ReadHelper.readCIAFile("EF_AOD.bin");
	AuthenticationObjectChoice pkc = new AuthenticationObjectChoice(tlv);
    }

}
