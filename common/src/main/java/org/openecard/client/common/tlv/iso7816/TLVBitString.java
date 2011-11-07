package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import java.util.Arrays;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLVBitString {

    private TLV tlv;
    private byte[] data;

    public TLVBitString(TLV tlv, long tagNumWithClass) throws TLVException {
	if (tlv.getTagNumWithClass() != tagNumWithClass) {
	    throw new TLVException("Type numbers don't match.");
	}

        this.tlv = tlv;
	this.data = tlv.getValue();
	this.data = Arrays.copyOfRange(data, 1, data.length);
    }

    public TLVBitString(TLV tlv) throws TLVException {
	this(tlv, Tag.BitstringTag.getTagNumWithClass());
    }

    public boolean isSet(int pos) {
	int i = pos / 8;
	int j = 1 << 7 - pos % 8;

	if ((this.data != null) && (this.data.length > i)) {
	    int k = this.data[i];
	    return (k & j) != 0;
	} else {
	    return false;
	}
    }

}
