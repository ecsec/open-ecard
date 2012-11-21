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
