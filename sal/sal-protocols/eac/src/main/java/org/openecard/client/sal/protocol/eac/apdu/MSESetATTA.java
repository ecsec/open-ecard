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
 * Implements a new MSE:Set AT APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, Section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetATTA extends ManageSecurityEnviroment {

    /**
     * Creates a new MSE:Set AT for Terminal Authentication.
     */
    public MSESetATTA() {
	super((byte) 0x81, (byte) 0xA4);
    }

    /**
     * Creates a new MSE:Set AT for Terminal Authentication.
     *
     * @param oID Terminal Authentication object identifier
     * @param chr Certificate Holder Reference
     * @param pkPCD Ephemeral Public Key
     * @param aad Auxiliary Data Verification
     */
    public MSESetATTA(byte[] oID, byte[] chr, byte[] pkPCD, byte[] aad) {
	super((byte) 0x81, (byte) 0xA4);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);

	    if (chr != null) {
		caos.writeTLV((byte) 0x83, chr);
	    }
	    if (pkPCD != null) {
		caos.writeTLV((byte) 0x91, pkPCD);
	    }
	    if (aad != null) {
		caos.write(aad);
	    }

	    caos.flush();
	} catch (IOException ex) {
	    LoggerFactory.getLogger(MSESetATTA.class).error(LoggingConstants.THROWING, "Exception", ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}
