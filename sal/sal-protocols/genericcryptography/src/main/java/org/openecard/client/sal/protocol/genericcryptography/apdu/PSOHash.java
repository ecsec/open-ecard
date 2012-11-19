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

package org.openecard.client.sal.protocol.genericcryptography.apdu;

import org.openecard.client.common.apdu.PerformSecurityOperation;


/**
 * Implements a Hash operation.
 * See ISO/IEC 7816-8, section 11.8.
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PSOHash extends PerformSecurityOperation {

    /**
     * Creates a new PSO Hash APDU.
     * 
     * @param data Data to be hashed
     */
    public PSOHash(byte[] data){
	super((byte) 0x90, (byte) 0x80);
	setData(data);
    }

}
