/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.common.apdu.common;


/**
 * Base for all APDU types.
 *
 * @author Moritz Horsch
 */
class CardAPDU {

    // TODO: what is this comment "do not use with bitmask
    /**
     * 0x00 byte. Do not use me with a bit mask!
     */
    protected static final byte x00 = (byte) 0x00;
    /**
     * 0xFF byte. Do not use me with a bit mask!
     */
    protected static final byte xFF = (byte) 0xFF;
    // TODO: make field inaccessible
    /**
     * Data field of the APDU.
     */
    protected byte[] data = new byte[0];

    /**
     * Returns the data field of the APDU.
     *
     * @return Data field. May be empty if no data is available
     */
    public byte[] getData() {
	if (data.length == 0) {
	    return new byte[0];
	} else {
	    byte[] ret = new byte[data.length];
	    System.arraycopy(data, 0, ret, 0, data.length);

	    return ret;
	}
    }

    /**
     * Sets the data field of the APDU.
     *
     * @param data Data field
     */
    public void setData(byte[] data) {
	this.data = data;
    }

}
