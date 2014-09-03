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
 * Parser class to help evaluate the internas of TLV structures.
 * This implementation works on the children of a given TLV object. If the parser is needed to evaluate deep structures,
 * then new instances of it have to be created for the respective substructures.
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

    /**
     * Reset the parser to the original object given in the constructor.
     */
    public final void reset() {
	this.next = this.tlv;
    }


    /**
     * Matches the tag of the next element against any of the given tags.
     *
     * @param tags List of {@code Tag} objects which yield a positive result when found.
     * @return {@code true} when the next object has one of the given tags, {@code false} otherwise.
     */
    public boolean match(Tag... tags) {
	return match(next, convertTags(tags));
    }

    /**
     * Matches the tag of the next element against any of the given tags.
     *
     * @param tagsWithClass List of tags which yield a positive result when found.
     * @return {@code true} when the next object has one of the given tags, {@code false} otherwise.
     */
    public boolean match(long... tagsWithClass) {
	return match(next, tagsWithClass);
    }

    /**
     * Matches the tag of the ith element against any of the given tags.
     *
     * @param i Number stating how far to look ahead. 0 means to look at the current element.
     * @param tagsWithClass List of tags which yield a positive result when found.
     * @return {@code true} when the ith object has one of the given tags, {@code false} otherwise.
     */
    public boolean matchLA(int i, long... tagsWithClass) {
	TLV lookahead = LA(i);
	return match(lookahead, tagsWithClass);
    }

    /**
     * Matches the tag of the ith element against any of the given tags.
     *
     * @param i Number stating how far to look ahead. 0 means to look at the current element.
     * @param tags List of {@code Tag} objects which yield a positive result when found.
     * @return {@code true} when the ith object has one of the given tags, {@code false} otherwise.
     */
    public boolean matchLA(int i, Tag... tags) {
	TLV lookahead = LA(i);
	return match(lookahead, convertTags(tags));
    }

    private boolean match(TLV tlv, long[] tagsWithClass) {
	if (tlv != null) {
	    long tagNumWithClass = tlv.getTagNumWithClass();
	    for (long nextTag : tagsWithClass) {
		if (tagNumWithClass == nextTag) {
		    return true;
		}
	    }
	}
	return false;
    }

    private long[] convertTags(Tag... tags) {
	long[] tagList = new long[tags.length];
	for (int i=0; i < tags.length; i++) {
	    tagList[i] = tags[i].getTagNumWithClass();
	}
	return tagList;
    }

    /**
     * Get TLV for index i and advance parser to the next sibling of the ith element.
     *
     * @param i 0 for the current element, a positive number for any element further in the structure.
     * @return The next element for the given index, or {@code null} if the index is greater than the number of
     *   available elements.
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
		count--;
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
		count--;
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
