/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.crypto.common.asn1.eac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ListIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.eac.EACTags;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.iso7816.TLVList;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier;


/**
 * Data object for the AuthenticatedAuxiliaryData according to TR-03110-3 Sec. A.6.5.
 *
 * @author Tobias Wich
 */
public class AuthenticatedAuxiliaryData extends TLVList {

    private final HashMap<ASN1ObjectIdentifier, DiscretionaryDataTemplate> templates;
    private boolean empty = false;

    public AuthenticatedAuxiliaryData(@Nonnull TLV tlv) throws TLVException {
	super(tlv, EACTags.AUTHENTIFICATION_DATA);
	templates = new HashMap<>();
	// convert content to DiscretionaryDataTemplates
	ArrayList<DiscretionaryDataTemplate> ts = new ArrayList<>();
	for (TLV next : getContent()) {
	    DiscretionaryDataTemplate d = new DiscretionaryDataTemplate(next);
	    ts.add(d);
	}
	// Fill map by looking at the objects in reverse order, because the TR states only the last element of a given
	// OID should be used.
	ListIterator<DiscretionaryDataTemplate> i = ts.listIterator(ts.size());
	while (i.hasPrevious()) {
	    DiscretionaryDataTemplate next = i.previous();
	    ASN1ObjectIdentifier oid = next.getObjectIdentifier();
	    if (! templates.containsKey(oid)) {
		templates.put(oid, next);
	    }
	}
    }

    public AuthenticatedAuxiliaryData(@Nullable byte[] data) throws TLVException {
	this(emptyOrStructure(data));
	if (data == null) {
	    empty = true;
	}
    }

    private static TLV emptyOrStructure(byte[] data) throws TLVException {
	if (data == null) {
	    TLV tlv = new TLV();
	    tlv.setTagNumWithClass(EACTags.AUTHENTIFICATION_DATA);
	    return tlv;
	} else {
	    return TLV.fromBER(data);
	}
    }

    public byte[] getData() {
	return empty ? null : this.tlv.toBER();
    }


    public Calendar getAgeVerificationData() {
	ASN1ObjectIdentifier reqOID = new ASN1ObjectIdentifier(EACObjectIdentifier.id_DateOfBirth);
	if (templates.containsKey(reqOID)) {
	    DiscretionaryDataTemplate t = templates.get(reqOID);
	    return convertDate(t.getDiscretionaryData());
	} else {
	    return null;
	}
    }

    public Calendar getDocumentValidityVerificationData() {
	ASN1ObjectIdentifier reqOID = new ASN1ObjectIdentifier(EACObjectIdentifier.id_DateOfExpiry);
	if (templates.containsKey(reqOID)) {
	    DiscretionaryDataTemplate t = templates.get(reqOID);
	    return convertDate(t.getDiscretionaryData());
	} else {
	    return null;
	}
    }

    public byte[] getCommunityIDVerificationData() {
	ASN1ObjectIdentifier reqOID = new ASN1ObjectIdentifier(EACObjectIdentifier.id_CommunityID);
	if (templates.containsKey(reqOID)) {
	    DiscretionaryDataTemplate t = templates.get(reqOID);
	    return ByteUtils.clone(t.getDiscretionaryData());
	} else {
	    return null;
	}
    }

    private Calendar convertDate(byte[] discretionaryData) {
	if (discretionaryData.length != 8) {
	    throw new IllegalArgumentException("Given value in discretionary data does not represent a date.");
	}
	// the date is in the form YYYYMMDD and encoded as characters
	String yearStr = new String(Arrays.copyOfRange(discretionaryData, 0, 4));
	int year = Integer.parseInt(yearStr);
	String monthStr = new String(Arrays.copyOfRange(discretionaryData, 4, 6));
	int month = Integer.parseInt(monthStr);
	String dayStr = new String(Arrays.copyOfRange(discretionaryData, 6, 8));
	int day = Integer.parseInt(dayStr);
	// convert to Date object
	Calendar c = Calendar.getInstance();
	c.set(year, month, day);
	return c;
    }

}
