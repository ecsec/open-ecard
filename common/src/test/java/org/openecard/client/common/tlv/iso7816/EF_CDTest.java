package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.iso7816.CertificateChoice;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.IOException;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_CDTest {

    @Test
    public void readEF_CD() throws TLVException, IOException {
	TLV tlv = ReadHelper.readCIAFile("EF_CD.bin");
	CertificateChoice pkc = new CertificateChoice(tlv);
    }

}
