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

package org.openecard.client.ifd.scio.wrapper;

import java.util.Arrays;
import java.util.List;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.ifd.scio.EventListener;
import org.openecard.client.ifd.scio.IFDException;
import org.openecard.client.ifd.scio.TransmitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCChannel {

    private static final Logger _logger = LoggerFactory.getLogger(SCChannel.class);

    private final CardChannel channel;
    private final byte[] handle;
    /**
     * Currently active secure messaging protocol.
     */
    private Protocol smProtocol = null;

    public SCChannel(CardChannel channel, byte[] handle) {
        this.channel = channel;
        this.handle = handle;
    }

    public byte[] getHandle() {
        return handle;
    }

    void close() throws CardException {
        if (channel.getChannelNumber() != 0) {
            channel.close(); // this only closes logical channels
        }
    }

    public byte[] transmit(byte[] input, List<byte[]> responses) throws TransmitException, IFDException {
	// pause background threads talking to PCSC
	EventListener.pause();

        // add default value if not present
        if (responses.isEmpty()) {
            responses.add(new byte[]{(byte) 0x90, (byte) 0x00});
        }

        try {
            byte[] inputAPDU = input;
            if (isSM()) {
		_logger.debug("Apply secure messaging to APDU: {}", ByteUtils.toHexString(inputAPDU, true));
                inputAPDU = smProtocol.applySM(inputAPDU);
            }
	    _logger.debug("Send APDU: {}", ByteUtils.toHexString(inputAPDU, true));
            CommandAPDU capdu = new CommandAPDU(inputAPDU);
            ResponseAPDU rapdu = channel.transmit(capdu);
            byte[] result = rapdu.getBytes();
	    _logger.debug("Receive APDU: {}", ByteUtils.toHexString(result, true));
            if (isSM()) {
                result = smProtocol.removeSM(result);
		_logger.debug("Remove secure messaging from APDU: {}", ByteUtils.toHexString(result, true));
            }
            // get status word
            byte[] sw = new byte[2];
            sw[0] = (byte) result[result.length - 2];
            sw[1] = (byte) result[result.length - 1];

	    // return without validation when no expected results given
	    if (responses.isEmpty()) {
		return result;
	    }
            // verify result
            for (byte[] expected : responses) {
                if (Arrays.equals(expected, sw)) {
                    return result;
                }
            }

            // not an expected result
            TransmitException tex = new TransmitException(result, CardCommandStatus.getMessage(sw));
            throw tex;
        } catch (IllegalArgumentException ex) {
            IFDException ifdex = new IFDException(ex);
	    _logger.error(ifdex.getMessage(), ifdex);
            throw ifdex;
        } catch (CardException ex) {
            IFDException ifdex = new IFDException(ex);
	    _logger.error(ifdex.getMessage(), ifdex);
            throw ifdex;
        }
    }

    private synchronized boolean isSM() {
	boolean result = this.smProtocol != null;
        return result;
    }

    public synchronized void addSecureMessaging(Protocol protocol) {
        this.smProtocol = protocol;
    }

    public synchronized void removeSecureMessaging() {
        this.smProtocol = null;
    }

}
