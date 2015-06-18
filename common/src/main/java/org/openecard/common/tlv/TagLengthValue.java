/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.common.tlv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.openecard.common.util.IntegerUtils;


/**
 * Internal class representing one entry with a TLV definition.
 *
 * @author Tobias Wich
 */
class TagLengthValue {

    private int numOctets;

    private Tag tag;
    private byte[] value;


    private TagLengthValue(int numOctets, Tag tag, byte[] value) {
	this.numOctets = numOctets;
	this.tag = tag;
	this.value = value;
    }

    private TagLengthValue(int numOctets, TagClass tagClass, boolean primitive, long tagNum, byte[] value) {
	this(numOctets, new Tag(tagClass, primitive, tagNum), value);
    }

    private TagLengthValue(Tag tag, byte[] value) {
	this(0, tag, value);
    }

    public TagLengthValue(TagClass tagClass, boolean primitive, long tagNum, byte[] value) {
	this(0, tagClass, primitive, tagNum, value);
    }

    public TagLengthValue() {
	this(0, new Tag(), new byte[]{});
    }



    public TagClass getTagClass() {
	return this.tag.getTagClass();
    }
    public void setTagClass(TagClass tagClass) {
	this.tag.setTagClass(tagClass);
    }

    public boolean isPrimitive() {
	return this.tag.isPrimitive();
    }
    void setPrimitive(boolean primitive) {
	this.tag.setPrimitive(primitive);
    }

    public long getTagNum() {
	return this.tag.getTagNum();
    }
    public void setTagNum(long tagNum) {
	this.tag.setTagNum(tagNum);
    }

    public long getTagNumWithClass() {
	return this.tag.getTagNumWithClass();
    }
    public void setTagNumWithClass(long tagNumWithClass) throws TLVException {
	this.tag.setTagNumWithClass(tagNumWithClass);
    }
    public void setTagNumWithClass(byte[] tagNumWithClass) throws TLVException {
	this.tag = Tag.fromBER(tagNumWithClass);
    }

    public int getValueLength() {
	return this.value.length;
    }

    public byte[] getValue() {
	return this.value;
    }
    public void setValue(byte[] value) {
	this.value = value;
    }


    /**
     * Get number of the bytes from which this TLV was created.<br>
     * Only makes sense if created from bytes.
     * @return
     */
    int getRawLength() {
	return this.numOctets;
    }
    /**
     * When fed with a large input stream, cut off the portion which makes up this TLV.
     * @param inputWithThisTag
     * @return
     */
    byte[] extractRest(byte[] inputWithThisTag) {
	return Arrays.copyOfRange(inputWithThisTag, getRawLength(), inputWithThisTag.length);
    }


    static TagLengthValue fromBER(byte[] data) throws TLVException {
	Tag tag = Tag.fromBER(data);
	// how many octets made up this tag?
	int numOctets = tag.numOctets;

	// get length
	int dataLength = 0;
	boolean endOfLine = false;
	if (((data[numOctets] >> 7) & 0x01) == 0) {
	    // short form
	    dataLength = data[numOctets];
	    numOctets++;
	} else {
	    // has end-of-line octets
	    if ((data[numOctets] & 0x7F) == 0x00) {
		endOfLine = true;
		numOctets++;
		// loop through content to find termination point
		int i = 0;
		boolean endFound = false;
		boolean zeroFound = false;
		do {
		    if (data.length <= numOctets+i) {
			throw new TLVException("Not enough bytes in input to read TLV length.");
		    }
		    byte next = data[numOctets+i];
		    if (next == 0x00) {
			if (zeroFound) {
			    endFound = true;
			} else {
			    zeroFound = true;
			}
		    } else {
			zeroFound = false;
		    }
		    i++;
		} while (! endFound);
		// calculate data length
		dataLength = i-2;
	    } else {
		// long form
		// first byte indicates number of length bytes
		int numLengthBytes = data[numOctets] & 0x7F;
		numOctets++;

		int i = 0;
		while (i < numLengthBytes) {
		    if (i*8 > 32) {
			throw new TLVException("Length doesn't fit into a 32 bit word.");
		    } else if (data.length < numOctets+i+1) {
			throw new TLVException("Not enough bytes in input to read TLV length.");
		    }
		    if (data[numOctets+i] < 0) {
			// correct bytes wich are interpreted as negative numbers by java
			dataLength = (dataLength << 8) | (256+data[numOctets+i]);
		    } else {
			dataLength = (dataLength << 8) | data[numOctets+i];
		    }
		    i++;
		}
		numOctets += i;
	    }
	}

	// extract data based on calculated length
	byte[] dataField = Arrays.copyOfRange(data, numOctets, numOctets+dataLength);

	// recalculate total length of datablock
	numOctets = numOctets + dataLength;
	if (endOfLine) {
	    numOctets += 2;
	}

	// we have all values, build Tag object and return
	TagLengthValue result = new TagLengthValue(numOctets, tag, dataField);
	return result;
    }


    byte[] toBER() {
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();

	    byte[] tagBytes = tag.toBER();
	    out.write(tagBytes);

	    // calculate length according to input data
	    int len = getValueLength();
	    if (len <= 127) {
		// short form
		out.write((byte)len);
	    } else {
		byte[] lenBytes = IntegerUtils.toByteArray(len);
		byte lenHeader = (byte) (0x80 | lenBytes.length);
		out.write(lenHeader);
		out.write(lenBytes);
	    }

	    // write actual data
	    out.write(getValue());

	    return out.toByteArray();
	} catch (IOException ex) {
	    // IOException depends solely on the stream. The only thing that can happen here is OOM.
	    throw new RuntimeException(ex);
	}
    }

}
