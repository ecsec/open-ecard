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


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Parser {

    private final TLV tlv;
    private TLV next;

    public Parser(TLV tlv) {
	this.tlv = tlv;
	reset();
    }

    public final void reset() {
	this.next = this.tlv;
    }


    public boolean match(Tag... tags) {
	return match(next, tags);
    }
    private boolean match(TLV tag, Tag... tags) {
	long[] tagList = new long[tags.length];
	for (int i=0; i < tags.length; i++) {
	    tagList[i] = tags[i].getTagNumWithClass();
	}
	return match(tagList);
    }

    public boolean match(long... tagsWithClass) {
	return match(next, tagsWithClass);
    }
    private boolean match(TLV tag, long... tagsWithClass) {
	for (int i=0; i < tagsWithClass.length; i++) {
	    long nextTag = tagsWithClass[i];
	    if (tag != null && tag.getTagNumWithClass() == nextTag) {
		return true;
	    }
	}
	return false;
    }

    public boolean matchLA(int i, long... tagsWithClass) {
	TLV lookahead = LA(i);
	return match(lookahead, tagsWithClass);
    }
    public boolean matchLA(int i, Tag... tags) {
	TLV lookahead = LA(i);
	return match(lookahead, tags);
    }

    /**
     * Get TLV for index i and advance to the next sibling of the current element.
     *
     * @param i 0 for the current element, a positive number for any element further in the structure.
     * @return
     */
    public TLV next(int i) {
	if (next == null || i < 0) {
	    return null;
	}

	int count = i;
	TLV nextTLV = next;
	while (count != 0) {
	    if (nextTLV.hasNext()) {
		nextTLV = nextTLV.getNext();
	    } else {
		return null;
	    }
	}

	// set new next element
	next = nextTLV.getNext();
	// make copy and remove forward link
	nextTLV = new TLV(nextTLV);
	nextTLV.next = null;
	return nextTLV;
    }

    private TLV LA(int i) {
	if (next == null || i < 0) {
	    return null;
	}

	int count = i;
	TLV nextTLV = next;
	while (count != 0) {
	    if (nextTLV.hasNext()) {
		nextTLV = nextTLV.getNext();
	    } else {
		return null;
	    }
	}

	// make copy and remove forward link
	nextTLV = new TLV(nextTLV);
	nextTLV.next = null;
	return nextTLV;
    }

}
