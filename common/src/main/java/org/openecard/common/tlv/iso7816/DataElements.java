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

package org.openecard.common.tlv.iso7816;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DataElements {

    private final List<DataElement> dataElements;

    public DataElements(List<byte[]> dataElements) {
	this.dataElements = new ArrayList<DataElement>(dataElements.size());
	for (byte[] next : dataElements) {
	    this.dataElements.add(new DataElement(next));
	}
    }


    ///
    /// FileDescriptorBytes combined - some of them must be seen as a whole (linear and record)
    ///

    public boolean shareable() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().shareable();
	}
	return false;
    }


    public boolean isDF() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().isDF();
	}
	return false;
    }

    public boolean isEF() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().isEF();
	}
	return false;
    }

    public boolean isWorkingEF() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().isWorkingEF();
	}
	return false;
    }

    public boolean isInternalEF() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().isInternalEF();
	}
	return false;
    }

    public boolean isUnknownFormat() {
	// only one needed to state this claim
	if (! dataElements.isEmpty()) {
	    return dataElements.get(0).getFileDescriptorByte().isUnknownFormat();
	}
	return false;
    }

    public boolean isTransparent() {
	for (DataElement next : dataElements) {
	    if (next.getFileDescriptorByte().isTransparent()) {
		return true;
	    }
	}
	return false;
    }
    public boolean isLinear() {
	for (DataElement next : dataElements) {
	    if (next.getFileDescriptorByte().isLinear()) {
		return true;
	    }
	}
	return false;
    }
    public boolean isCyclic() {
	for (DataElement next : dataElements) {
	    if (next.getFileDescriptorByte().isCyclic()) {
		return true;
	    }
	}
	return false;
    }
    public boolean isDataObject() {
	for (DataElement next : dataElements) {
	    if (next.getFileDescriptorByte().isDataObject()) {
		return true;
	    }
	}
	return false;
    }



    // TODO: implement other aggregating functions when needed (e.g. for DataCodingByte or record size)

    public String toString(String prefix) {
	StringBuilder b = new StringBuilder(4096);
	b.append("DataElements:");
	for (DataElement next : dataElements) {
	    b.append("\n");
	    b.append(next.toString(prefix + " "));
	}
	return b.toString();
    }

    @Override
    public String toString() {
	return toString("");
    }

}
