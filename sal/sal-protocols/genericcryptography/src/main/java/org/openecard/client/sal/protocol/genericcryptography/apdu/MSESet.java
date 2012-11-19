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

import java.io.IOException;
import org.openecard.client.common.apdu.ManageSecurityEnvironment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;
import org.openecard.client.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESet extends ManageSecurityEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(MSESet.class);

    /**
     * Creates a MSE:Set APDU.
     *
     * @param oID Object identifier
     * @param keyID Key reference
     */
    public MSESet(byte p2, byte[] oID, byte[] keyID) {
	super((byte) 0x41, p2);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);
	    caos.writeTLV((byte) 0x84, ByteUtils.cutLeadingNullBytes(keyID));
	    caos.flush();
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}
