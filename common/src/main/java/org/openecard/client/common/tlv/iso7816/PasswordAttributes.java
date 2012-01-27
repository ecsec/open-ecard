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
public class PasswordAttributes extends TLVType {

    private TLVBitString passwordFlags;
    private int passwordType; // enum PasswordType
    private int minLength;
    private int storedLength;
    private Integer maxLength;
    private Integer passwordReference;
    private Byte padChar;
    private TLV lastPasswordChange;
    private Path path;


    public PasswordAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.BitstringTag)) {
	    passwordFlags = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("passwordFlags element missing.");
	}
	if (p.match(Tag.EnumeratedTag)) {
	    passwordType = IntegerUtils.toInteger(p.next(0).getValue());
	} else {
	    throw new TLVException("passwordType element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    minLength = IntegerUtils.toInteger(p.next(0).getValue());
	} else {
	    throw new TLVException("minLength element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    storedLength = IntegerUtils.toInteger(p.next(0).getValue());
	} else {
	    throw new TLVException("storedLength element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    maxLength = IntegerUtils.toInteger(p.next(0).getValue());
	}
	if (p.match(new Tag(TagClass.CONTEXT, true, 0))) {
	    passwordReference = IntegerUtils.toInteger(p.next(0).getValue());
	}
	if (p.match(Tag.OctetstringTag)) {
	    padChar = p.next(0).getValue()[0];
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 24))) {
	    lastPasswordChange = p.next(0);
	}
	if (p.match(Tag.SequenceTag)) {
	    path = new Path(p.next(0));
	}
    }

}
