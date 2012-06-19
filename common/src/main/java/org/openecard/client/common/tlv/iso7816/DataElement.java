/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.common.tlv.iso7816;

import java.util.Arrays;
import org.openecard.client.common.util.ByteUtils;


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
	    maxRecordSize = ByteUtils.toInteger(Arrays.copyOfRange(data, 2, 4));
	    if (data.length > 4) {
		numRecords = ByteUtils.toInteger(Arrays.copyOfRange(data, 4, 6));
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
