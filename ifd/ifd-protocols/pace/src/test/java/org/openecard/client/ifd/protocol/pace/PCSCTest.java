/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class PCSCTest {

    private static final Logger logger = LoggerFactory.getLogger(PCSCTest.class);
    private CardChannel connection;

    @Ignore
    @Test
    public void PCSCTest() {
	connect();

	byte[] selectmf = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0x3F, (byte) 0x00};
	try {
	    logger.info("Send APDU {}", ByteUtils.toHexString(selectmf));
	    ResponseAPDU response = connection.transmit(new CommandAPDU(selectmf));
	    logger.info("Receive APDU {}", ByteUtils.toHexString(response.getBytes()));
	} catch (CardException ex) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
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
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	}
    }
}
