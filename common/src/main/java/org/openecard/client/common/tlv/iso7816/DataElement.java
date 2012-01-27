/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.common.tlv.iso7816;

import java.util.Arrays;
import org.openecard.client.common.util.IntegerUtils;


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
	    maxRecordSize = IntegerUtils.toInteger(Arrays.copyOfRange(data, 2, 4));
	    if (data.length > 4) {
		numRecords = IntegerUtils.toInteger(Arrays.copyOfRange(data, 4, 6));
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
