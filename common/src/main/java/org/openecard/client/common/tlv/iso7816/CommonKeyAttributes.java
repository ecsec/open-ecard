/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.common.tlv.iso7816;

import java.util.LinkedList;
import java.util.List;
import org.openecard.client.common.tlv.*;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CommonKeyAttributes extends TLVType {

    private byte[] id;
    private TLVBitString usage;
    private boolean nativeFlag = true;
    private TLVBitString accessFlags;
    private Integer keyReference;
    private TLV startDate;
    private TLV endDate;
    private List<Integer> algRefs;


    public CommonKeyAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.OctetstringTag)) {
	    id = p.next(0).getValue();
	} else {
	    throw new TLVException("No id element in structure.");
	}
	if (p.match(Tag.BitstringTag)) {
	    usage = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("No usage element in structure.");
	}
	if (p.match(Tag.BooleanTag)) {
	    nativeFlag = p.next(0).getValue()[0] != 0x00;
	}
	if (p.match(Tag.BitstringTag)) {
	    accessFlags = new TLVBitString(p.next(0));
	}
	if (p.match(Tag.IntegerTag)) {
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

}
