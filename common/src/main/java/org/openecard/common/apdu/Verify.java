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
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Verify extends CardCommandAPDU {

    /**
     * VERIFY command instruction byte
     */
    private static final byte VERIFY_INS_1 = (byte) 0x20;
    private static final byte VERIFY_INS_2 = (byte) 0x21;

    /**
     * Creates a new VERIFY APDU.
     *
     * @param p2 Parameter p2.
     */
    public Verify(byte p2) {
	super(x00, VERIFY_INS_1, x00, p2);
    }

    /**
     * Creates a new VERIFY APDU.
     *
     * @param p2 Parameter p2.
     * @param data Verification data
     */
    public Verify(byte p2, byte[] data) {
	super(x00, VERIFY_INS_1, x00, p2);
	setData(data);
    }

}
