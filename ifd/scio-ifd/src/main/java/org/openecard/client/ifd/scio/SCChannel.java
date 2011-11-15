package org.openecard.client.ifd.scio;

import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.ifd.IFDException;
import org.openecard.client.ifd.TransmitException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCChannel {

    private static final Logger _logger = LogManager.getLogger(SCChannel.class.getName());
    
    private final CardChannel channel;
    private final byte[] handle;

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
            CommandAPDU capdu = new CommandAPDU(input);
	    ResponseAPDU rapdu = channel.transmit(capdu);
	    byte[] result = rapdu.getBytes();
	    // get status word
	    byte[] sw = new byte[2];
	    sw[0] = (byte) rapdu.getSW1();
	    sw[1] = (byte) rapdu.getSW2();

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

}
