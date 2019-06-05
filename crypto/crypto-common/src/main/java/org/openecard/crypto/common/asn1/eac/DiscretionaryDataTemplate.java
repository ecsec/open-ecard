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

import javax.annotation.Nonnull;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.eac.EACTags;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;
import org.openecard.common.tlv.iso7816.TLVList;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 * Data object for the DiscretionaryDataTemplate described in BSI TR-03110-3 Sec. A.6.5.1.
 *
 * @author Tobias Wich
 */
public class DiscretionaryDataTemplate extends TLVList {

    private final ASN1ObjectIdentifier objId;
    private final byte[] data;

    public DiscretionaryDataTemplate(@Nonnull TLV tlv) throws TLVException {
	super(tlv, new Tag(TagClass.APPLICATION, false, EACTags.DISCRETIONARY_DATA_OBJECTS));

	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.OID_TAG)) {
	    try {
		String oidStr = ObjectIdentifierUtils.toString(p.next(0).getValue());
		objId = new ASN1ObjectIdentifier(oidStr);
	    } catch (IllegalArgumentException ex) {
		throw new TLVException(ex);
	    }
	} else {
	    throw new TLVException("Object Identifier is missing in DiscretionaryDataTemplate.");
	}
	if (p.match(new Tag(TagClass.APPLICATION, true, EACTags.DISCRETIONARY_DATA))) {
	    data = p.next(0).getValue();
	} else {
	    throw new TLVException("Discretionary Data is missing in DiscretionaryDataTemplate.");
	}
    }

    public ASN1ObjectIdentifier getObjectIdentifier() {
	return objId;
    }

    public byte[] getDiscretionaryData() {
	return ByteUtils.clone(data);
    }

}
