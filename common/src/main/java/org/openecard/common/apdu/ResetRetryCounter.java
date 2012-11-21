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
 * RESET RETRY COUNTER command
 * See ISO/IEC 7816-4 Section 7.5.10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ResetRetryCounter extends CardCommandAPDU {

    /**
     * RESET RETRY COUNTER command instruction byte
     */
    private static final byte RESET_RETRY_COUNTER_INS = (byte) 0x2C;

    /**
     * Creates a RESET RETRY COUNTER APDU.
     *
     * @param password Password
     * @param type Password type
     */
    public ResetRetryCounter(byte[] password, byte type) {
	super(x00, RESET_RETRY_COUNTER_INS, (byte) 0x02, type);
	setData(password);
    }

    /**
     * Creates a RESET RETRY COUNTER APDU.
     *
     * @param type Password type
     */
    public ResetRetryCounter(byte type) {
	super(x00, RESET_RETRY_COUNTER_INS, (byte) 0x03, type);
    }

}
