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

import org.openecard.common.util.ByteUtils;


/**
 * See ISO/IEC 7816-4 p.60 tab. 87
 *
 * @author Tobias Wich
 */
public class DataCodingByte {

    private final byte data;

    public DataCodingByte(byte data) {
	this.data = data;
    }

    // TODO: implement

    public String toString(String prefix) {
	StringBuilder b = new StringBuilder(64);
	b.append(prefix);
	b.append("DataCoding-Byte: ");
	b.append(ByteUtils.toHexString(new byte[] {data}));

	return b.toString();
    }

    @Override
    public String toString() {
	return toString("");
    }

}
