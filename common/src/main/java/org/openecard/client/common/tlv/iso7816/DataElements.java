package org.openecard.client.common.tlv.iso7816;

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
        StringBuilder b = new StringBuilder();
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
