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

package org.openecard.sal.protocol.genericcryptography.apdu;

import org.openecard.common.apdu.PerformSecurityOperation;


/**
 * Implements a Compute Digital Signature operation.
 * See ISO/IEC 7816-8, section 11.7.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PSOComputeDigitalSignature extends PerformSecurityOperation {

    /**
     * Creates a new PSO Compute Cryptographic Checksum APDU.
     *
     * @param message Message to be signed
     * @param le expected length of the response
     */
    public PSOComputeDigitalSignature(byte[] message, byte le) {
	super((byte) 0x9E, (byte) 0x9A);
	setData(message);
	setLE(le);
    }

}
