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

package org.openecard.sal.protocol.genericcryptography.apdu;

import org.openecard.common.apdu.PerformSecurityOperation;


/**
 * Implements a Hash operation.
 * See ISO/IEC 7816-8, section 11.8.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public final class PSOHash extends PerformSecurityOperation {

    /**
     * P2 value for complete hash generation on the card.
     */
    public static final byte P2_HASH_MESSAGE = (byte) 0x80;

    /**
     * P2 value for setting a HashValue or specific parameters.
     */
    public static final byte P2_SET_HASH_OR_PART = (byte) 0xA0;

    /**
     * Creates a new PSO Hash APDU.
     * APDU: 0x00 0x2A 0x90 0x80|0xA0 Lc data
     *
     * @param data Data to be hashed or hash or parameters for the hash according to ISO7816-8 section 11.8.3.
     * @param p2 P2 value according to ISO7816-98 section 11.8.3. The class provides two public variables for this
     * purpose.
     */
    public PSOHash(byte p2, byte[] data) {
	super((byte) 0x90, p2);
	setData(data);
    }

}
