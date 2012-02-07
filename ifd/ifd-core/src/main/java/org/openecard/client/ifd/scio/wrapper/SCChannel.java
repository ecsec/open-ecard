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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.ifd.scio.IFDException;
import org.openecard.client.ifd.scio.TransmitException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCChannel {

    private static final Logger _logger = LogManager.getLogger(SCChannel.class.getName());
    private final CardChannel channel;
    private final byte[] handle;
    /**
     * Currently active secure messaging protocol.
     */
    private Protocol smProtocol = null;

    public SCChannel(CardChannel channel, byte[] handle) {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "SCChannel(CardChannel channel, byte[] handle)", new Object[]{channel, handle});
        } // </editor-fold>
        this.channel = channel;
        this.handle = handle;
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "SCChannel(CardChannel channel, byte[] handle)");
        } // </editor-fold>
    }

    public byte[] getHandle() {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getHandle()");
            _logger.exiting(this.getClass().getName(), "getHandle()");
        } // </editor-fold>
        return handle;
    }

    void close() throws CardException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "close()");
        } // </editor-fold>
        if (channel.getChannelNumber() != 0) {
            channel.close(); // this only closes logical channels
        }
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "close()");
        } // </editor-fold>
    }

    public byte[] transmit(byte[] input, List<byte[]> responses) throws TransmitException, IFDException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "transmit(byte[] input, List<byte[]> responses)", new Object[]{input, responses});
        } // </editor-fold>
        // add default value if not present
        if (responses.isEmpty()) {
            responses.add(new byte[]{(byte) 0x90, (byte) 0x00});
        }

        try {
            byte[] inputAPDU = input;
            if (isSM()) {
                // <editor-fold defaultstate="collapsed" desc="log APDU">
                _logger.log(Level.FINE, "Send APDU: {0}", ByteUtils.toHexString(inputAPDU, true));
                // </editor-fold>
                inputAPDU = smProtocol.applySM(inputAPDU);
            }
            // <editor-fold defaultstate="collapsed" desc="log APDU">
            _logger.log(Level.FINE, "Send APDU: {0}", ByteUtils.toHexString(inputAPDU));
            // </editor-fold>
            CommandAPDU capdu = new CommandAPDU(inputAPDU);
            ResponseAPDU rapdu = channel.transmit(capdu);
            byte[] result = rapdu.getBytes();
            // <editor-fold defaultstate="collapsed" desc="log APDU">
            _logger.log(Level.FINE, "Receive  APDU: {0}", ByteUtils.toHexString(result));
            // </editor-fold>
            if (isSM()) {
                result = smProtocol.removeSM(result);
                // <editor-fold defaultstate="collapsed" desc="log APDU">
                _logger.log(Level.FINE, "Receive  APDU: {0}", ByteUtils.toHexString(result));
                // </editor-fold>
            }
            // get status word
            byte[] sw = new byte[2];
            sw[0] = (byte) result[result.length - 2];
            sw[1] = (byte) result[result.length - 1];

            // verify result
            for (byte[] expected : responses) {
                if (Arrays.equals(expected, sw)) {
                    // <editor-fold defaultstate="collapsed" desc="log trace">
                    if (_logger.isLoggable(Level.FINER)) {
                        _logger.exiting(this.getClass().getName(), "transmit(byte[] input, List<byte[]> responses)", result);
                    } // </editor-fold>
                    return result;
                }
            }

            // not an expected result
            TransmitException tex = new TransmitException(result, CardCommandStatus.getMessage(sw));
            // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.logp(Level.FINER, this.getClass().getName(), "transmit(byte[] input, List<byte[]> responses)", tex.getMessage(), tex);
            } // </editor-fold>
            throw tex;
        } catch (IllegalArgumentException ex) {
            IFDException ifdex = new IFDException(ex);
            // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "transmit(byte[] input, List<byte[]> responses)", ifdex.getMessage(), ifdex);
            } // </editor-fold>
            throw ifdex;
        } catch (CardException ex) {
            IFDException ifdex = new IFDException(ex);
            // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "transmit(byte[] input, List<byte[]> responses)", ifdex.getMessage(), ifdex);
            } // </editor-fold>
            throw ifdex;
        }
    }

    private synchronized boolean isSM() {
        return this.smProtocol != null;
    }

    public synchronized void addSecureMessaging(Protocol protocol) {
        this.smProtocol = protocol;
    }

    public synchronized void removeSecureMessaging() {
        this.smProtocol = null;
    }

}
