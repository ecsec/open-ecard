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
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class CommonCertificateAttributes extends TLVType {

    /**
     * General identifier used to identify the corresponding private/public key.
     */
    private byte[] id;

    private boolean authority = false;

    /**
     * Credential identifier.
     */
    private TLV identifier;

    private TLV certHash;

    private TLV trustedUsage;

    private TLV identifiers;

    private TLV validity;

    public CommonCertificateAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2))) {
	    id = p.next(0).getValue();
	} else {
	    throw new TLVException("Missing ID field in the CommonCertificateAttributes");
	}

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 1))) {
	    if (ByteUtils.toInteger(p.next(0).getValue()) == 0) {
		authority = false;
	    } else {
		authority = true;
	    }
	}

	if (p.match(Tag.SEQUENCE_TAG)) {
	    identifier = p.next(0);
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    certHash = p.next(0);
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    trustedUsage = p.next(0);
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    identifiers = p.next(0);
	}

	/*
	 * NOTE: context tag 3 is reserved for historical reasons (PKCS#15)
	 */

	if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    validity = p.next(0);
	}
    }

    /**
     * Get the value of the id property.
     * The id corresponds to a id of a private or public key.
     *
     * @return The id of the certificate which corresponds to a private or public key.
     */
    public byte[] getId() {
	return id;
    }

    public boolean isAuthority() {
	return authority;
    }

    /**
     * Get the value of the identifier property.
     * The identifier is here a credential identifier.
     *
     * @return
     */
    public TLV getIdentifier() {
	return identifier;
    }

    public TLV getCertHash() {
	return certHash;
    }

    public TLV getTrustedUsage() {
	return trustedUsage;
    }

    /**
     * List of credential identifiers.
     *
     * @return
     */
    public TLV getIdentifiers() {
	return identifiers;
    }

    public TLV getValidity() {
	return validity;
    }

}
