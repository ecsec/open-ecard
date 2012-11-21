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

package org.openecard.common.apdu;

import org.openecard.common.apdu.common.CardCommandAPDU;


/**
 * INTERNAL AUTHENTICATE command
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class InternalAuthenticate extends CardCommandAPDU {

    /**
     * INTERNAL AUTHENTICATION command instruction byte
     */
    private static final byte INTERNAL_AUTHENTICATE_INS = (byte) 0x88;

    /**
     * Creates a new INTERNAL AUTHENTICATE command.
     * APDU: 0x00 0x88 0x00 0x00
     */
    public InternalAuthenticate() {
	super(x00, INTERNAL_AUTHENTICATE_INS, x00, x00);
    }

    /**
     * Creates a new INTERNAL AUTHENTICATE command.
     * APDU: 0x00 0x88 0x00 0x00 LC DATA
     *
     * @param data Data
     */
    public InternalAuthenticate(byte[] data) {
	super(x00, INTERNAL_AUTHENTICATE_INS, x00, x00);
	setData(data);
    }

}
