package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.util.Helper;



/**
 * See ISO/IEC 7816-4 p.60 tab. 87
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DataCodingByte {

    private final byte data;

    public DataCodingByte(byte data) {
	this.data = data;
    }

    // TODO: implement

    public String toString(String prefix) {
        StringBuilder b = new StringBuilder();
        b.append(prefix);
        b.append("DataCoding-Byte: ");
        b.append(Helper.convByteArrayToString(Helper.convertPosIntToByteArray(data)));

        return b.toString();
    }

    @Override
    public String toString() {
        return toString("");
    }

}
