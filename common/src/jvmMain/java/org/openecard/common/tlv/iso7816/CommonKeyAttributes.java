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

package org.openecard.common.tlv.iso7816;

import java.util.LinkedList;
import java.util.List;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class CommonKeyAttributes extends TLVType {

    private byte[] id;
    private TLVBitString usage;
    private boolean nativeFlag = true;
    private TLVBitString accessFlags = null;
    private Integer keyReference = null;
    private TLV startDate = null;
    private TLV endDate = null;
    private List<Integer> algRefs = null;


    public CommonKeyAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.OCTETSTRING_TAG)) {
	    id = p.next(0).getValue();
	} else {
	    throw new TLVException("No id element in structure.");
	}
	if (p.match(Tag.BITSTRING_TAG)) {
	    usage = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("No usage element in structure.");
	}
	if (p.match(Tag.BOOLEAN_TAG)) {
	    nativeFlag = p.next(0).getValue()[0] != 0x00;
	}
	if (p.match(Tag.BITSTRING_TAG)) {
	    accessFlags = new TLVBitString(p.next(0));
	}
	if (p.match(Tag.INTEGER_TAG)) {
	    keyReference = ByteUtils.toInteger(p.next(0).getValue());
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 24))) {
	    startDate = p.next(0);
	}
	if (p.match(new Tag(TagClass.CONTEXT, true, 0))) {
	    endDate = p.next(0);
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    TLVList list = new TLVList(p.next(0), new Tag(TagClass.CONTEXT, false, 1).getTagNumWithClass());
	    algRefs = new LinkedList<Integer>();
	    for (TLV next : list.getContent()) {
		algRefs.add(ByteUtils.toInteger(next.getValue()));
	    }
	}
    }

    public byte[] getId() {
	return id;
    }

    public TLVBitString getUsage() {
	return usage;
    }

    public boolean isNativeFlag() {
	return nativeFlag;
    }

    public TLVBitString getAccessFlags() {
	return accessFlags;
    }

    public Integer getKeyReference() {
	if (keyReference != null) {
	    return keyReference;
	} else {
	    return -1;
	}
    }

    public TLV getStartDate() {
	return startDate;
    }

    public TLV getEndDate() {
	return endDate;
    }

    public List<Integer> getAlgRefs() {
	return algRefs;
    }

}
