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

package org.openecard.common.tlv.iso7816;

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Hans-Martin Haase
 */
public class PrivateECKeyAttributes extends TLVType {

    private Path value;
    // KeyInfo sequence
    // ECDomainParameters - Choice
    private TLV implicitCA; // NULL


    private TLV named; // ObjectIdentifier
    private TLV specified; // SpecifiedECDomain
    // PublicKeyOperations alias for Operations
    private TLVBitString operations;

    public PrivateECKeyAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.SEQUENCE_TAG)) {
	    value = new Path(p.next(0));
	} else {
	    throw new TLVException("No value element in structure.");
	}
	// only match sequence not Reference (historical)
	if (p.match(Tag.SEQUENCE_TAG)) {
	    Parser p1 = new Parser(p.next(0).getChild());
	    if (p1.match(new Tag(TagClass.UNIVERSAL, true, 5))) {
		implicitCA = p1.next(0);
	    } else if(p1.match(new Tag(TagClass.UNIVERSAL, true, 6))) {
		named = p1.next(0);
	    } else if (p1.match(Tag.SEQUENCE_TAG)) {
		specified = p1.next(0);
	    } else {
		throw new TLVException("No parameters element in structure.");
	    }
	    if (p1.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
		operations = new TLVBitString(p1.next(0), new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());
	    }
	}
    }


    public TLV getImplicitCA() {
	return implicitCA;
    }

    public TLV getNamed() {
	return named;
    }

    public TLV getSpecified() {
	return specified;
    }

    public TLVBitString getOperations() {
	return operations;
    }
}
