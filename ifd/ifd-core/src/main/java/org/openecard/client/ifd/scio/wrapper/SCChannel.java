/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd.scio.wrapper;

import java.util.Arrays;
import java.util.List;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.logging.XLogger;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommandStatus;
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
    private static final XLogger _trace = new XLogger(_logger);

    private final CardChannel channel;
    private final byte[] handle;
    /**
     * Currently active secure messaging protocol.
     */
    private Protocol smProtocol = null;

    public SCChannel(CardChannel channel, byte[] handle) {
	_trace.entry(channel, handle);
        this.channel = channel;
        this.handle = handle;
	_trace.exit();
    }

    public byte[] getHandle() {
	_trace.entry();
	_trace.exit(handle);
        return handle;
    }

    void close() throws CardException {
	_trace.entry();
        if (channel.getChannelNumber() != 0) {
            channel.close(); // this only closes logical channels
        }
	_trace.exit();
    }

    public byte[] transmit(byte[] input, List<byte[]> responses) throws TransmitException, IFDException {
	_trace.entry(input, responses);
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
		_trace.exit(result);
		return result;
	    }
            // verify result
            for (byte[] expected : responses) {
                if (Arrays.equals(expected, sw)) {
		    _trace.exit(result);
                    return result;
                }
            }

            // not an expected result
            TransmitException tex = new TransmitException(result, CardCommandStatus.getMessage(sw));
	    _trace.throwing(tex);
            throw tex;
        } catch (IllegalArgumentException ex) {
	    _trace.catching(ex);
            IFDException ifdex = new IFDException(ex);
	    _trace.throwing(ifdex);
            throw ifdex;
        } catch (CardException ex) {
	    _trace.catching(ex);
            IFDException ifdex = new IFDException(ex);
	    _trace.throwing(ifdex);
            throw ifdex;
        }
    }

    private synchronized boolean isSM() {
	_trace.entry();
	boolean result = this.smProtocol != null;
	_trace.exit(result);
        return result;
    }

    public synchronized void addSecureMessaging(Protocol protocol) {
	_trace.entry(protocol);
        this.smProtocol = protocol;
	_trace.exit();
    }

    public synchronized void removeSecureMessaging() {
	_trace.entry();
        this.smProtocol = null;
	_trace.exit();
    }

}
