/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class PublicKeyChoice extends TLVType {

    private GenericPublicKeyObject<TLV> usedKey;
    private TLV extension;
    private String usedKeyType;

    public PublicKeyChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(Tag.SEQUENCE_TAG)) {
	    usedKeyType = "publicRSAKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    usedKeyType = "publicECKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	}else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    usedKeyType = "publicDHKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	}else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    usedKeyType = "publicDSAKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	}else if (p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    usedKeyType = "publicKEAKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    usedKeyType = "genericPublicKey";
	    usedKey = new GenericPublicKeyObject(p.next(0), TLV.class);
	} else {
	    usedKeyType = "extension";
	    extension = p.next(0);
	}

    }

    /**
     * The method returns the name of the key type which is contained in the PublicKeyChoice object.
     *
     * @return One of the following strings will be returned: <br>
     * - publicRSAKey<br>
     * - publicECKey<br>
     * - publicDHKey<br>
     * - publicDSAKey<br>
     * - publicKEAKey<br>
     * - genericPublicKey<br>
     * - extension
     */
    public String getElementName() {
	return usedKeyType;
    }

    /**
     * Gets the corresponding TLV object to the element name.
     *
     * @return A TLV object containing a PublicKeyObject from ISO7816-15
     */
    public GenericPublicKeyObject<TLV> getElementValue() {
	return usedKey;
    }

}
