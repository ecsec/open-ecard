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
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Path {

    private final TLV tlv;

    private byte[] efIdOrPath;
    private Integer index;
    private Integer length;

    public Path(TLV tlv) throws TLVException {
	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    efIdOrPath = p.next(0).getValue();
	} else {
	    throw new TLVException("No efIdOrPath given.");
	}
	index = null;
	length = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2)) && p.matchLA(1, new Tag(TagClass.CONTEXT, true, 0))) {
	    index = ByteUtils.toInteger(p.next(0).getValue());
	    length = ByteUtils.toInteger(p.next(0).getValue());
	}
    }

    public Path(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public byte[] efIdOrPath() {
	return efIdOrPath;
    }

    public Integer getIndex() {
	// optional
	return index;
    }

    public Integer getLength() {
	// optional
	return length;
    }

}
