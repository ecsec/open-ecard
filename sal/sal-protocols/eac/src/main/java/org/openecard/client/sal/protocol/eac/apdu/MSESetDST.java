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
import org.openecard.client.common.apdu.ManageSecurityEnviroment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;
import org.openecard.client.common.logging.LoggingConstants;
import org.slf4j.LoggerFactory;


/**
 * Implements a MSE:Set DST APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.4.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetDST extends ManageSecurityEnviroment {

    /**
     * Creates a new MSE:Set DST APDU.
     */
    public MSESetDST() {
	super((byte) 0x81, (byte) 0xB6);
    }

    /**
     * Creates a new MSE:Set DST APDU.
     *
     * @param chr Certificate Holder Reference
     */
    public MSESetDST(byte[] chr) {
	super((byte) 0x81, (byte) 0xB6);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x83, chr);

	    caos.flush();
	} catch (IOException ex) {
	    LoggerFactory.getLogger(MSESetDST.class).error(LoggingConstants.THROWING, "Exception", ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}
