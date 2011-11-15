package org.openecard.client.ifd.scio;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.ifd.reader.PCSCFeatures;
import org.openecard.client.ifd.IFDException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCCard {

    private static final Logger _logger = LogManager.getLogger(SCCard.class.getName());

    private final Card card;
    private final SCTerminal terminal;

    private Map<Integer,Integer> featureCodes;

    private final ConcurrentSkipListMap<byte[],SCChannel> scChannels;

    public SCCard(Card card, SCTerminal terminal) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "SCCard(Card card, SCTerminal terminal)", new Object[]{card, terminal});
        } // </editor-fold>
        this.card = card;
	this.terminal = terminal;
	this.scChannels = new ConcurrentSkipListMap<byte[], SCChannel>(new ByteArrayComparator());
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "SCCard(Card card, SCTerminal terminal)");
        } // </editor-fold>
    }


    public byte[] controlCommand(int controlCode, byte[] commandData) throws CardException {
        byte[] result = card.transmitControlCommand(controlCode, commandData);
        return result;
    }

    public Map<Integer,Integer> getFeatureCodes() throws CardException {
        if (featureCodes == null) {
            int code = PCSCFeatures.GET_FEATURE_REQUEST_CTLCODE();
            byte[] response = controlCommand(code, new byte[0]);
            featureCodes = PCSCFeatures.featureMapFromRequest(response);
        }

        return featureCodes;
    }

    public String getProtocol() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getProtocol()");
        } // </editor-fold>
	String p = card.getProtocol();
	if (p.equals("T=0")) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "getProtocol()", ECardConstants.IFD.Protocol.T0);
            } // </editor-fold>
	    return ECardConstants.IFD.Protocol.T0;
	} else if (p.equals("T=1")) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "getProtocol()", ECardConstants.IFD.Protocol.T1);
            } // </editor-fold>
	    return ECardConstants.IFD.Protocol.T1;
	} else {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "getProtocol()", null);
            } // </editor-fold>
	    return null;
	}
    }

    public ATR getATR() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getATR()");
            _logger.exiting(this.getClass().getName(), "getATR()", card.getATR());
        } // </editor-fold>
	return card.getATR();
    }

    public synchronized SCChannel getChannel(byte[] handle) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getChannel(byte[] handle)", handle);
        } // </editor-fold>
	SCChannel ch = scChannels.get(handle);
	if (ch == null) {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "No such slot handle.");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.logp(Level.FINEST, this.getClass().getName(), "getChannel(byte[] handle)", ex.getMessage(), ex);
            } // </editor-fold>
            throw ex;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "getChannel(byte[] handle)", ch);
        } // </editor-fold>
	return ch;
    }

    public synchronized void closeChannel(byte[] handle, boolean reset) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "closeChannel(byte[] handle, boolean reset)", new Object[]{handle, reset});
        } // </editor-fold>
        SCChannel ch = getChannel(handle);
	try {
	    ch.close();
	    scChannels.remove(handle);
	    // close card
	    if (scChannels.isEmpty()) {
		terminal.removeCard();
		card.disconnect(reset);
	    }
	} catch (CardException ex) {
	    IFDException ifdex = new IFDException(ex);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "closeChannel(byte[] handle, boolean reset)", ifdex.getMessage(), ifdex);
            } // </editor-fold>
            throw ifdex;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "closeChannel(byte[] handle, boolean reset)");
        } // </editor-fold>
    }


    public SCChannel addChannel(byte[] handle) throws CardException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "addChannel(byte[] handle)", handle);
        } // </editor-fold>
	CardChannel channel = card.getBasicChannel();
	SCChannel scChannel = new SCChannel(channel, handle);
	scChannels.put(handle, scChannel);
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "addChannel(byte[] handle)", scChannel);
        } // </editor-fold>
	return scChannel;
    }

    synchronized void disconnect() throws CardException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "disconnect()");
        } // </editor-fold>
	card.disconnect(true);
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "disconnect()");
        } // </editor-fold>
    }
    
    public void beginExclusive() throws CardException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "beginExclusive()");
        } // </editor-fold>
        card.beginExclusive();
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "beginExclusive()");
        } // </editor-fold>
    }
    
    public void endExclusive() throws CardException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "endExclusive()");
        } // </editor-fold>
        card.endExclusive();
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "endExclusive()");
        } // </editor-fold>
    }

    public boolean equalCardObj(Card other) {
	return card == other;
    }

}
