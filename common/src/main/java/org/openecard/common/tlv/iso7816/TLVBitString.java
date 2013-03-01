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

import java.util.Arrays;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLVBitString {

    private TLV tlv;
    private byte[] data;

    public TLVBitString(TLV tlv, long tagNumWithClass) throws TLVException {
	if (tlv.getTagNumWithClass() != tagNumWithClass) {
	    throw new TLVException("Type numbers don't match.");
	}

	this.tlv = tlv;
	this.data = tlv.getValue();
	this.data = Arrays.copyOfRange(data, 1, data.length);
    }

    public TLVBitString(TLV tlv) throws TLVException {
	this(tlv, Tag.BITSTRING_TAG.getTagNumWithClass());
    }

    public boolean isSet(int pos) {
	int i = pos / 8;
	int j = 1 << 7 - pos % 8;

	if ((this.data != null) && (this.data.length > i)) {
	    int k = this.data[i];
	    return (k & j) != 0;
	} else {
	    return false;
	}
    }

}
