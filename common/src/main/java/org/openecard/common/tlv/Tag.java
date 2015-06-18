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

package org.openecard.common.tlv;

import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.LongUtils;


/**
 *
 * @author Tobias Wich
 */
public class Tag {

    ///
    /// Builtin standard tags
    ///

    public static final Tag BOOLEAN_TAG = new Tag(TagClass.UNIVERSAL, true, 1);
    public static final Tag INTEGER_TAG = new Tag(TagClass.UNIVERSAL, true, 2);
    public static final Tag BITSTRING_TAG = new Tag(TagClass.UNIVERSAL, true, 3); // primitive or constructed
    public static final Tag OCTETSTRING_TAG = new Tag(TagClass.UNIVERSAL, true, 4); // primitive or constructed
    public static final Tag NULL_TAG = new Tag(TagClass.UNIVERSAL, true, 5);
    public static final Tag OID_TAG = new Tag(TagClass.UNIVERSAL, true, 6);
    //public static final Tag ObjectDescriptorTag = new Tag(TagClass.UNIVERSAL, true, 7);
    //public static final Tag ExternalTypeTag = new Tag(TagClass.UNIVERSAL, true, 8);
    public static final Tag REAL_TAG = new Tag(TagClass.UNIVERSAL, true, 9);
    public static final Tag ENUMERATED_TAG = new Tag(TagClass.UNIVERSAL, true, 10);
    public static final Tag EMBEDDED_PDV_TAG = new Tag(TagClass.UNIVERSAL, false, 11);
    //public static final Tag UTF8Tag = new Tag(TagClass.UNIVERSAL, true, 12);
    public static final Tag RELATIVE_OID_TAG = new Tag(TagClass.UNIVERSAL, true, 13);
    //public static final Tag TimeTag = new Tag(TagClass.UNIVERSAL, true, 14);
    // 15 is reserved for future recommendations
    public static final Tag SEQUENCE_TAG = new Tag(TagClass.UNIVERSAL, false, 16);
    public static final Tag SET_TAG = new Tag(TagClass.UNIVERSAL, false, 17);
    // char strings 18-22, 25-30
    //public static final Tag UTCTimeTag = new Tag(TagClass.UNIVERSAL, true, 23);
    //public static final Tag GeneralizedTimeTag = new Tag(TagClass.UNIVERSAL, true, 24);
    //public static final Tag DateTag = new Tag(TagClass.UNIVERSAL, true, 31);
    //public static final Tag TimeOfDayTag = new Tag(TagClass.UNIVERSAL, true, 32);
    //public static final Tag DateTimeTag = new Tag(TagClass.UNIVERSAL, true, 33);
    //public static final Tag DurationTag = new Tag(TagClass.UNIVERSAL, true, 34);
    //public static final Tag IntOIDTag = new Tag(TagClass.UNIVERSAL, true, 35);
    //public static final Tag IntOIDReferenceTag = new Tag(TagClass.UNIVERSAL, true, 36);



    private TagClass tagClass;
    private boolean primitive;
    private long tagNum;

    private long tagNumWithClass;

    /** Set when created from BER. This is needed when creating a TagLengthValue instance from BER */
    protected int numOctets=0;


    public Tag() {
	this(TagClass.UNIVERSAL, true, 0);
    }
    public Tag(TagClass tagClass, boolean primitive, long tagNum) {
	this.tagClass = tagClass;
	this.primitive = primitive;
	this.tagNum = tagNum;
	calculateTagNumWithClass();
    }


    public TagClass getTagClass() {
	return tagClass;
    }
    public void setTagClass(TagClass tagClass) {
	this.tagClass = tagClass;
	calculateTagNumWithClass();
    }

    public boolean isPrimitive() {
	return this.primitive;
    }
    void setPrimitive(boolean primitive) {
	this.primitive = primitive;
	calculateTagNumWithClass();
    }

    public long getTagNum() {
	return this.tagNum;
    }
    public void setTagNum(long tagNum) {
	this.tagNum = tagNum;
	calculateTagNumWithClass();
    }

    public long getTagNumWithClass() {
	return this.tagNumWithClass;
    }
    public void setTagNumWithClass(long tagNumWithClass) throws TLVException {
	Tag newTag = Tag.fromBER(LongUtils.toByteArray(tagNumWithClass));
	this.tagClass = newTag.tagClass;
	this.primitive = newTag.primitive;
	this.tagNum = newTag.tagNum;
	this.tagNumWithClass = tagNumWithClass;
    }


    private void calculateTagNumWithClass() {
	byte leading = this.tagClass.num;
	leading = (byte) ((leading << 1) | ((this.primitive) ? 0 : 1));

	byte[] rest;
	if (this.tagNum >= 31) {
	    // long
	    leading = (byte) ((leading << 5) | 0x1F);
	    rest = LongUtils.toByteArray(this.tagNum, 7);
	    for (int i=0; i < rest.length-1; i++) {
		rest[i] |= 0x80;
	    }
	} else {
	    // short
	    leading = (byte) ((leading << 5) | this.tagNum);
	    rest = new byte[0];
	}

	byte[] resultBytes = ByteUtils.concatenate(leading, rest);
	this.tagNumWithClass = ByteUtils.toLong(resultBytes);
    }


    public static Tag fromBER(byte[] data) throws TLVException {
	// how many octets made up this tag?
	int numOctets = 1;
	// get common values independed from encoding type
	TagClass tagClass = TagClass.getTagClass(data[0]);
	boolean primitive = ((data[0] >> 5) & 0x01) == 0x00;

	// get value so it can be seen if short or long form is present
	long tagNum = 0;
	byte tmpTagNum = (byte) (data[0] & 0x1F);
	if (tmpTagNum <= 30) {
	    // short form
	    tagNum = tmpTagNum;
	} else {
	    // long form
	    byte next;
	    // read as long as it is not the last octet
	    do {
		// terminate if there are no bytes left or the number is larger than 64 bits
		if (numOctets*7 > 64) {
		    throw new TLVException("Tag number doesn't fit into a 64 bit word.");
		} else if (data.length < numOctets) {
		    throw new TLVException("Not enough bytes in input bytes to build TLV tag.");
		}
		// get next number
		next = data[numOctets];
		numOctets++;
		// get next bytes and merge them into result
		byte nextValue = (byte) (next & 0x7F);
		tagNum = (tagNum << 7) | nextValue;
	    } while (((next >> 7) & 0x01) == 0x01);
	}

	Tag resultTag = new Tag(tagClass, primitive, tagNum);
	resultTag.numOctets = numOctets;
	return resultTag;
    }

    public byte[] toBER() {
	return LongUtils.toByteArray(tagNumWithClass);
    }

    @Override
    public String toString() {
	return "[" + tagClass.toString() + " " +
	    (primitive ? "prim " : "cons ") +
	    tagNum + " (0x" + Long.toHexString(tagNumWithClass) + ")]";
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Tag) {
	    Tag other = (Tag) obj;
	    return this.getTagNumWithClass() == other.getTagNumWithClass();
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 79 * hash + (int) (this.tagNumWithClass ^ (this.tagNumWithClass >>> 32));
	return hash;
    }

}
