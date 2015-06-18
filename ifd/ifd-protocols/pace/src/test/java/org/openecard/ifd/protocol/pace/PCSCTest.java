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

package org.openecard.ifd.protocol.pace;

import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import org.openecard.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch
 */
public class PCSCTest {

    private static final Logger logger = LoggerFactory.getLogger(PCSCTest.class);
    private CardChannel connection;

    @Test(enabled = false)
    public void PCSCTest() {
	connect();

	byte[] selectmf = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0x3F, (byte) 0x00};
	try {
	    logger.info("Send APDU {}", ByteUtils.toHexString(selectmf));
	    ResponseAPDU response = connection.transmit(new CommandAPDU(selectmf));
	    logger.info("Receive APDU {}", ByteUtils.toHexString(response.getBytes()));
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

    private void connect() {
	try {
//            File libPcscLite = new File("/usr/lib/libpcsclite.so.1");
//            if (libPcscLite.exists()) {
//                System.setProperty("sun.security.smartcardio.library", libPcscLite.getAbsolutePath());
//            }
	    TerminalFactory t = TerminalFactory.getInstance("PC/SCs", null);
//            TerminalFactory t = TerminalFactory.getDefault();
	    CardTerminals c = t.terminals();
	    logger.info("Card terminals: {}", c.list().size());

	    List terminals = c.list();
	    if (terminals.isEmpty()) {
		logger.info("No presend cards!");
	    } else {
		for (int i = 0; i < terminals.size(); i++) {
		    CardTerminal ct = (CardTerminal) terminals.get(i);
		    if (ct.isCardPresent()) {
			Card card = ct.connect("*");
			connection = card.getBasicChannel();
			logger.info("Card found at card terminal " + i + ": ", card.toString());
		    }
		}
	    }

	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

}
