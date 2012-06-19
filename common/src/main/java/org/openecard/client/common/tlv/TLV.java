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

package org.openecard.client.common.tlv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLV {

    TagLengthValue tag;
    //protected TLV parent = null;
    protected TLV next = null;
    protected TLV child = null;

    public TLV() {
	tag = new TagLengthValue();
    }

    public TLV(TLV obj) {
	this.tag = obj.tag;
	this.next = (obj.next != null) ? new TLV(obj.next) : null;
	this.child = (obj.child != null) ? new TLV(obj.child) : null;
    }

    ///
    /// deferred setters for TLV container
    ///
    public TagClass getTagClass() {
	return tag.getTagClass();
    }

    public void setTagClass(TagClass tagClass) {
	tag.setTagClass(tagClass);
    }

    public boolean isPrimitive() {
	return tag.isPrimitive();
    }

    public void setPrimitive(boolean primitive) {
	tag.setPrimitive(primitive);
    }

    public long getTagNum() {
	return tag.getTagNum();
    }

    public void setTagNum(byte tagNum) {
	setTagNum(tagNum & 0xFF);
    }

    public void setTagNum(long tagNum) {
	tag.setTagNum(tagNum);
    }

    public long getTagNumWithClass() {
	return tag.getTagNumWithClass();
    }

    public void setTagNumWithClass(byte tagNumWithClass) throws TLVException {
	setTagNumWithClass(tagNumWithClass & 0xFF);
    }

    public void setTagNumWithClass(long tagNumWithClass) throws TLVException {
	tag.setTagNumWithClass(tagNumWithClass);
    }

    public int getValueLength() {
	return tag.getValueLength();
    }

    public byte[] getValue() {
	return tag.getValue();
    }

    public void setValue(byte[] value) {
	tag.setValue(value);
    }

    ///
    /// modification functions
    ///
    public void addToEnd(TLV sibling) {
	if (next == null) {
	    next = sibling;
	} else {
	    next.addToEnd(sibling);
	}
    }

    /**
     * Remove next which is indicated by n. 0 means direct sibling.
     */
    public TLV remove(int n) {
	if (n == 0) {
	    TLV tmp = next;
	    next = null;
	    return tmp;
	} else if (n > 0 && next != null) {
	    return next.remove(n - 1);
	} else {
	    return null;
	}
    }

    public TLV removeNext() {
	return remove(0);
    }

    public void setChild(TLV child) {
	this.child = child;
    }

    public boolean hasChild() {
	return child != null;
    }

    public TLV getChild() {
	return child;
    }

    public boolean hasNext() {
	return next != null;
    }

    public TLV getNext() {
	return next;
    }

    public List<TLV> asList() {
	LinkedList<TLV> result = new LinkedList<TLV>();

	TLV nextTag = this;
	while (nextTag != null) {
	    TLV toAdd = new TLV(nextTag);
	    toAdd.next = null; // delete reference to next
	    result.add(toAdd);

	    nextTag = nextTag.next;
	}

	return result;
    }

    public List<TLV> findNextTags(long num) {
	List<TLV> all = asList();
	LinkedList<TLV> result = new LinkedList<TLV>();

	for (TLV nextTLV : all) {
	    if (nextTLV.getTagNumWithClass() == num) {
		result.add(nextTLV);
	    }
	}

	return result;
    }

    public List<TLV> findChildTags(long num) {
	if (hasChild()) {
	    return getChild().findNextTags(num);
	} else {
	    return new LinkedList<TLV>();
	}
    }

    ///
    /// TLV construction from and to different encodings
    ///
    public static TLV fromBER(byte[] input) throws TLVException {
	byte[] rest = input;

	TLV first = new TLV();
	boolean isFirst = true;
	TLV last = first;
	// build as long as there is input left
	while (rest.length > 0) {
	    TLV next;
	    if (isFirst) {
		next = first;
	    } else {
		next = new TLV();
	    }

	    // break execution when 0 tag encountered
	    if (rest[0] == (byte) 0) {
		return first;
	    }
	    // convert bytes to flat TLV data
	    next.tag = TagLengthValue.fromBER(rest);
	    // if constructed build child structure
	    if (!next.tag.isPrimitive() && next.tag.getValueLength() > 0) {
		next.child = fromBER(next.tag.getValue());
	    }

	    // set next as sibling in last
	    if (isFirst) {
		isFirst = false;
	    } else {
		last.next = next;
	    }
	    last = next;

	    // get rest of the bytes for next iteration
	    rest = last.tag.extractRest(rest);
	}

	return first;
    }

    public byte[] toBER() throws TLVException {
	return toBER(false);
    }

    public byte[] toBER(boolean withSuccessors) throws TLVException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	// value calculated from child if any
	toBER(out, withSuccessors);
	return out.toByteArray();
    }

    private void toBER(ByteArrayOutputStream out, boolean withSuccessors) throws TLVException {
	if (child != null) {
	    byte[] childBytes = child.toBER(withSuccessors);
	    tag.setPrimitive(false);
	    tag.setValue(childBytes);
	} else {
	    tag.setPrimitive(true);
	}
	// write child to output stream
	try {
	    out.write(tag.toBER());
	} catch (IOException ex) {
	    throw new TLVException(ex);
	}

	if (withSuccessors && next != null) {
	    next.toBER(out, withSuccessors);
	}
    }

    @Override
    public String toString() {
	return toString("");
    }

    public String toString(String prefix) {
	String result = prefix + String.format("%02X", getTagNumWithClass());

	if (!hasChild()) {
	    result += " " + tag.getValueLength() + " " + ByteUtils.toHexString(tag.getValue());
	} else {
	    result += "\n" + getChild().toString(prefix + "  ");
	}

	if (hasNext()) {
	    result += "\n" + getNext().toString(prefix);
	}

	return result;
    }

}
