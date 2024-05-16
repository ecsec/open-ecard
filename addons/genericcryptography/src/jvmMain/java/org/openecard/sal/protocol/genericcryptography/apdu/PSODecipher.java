/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
 * Implements a Deciper operation.
 * See ISO/IEC 7816-8, section 11.13.
 *
 * @author Dirk Petrautzki
 */
public final class PSODecipher extends PerformSecurityOperation {

    /**
     * Creates a new PSO Decipher APDU.
     *
     * @param message Message to be deciphered
     * @param le expected length of response
     */
    public PSODecipher(byte[] message, byte le) {
	super((byte) 0x80, (byte) 0x86);
	setLE(le);
	setData(message);
    }

}
