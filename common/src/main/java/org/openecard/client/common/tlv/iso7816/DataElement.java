package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.util.Helper;
import java.util.Arrays;


/**
 * See ISO/IEC 7816-4 p.20 tab. 12, Tag 82
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DataElement {

    private final FileDescriptorByte fdb;
    private final DataCodingByte dcb;
    private final int maxRecordSize;
    private final int numRecords;

    public DataElement(byte[] data) {
	this.fdb = new FileDescriptorByte(data[0]);
	if (data.length >= 2) {
	    dcb = new DataCodingByte(data[1]);
	} else {
	    dcb = null;
	}
	if (data.length >= 3) {
	    maxRecordSize = Helper.convertByteArrayToInt(Arrays.copyOfRange(data, 2, 4));
	    if (data.length > 4) {
		numRecords = Helper.convertByteArrayToInt(Arrays.copyOfRange(data, 4, 6));
	    } else {
		numRecords = -1;
	    }
	} else {
	    maxRecordSize = -1;
	    numRecords = -1;
	}
    }


    public FileDescriptorByte getFileDescriptorByte() {
	return fdb;
    }


    public boolean hasDataCodingByte() {
	return dcb != null;
    }

    public DataCodingByte getDataCodingByte() {
	return dcb;
    }


    public int getMaxRecordSize() {
	return maxRecordSize;
    }

    public int getNumRecords() {
	return numRecords;
    }


    public String toString(String prefix) {
        StringBuilder b = new StringBuilder();
        b.append(prefix);
        b.append("DataElement:\n");
        b.append(fdb.toString(prefix + " "));
        b.append("\n");
        if (dcb != null) {
            b.append(dcb.toString(prefix + " "));
            b.append("\n");
        }
        b.append(prefix);
        b.append(" ");
        b.append("max-record-size=");
        b.append(getMaxRecordSize());
        b.append(" num-records=");
        b.append(getNumRecords());

        return b.toString();
    }

    @Override
    public String toString() {
        return toString("");
    }

}
