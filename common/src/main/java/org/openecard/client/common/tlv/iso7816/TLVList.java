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

import java.util.LinkedList;
import java.util.List;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLVList {

    protected final TLV tlv;

    protected TLVList(TLV tlv, long expectedTagNum) throws TLVException {
	if (tlv.getTagNumWithClass() != expectedTagNum) {
	    throw new TLVException("Not of type TLVList.");
	}
	this.tlv = tlv;
    }

    public TLVList(List<TLV> children) throws TLVException {
	tlv = new TLV();
	tlv.setTagNumWithClass(Tag.SequenceTag.getTagNumWithClass());
	// link in children
	if (! children.isEmpty()) {
	    TLV first = children.get(0);
	    tlv.setChild(first);
	    for (int i=1; i < children.size(); i++) {
		first.addToEnd(children.get(i));
	    }
	}
    }

    public TLVList(TLV tlv) throws TLVException {
	this(tlv, 0x61);
    }

    public TLVList(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public List<TLV> getContent() {
	if (tlv.hasChild()) {
	    return tlv.getChild().asList();
	}
	return new LinkedList<TLV>();
    }

}
