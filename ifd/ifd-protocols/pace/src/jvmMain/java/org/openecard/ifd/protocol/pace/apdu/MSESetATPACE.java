/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace.apdu;

import java.io.IOException;
import java.math.BigInteger;
import org.openecard.common.apdu.ManageSecurityEnvironment;
import org.openecard.common.apdu.common.CardAPDUOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a MSE:Set AT APDU for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, section 7.5.11.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class MSESetATPACE extends ManageSecurityEnvironment.Set {

    private static final Logger logger = LoggerFactory.getLogger(MSESetATPACE.class);

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     */
    public MSESetATPACE() {
	super((byte) 0xC1, AT);
    }

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     *
     * @param oID PACE object identifier
     * @param passwordID Password type (PIN, PUK, CAN, MRZ)
     * @param domainParamId Id of the domain parameter, or -1 if none is defined.
     */
    public MSESetATPACE(byte[] oID, byte passwordID, int domainParamId) {
	this(oID, passwordID, domainParamId, null);
    }

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     *
     * @param oID PACE object identifier
     * @param passwordID Password type (PIN, PUK, CAN, MRZ)
     * @param domainParamId Id of the domain parameter, or -1 if none is defined.
     * @param chat Certificate Holder Authentication Template
     */
    public MSESetATPACE(byte[] oID, byte passwordID, int domainParamId, byte[] chat) {
	super((byte) 0xC1, AT);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);
	    caos.writeTLV((byte) 0x83, passwordID);
	    if (domainParamId != -1) {
		byte[] domainParamVal = BigInteger.valueOf(domainParamId).toByteArray();
		caos.writeTLV((byte) 0x84, domainParamVal);
	    }

	    if (chat != null) {
		caos.write(chat);
	    }

	    caos.flush();
	} catch (IOException ex) {
	    logger.error(ex.getMessage(), ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}
