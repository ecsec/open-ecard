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
package org.openecard.client.sal.protocol.eac.apdu;

import java.io.IOException;
import org.openecard.client.common.apdu.ManageSecurityEnvironment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;
import org.openecard.client.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a MSE:Set AT APDU for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, section 7.5.11.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetATCA extends ManageSecurityEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(MSESetATCA.class);

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     */
    public MSESetATCA() {
	super((byte) 0x41, AT);
    }

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     *
     * @param oid Chip Authentication object identifier
     */
    public MSESetATCA(byte[] oid) {
	this(oid, null);
    }

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     *
     * @param oID Chip Authentication object identifier
     * @param keyID Reference of a private key
     */
    public MSESetATCA(byte[] oID, byte[] keyID) {
	super((byte) 0x41, AT);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);

	    if (keyID != null) {
		caos.writeTLV((byte) 0x84, ByteUtils.cutLeadingNullBytes(keyID));
	    }

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
