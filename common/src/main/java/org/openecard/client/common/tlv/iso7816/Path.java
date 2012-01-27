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

import org.openecard.client.common.tlv.*;
import org.openecard.client.common.util.IntegerUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Path {

    private final TLV tlv;

    private byte[] efIdOrPath;
    private Integer index;
    private Integer length;

    public Path(TLV tlv) throws TLVException {
	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    efIdOrPath = p.next(0).getValue();
	} else {
	    throw new TLVException("No efIdOrPath given.");
	}
	index = null;
	length = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2)) && p.matchLA(1, new Tag(TagClass.CONTEXT, true, 0))) {
	    index = IntegerUtils.toInteger(p.next(0).getValue());
	    length = IntegerUtils.toInteger(p.next(0).getValue());
	}
    }

    public Path(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public byte[] efIdOrPath() {
	return efIdOrPath;
    }

    public Integer getIndex() {
	// optional
	return index;
    }

    public Integer getLength() {
	// optional
	return length;
    }

}
