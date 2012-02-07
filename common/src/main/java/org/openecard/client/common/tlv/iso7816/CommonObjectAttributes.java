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

import java.nio.charset.Charset;
import java.util.List;
import org.openecard.client.common.tlv.*;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CommonObjectAttributes extends TLVType {

    private String label;
    private TLVBitString flags;
    private byte[] authId;
    private Integer userConsent; // 1..15
    private List<TLV> acls;


    public CommonObjectAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 12))) {
	    label = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
	    flags = new TLVBitString(p.next(0), new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    authId = p.next(0).getValue();
	}
	if (p.match(Tag.IntegerTag)) {
	    userConsent = ByteUtils.toInteger(p.next(0).getValue());
	}
	if (p.match(Tag.SequenceTag)) {
	    TLVList list = new TLVList(p.next(0));
	    acls = list.getContent();
	}
    }


}
