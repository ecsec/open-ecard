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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich
 */
public class PrivateRSAKeyAttribute extends TLVType {

    private Path value;
    private byte[] modulusLength;
    // KeyInfo sequence
    private TLV parameters; // NULL, so not interesting
    private TLVBitString operations;

    public PrivateRSAKeyAttribute(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.SEQUENCE_TAG)) {
	    value = new Path(p.next(0));
	} else {
	    throw new TLVException("No value element in structure.");
	}
	if (p.match(Tag.INTEGER_TAG)) {
	    modulusLength = p.next(0).getValue();
	} else {
	    throw new TLVException("No modulusLength element in structure.");
	}
	// only match sequence not Reference (historical)
	if (p.match(Tag.SEQUENCE_TAG)) {
	    Parser p1 = new Parser(p.next(0).getChild());
	    if (p1.match(Tag.NULL_TAG)) {
		parameters = p1.next(0);
	    } else {
		throw new TLVException("No parameters element in structure.");
	    }
	    if (p1.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
		operations = new TLVBitString(p1.next(0), new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());
	    }
	}
    }

}
