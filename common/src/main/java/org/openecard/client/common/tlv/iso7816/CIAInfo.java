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
import java.util.LinkedList;
import java.util.List;
import org.openecard.client.common.tlv.*;
import org.openecard.client.common.util.LongUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CIAInfo extends TLV {

    private long version;
    private byte[] serialNumber;
    private String manufacturerID;
    private String label;
    private CardFlags cardFlags;
    private List<TLV> seInfo;
    private TLV recordInfo;
    private List<TLV> supportedAlgorithms;
    private String issuerId;
    private String holderId;
    private TLV lastUpdate;
    private String preferredLanguage;
    private List<TLV> profileIndication;


    public CIAInfo(TLV tlv) throws TLVException {
        super(tlv);

	if (tlv.getTagNumWithClass() != Tag.SequenceTag.getTagNumWithClass()) {
	    throw new TLVException("Data doesn't represent a CIAInfo structure.");
	}

	Parser p = new Parser(tlv.getChild());
	// version
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2))) {
	    version = LongUtils.toLong(p.next(0).getValue());
	} else {
	    throw new TLVException("Expected version tag.");
	}
	// serialNumber
	serialNumber = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    serialNumber = p.next(0).getValue();
	}
	// manufacturer ID
	manufacturerID = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 12))) {
	    manufacturerID = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	// label
	label = null;
	if (p.match(new Tag(TagClass.CONTEXT, true, 0))) {
	    label = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	// cardflags
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
	    cardFlags = new CardFlags(p.next(0));
	} else {
	    throw new TLVException("Expected cardflags tag.");
	}
	// seInfo
	seInfo = new LinkedList<TLV>();
	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    seInfo = new TLVList(p.next(0), new Tag(TagClass.UNIVERSAL, false, 16).getTagNumWithClass()).getContent();
	}
	// recordInfo
	recordInfo = null;
	if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    recordInfo = p.next(0);
	}
	// supportedAlgorithms
	supportedAlgorithms = new LinkedList<TLV>();
	if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    supportedAlgorithms = new TLVList(p.next(0).getChild(), Tag.SequenceTag.getTagNumWithClass()).getContent();
	}
	// issuerId
	issuerId = null;
	if (p.match(new Tag(TagClass.CONTEXT, true, 3))) {
	    issuerId = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	// holderId
	holderId = null;
	if (p.match(new Tag(TagClass.CONTEXT, true, 4))) {
	    holderId = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	// lastUpdate
	lastUpdate = null;
	if (p.match(new Tag(TagClass.CONTEXT, false, 5))) {
	    lastUpdate = p.next(0);
	}
	// preferredLanguage
	preferredLanguage = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 19))) {
	    preferredLanguage = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	// profileIndication
	profileIndication = new LinkedList<TLV>();
	if (p.match(new Tag(TagClass.CONTEXT, false, 6))) {
	    profileIndication = new TLVList(p.next(0), new Tag(TagClass.CONTEXT, false, 6).getTagNumWithClass()).getContent();
	}
    }

}
