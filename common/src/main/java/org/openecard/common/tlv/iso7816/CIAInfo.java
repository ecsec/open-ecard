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

package org.openecard.common.tlv.iso7816;

import java.nio.charset.Charset;
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

	if (tlv.getTagNumWithClass() != Tag.SEQUENCE_TAG.getTagNumWithClass()) {
	    throw new TLVException("Data doesn't represent a CIAInfo structure.");
	}

	Parser p = new Parser(tlv.getChild());
	// version
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2))) {
	    version = ByteUtils.toLong(p.next(0).getValue());
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
	    supportedAlgorithms = new TLVList(p.next(0).getChild(), Tag.SEQUENCE_TAG.getTagNumWithClass()).getContent();
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

    public long getVersion() {
	return version;
    }

    public byte[] getSerialNumber() {
	return serialNumber;
    }

    public String getManufacturerID() {
	return manufacturerID;
    }

    public String getLabel() {
	return label;
    }

    public CardFlags getCardFlags() {
	return cardFlags;
    }

    public List<TLV> getSeInfo() {
	return seInfo;
    }

    public TLV getRecordInfo() {
	return recordInfo;
    }

    public List<TLV> getSupportedAlgorithms() {
	return supportedAlgorithms;
    }

    public String getIssuerId() {
	return issuerId;
    }

    public String getHolderId() {
	return holderId;
    }

    public TLV getLastUpdate() {
	return lastUpdate;
    }

    public String getPreferredLanguage() {
	return preferredLanguage;
    }

    public List<TLV> getProfileIndication() {
	return profileIndication;
    }

}
