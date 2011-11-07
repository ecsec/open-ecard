package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ReadHelper {

    public static TLV readCIAFile(String name) throws IOException, TLVException {
	String path = "/df.cia/" + name;
	InputStream ins = ReadHelper.class.getResourceAsStream(path);
	ByteArrayOutputStream outs = new ByteArrayOutputStream(ins.available());

	int next;
	while ((next = ins.read()) != -1) {
	    outs.write((byte)next);
	}

	byte[] resultBytes = outs.toByteArray();
	TLV result = TLV.fromBER(resultBytes);
	return result;
    }

}
