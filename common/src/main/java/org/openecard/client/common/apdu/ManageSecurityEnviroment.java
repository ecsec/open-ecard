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

package org.openecard.client.common.apdu;

import org.openecard.client.common.apdu.common.CardCommandAPDU;


/**
 * MANAGE SECURITY ENVIRONMENT command.
 * See ISO/IEC 7816-4 Section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ManageSecurityEnviroment extends CardCommandAPDU {

    /**
     * MANAGE SECURITY ENVIRONMENT command instruction byte
     */
    private static final byte COMMMAND_MSESet_AT = (byte) 0x22;

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    public ManageSecurityEnviroment(byte p1, byte p2) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
    }

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data
     */
    public ManageSecurityEnviroment(byte p1, byte p2, byte[] data) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
	setData(data);
    }

}
