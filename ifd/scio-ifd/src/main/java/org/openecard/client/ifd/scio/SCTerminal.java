package org.openecard.client.ifd.scio;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.Helper;
import org.openecard.client.ifd.reader.PCSCFeatures;
import org.openecard.client.ifd.reader.ExecutePACERequest;
import org.openecard.client.ifd.reader.ExecutePACEResponse;
import org.openecard.client.ifd.IFDException;
import org.openecard.client.ifd.IFDUtils;
import org.openecard.client.ifd.reader.PACECapabilities;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;



/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCTerminal {
    
    private static final Logger _logger = LogManager.getLogger(SCTerminal.class.getName());

    private final CardTerminal terminal;
    private final SCWrapper scwrapper;

    // capabilities entries
    private Boolean acoustic = null;
    private Boolean optic    = null;
    private boolean dispCapRead = false;
    private DisplayCapabilityType dispCap = null;
    private boolean keyCapRead = false;
    private KeyPadCapabilityType keyCap = null;
    private List<Long> PACECapabilities = null;
    // card if available
    private SCCard scCard = null;


    public SCTerminal(CardTerminal terminal, SCWrapper scwrapper) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "SCTerminal(CardTerminal terminal, SCWrapper scwrapper)", new Object[]{terminal, scwrapper});
        } // </editor-fold>
        this.terminal = terminal;
	this.scwrapper = scwrapper;
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "SCTerminal(CardTerminal terminal, SCWrapper scwrapper)");
        } // </editor-fold>
    }


    public String getName() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getName()");
            _logger.exiting(this.getClass().getName(), "getName()", terminal.getName());
        } // </editor-fold>
	return terminal.getName();
    }

    public boolean isCardPresent() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "isCardPresent()");
        } // </editor-fold>
	try {
            Boolean result = terminal.isCardPresent();
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "isCardPresent()", result);
            } // </editor-fold>
	    return result.booleanValue();
	} catch (CardException ex) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "isCardPresent()", Boolean.FALSE);
            } // </editor-fold>
            return false;
	}
    }

    public boolean isConnected() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "isConnected()");
        } // </editor-fold>
        Boolean result = scCard != null;
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "isConnected()", result);
        } // </editor-fold>
	return result.booleanValue();
    }

    public synchronized SCCard getCard() throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getCard()");
        } // </editor-fold>
        if (scCard == null) {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.NO_CARD, "No card inserted in terminal.");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "getCard()", ex.getMessage(), ex);
            } // </editor-fold>
            throw ex;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "getCard()", scCard);
        } // </editor-fold>
	return scCard;
    }


    public synchronized IFDStatusType getStatus() throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getStatus()");
        } // </editor-fold>
	try {
	    IFDStatusType status = new IFDStatusType();
	    status.setIFDName(getName());
	    status.setConnected(true);
	    // set slot status type
	    SlotStatusType stype = new SlotStatusType();
	    status.getSlotStatus().add(stype);
	    boolean cardPresent = isCardPresent();
	    stype.setCardAvailable(cardPresent);
	    stype.setIndex(IFDUtils.getSlotIndex(getName()));
	    // get card status and stuff
	    if (isCardPresent()) {
		if (isConnected()) {
		    ATR atr = scCard.getATR();
		    stype.setATRorATS(atr.getBytes());
		} else {
		    // connect ourselves
		    Card c = terminal.connect("*");
		    ATR atr = c.getATR();
		    stype.setATRorATS(atr.getBytes());
		    c.disconnect(false);
		}
	    }
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "getStatus()", status);
            } // </editor-fold>
	    // ifd status completely constructed
	    return status;
	} catch (Exception ex) {
	    IFDException ifdex = new IFDException(ex);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "getStatus()", ifdex.getMessage(), ifdex);
            } // </editor-fold>
            throw ifdex;
	}
    }


    public boolean equals(String ifdName) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "equals(String ifdName)", ifdName);
	} // </editor-fold>
	Boolean result = terminal.getName().equals(ifdName);
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "equals(ifdName)", result);
	} // </editor-fold>
	return result.booleanValue();
    }

    public boolean equals(CardTerminal other) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "equals(CardTerminal other)", other);
	} // </editor-fold>
	Boolean result = terminal.getName().equals(other.getName());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "terminal.getName().equals(other.getName())", result);
	} // </editor-fold>
	return result.booleanValue();
    }


    ///
    /// state changing methods
    ///

    void updateTerminal() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "updateTerminal()");
	} // </editor-fold>
	if (! isCardPresent()) {
	    scCard = null;
	} else {
	    try {
		if (scCard != null) {
		    // check if it is the same card, else remove
		    Card newCard = terminal.connect("*");
		    if (! scCard.equalCardObj(newCard)) {
			scCard = null;
		    }
		}
	    } catch (CardException ex) {
		// error means delete it anyways
		scCard = null;
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "updateTerminal()");
	} // </editor-fold>
    }

    public synchronized SCChannel connect() throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "connect()");
	} // </editor-fold>
	byte[] handle = scwrapper.createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE);
	// connect card if needed
	if (! isConnected()) {
	    try {
		Card c = terminal.connect("*");
		scCard = new SCCard(c, this);
	    } catch (CardNotPresentException ex) {
		IFDException ifdex = new IFDException(ECardConstants.Minor.IFD.NO_CARD, ex.getMessage());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "connect()", ifdex.getMessage(), ifdex);
		} // </editor-fold>
		throw ifdex;
	    } catch (CardException ex) {
		IFDException ifdex = new IFDException(ex);
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "connect()", ifdex.getMessage(), ifdex);
		} // </editor-fold>
		throw ifdex;
	    }
	}
	try {
	    SCChannel scChannel = scCard.addChannel(handle);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "connect()", scChannel);
	    } // </editor-fold>
	    return scChannel;
	} catch (CardException ex) {
	    IFDException ifdex = new IFDException(ex.getMessage());
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "connect()", ifdex.getMessage(), ifdex);
	    } // </editor-fold>
	    throw ifdex;
	}
    }

    // for use in release context
    synchronized void disconnect() throws CardException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "disconnect()");
	} // </editor-fold>
	if (isConnected()) {
	    scCard.disconnect();
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "disconnect()");
	} // </editor-fold>
    }

    synchronized void removeCard() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "remove()");
	} // </editor-fold>
	scCard = null;
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "remove()");
	} // </editor-fold>
    }


    public synchronized boolean isAcousticSignal() throws IFDException {
	if (acoustic == null) {
	    // no way to ask PCSC this question
	    return false;
	}
	return acoustic.booleanValue();
    }

    public synchronized boolean isOpticalSignal() throws IFDException {
	if (acoustic == null) {
	    // no way to ask PCSC this question
	    return false;
	}
	return acoustic.booleanValue();
    }

    public synchronized DisplayCapabilityType getDisplayCapability() throws IFDException {
	if (dispCapRead == false) {
	    if (isConnected()) {
		try {
		    Map<Integer,Integer> features = getCard().getFeatureCodes();
		    if (features.containsKey(PCSCFeatures.IFD_DISPLAY_PROPERTIES)) {
			byte[] data = getCard().controlCommand(features.get(PCSCFeatures.IFD_DISPLAY_PROPERTIES), new byte[0]);
			if (data != null && data.length == 4) {
			    int lineLength = Helper.convertByteArrayToInt(Arrays.copyOfRange(data, 0, 2));
			    int numLines   = Helper.convertByteArrayToInt(Arrays.copyOfRange(data, 2, 4));
			    if (lineLength > 0 && numLines > 0) {
				dispCap = new DisplayCapabilityType();
				dispCap.setIndex(BigInteger.ZERO);
				dispCap.setColumns(BigInteger.valueOf(lineLength));
				dispCap.setLines(BigInteger.valueOf(numLines));
			    }
			}
		    }
		    // regardless whether the data has been successfully extracted, or not, the data has been read
		    dispCapRead = true;
		} catch (CardException ex) {
		    throw new IFDException(ex);
		}
	    }
	}
	return dispCap;
    }

    public synchronized KeyPadCapabilityType getKeypadCapability() throws IFDException {
	if (keyCapRead == false) {
	    if (isConnected()) {
		try {
		    Map<Integer,Integer> features = getCard().getFeatureCodes();
		    if (features.containsKey(PCSCFeatures.IFD_PIN_PROPERTIES)) {
			byte[] data = getCard().controlCommand(features.get(PCSCFeatures.IFD_PIN_PROPERTIES), new byte[0]);
			if (data != null && data.length == 4) {
			    int wcdLayout = Helper.convertByteArrayToInt(Arrays.copyOfRange(data, 0, 2));
			    byte entryValidation = data[2];
			    byte timeOut2 = data[3];
			    // TODO: extract number of keys somehow
			}
		    }
		    // regardless whether the data has been successfully extracted, or not, the data has been read
		    keyCapRead = true;
		} catch (CardException ex) {
		    throw new IFDException(ex);
		}
	    }
	}
	return keyCap;
    }

    public synchronized Integer getPaceCtrlCode() throws IFDException {
	if (isConnected()) {
	    try {
		Map<Integer,Integer> features = getCard().getFeatureCodes();
		if (features.containsKey(PCSCFeatures.EXECUTE_PACE)) {
		    return features.get(PCSCFeatures.EXECUTE_PACE);
		}
	    } catch (CardException ex) {
		throw new IFDException(ex);
	    }
	}
	return null;
    }

    public synchronized boolean supportsPace() throws IFDException {
	return getPaceCtrlCode() != null;
    }

    public List<Long> getPACECapabilities() throws IFDException {
	List<Long> result = new LinkedList<Long>();

	if (PACECapabilities == null) {
	    if (isConnected()) {
		if (supportsPace()) {
		    int ctrlCode = getPaceCtrlCode();
		    byte[] getCapabilityRequest = new ExecutePACERequest(ExecutePACERequest.Function.GetReaderPACECapabilities).toBytes();
		    try {
			byte[] response = getCard().controlCommand(ctrlCode, getCapabilityRequest);
			ExecutePACEResponse paceResponse = new ExecutePACEResponse(response);
			if (paceResponse.isError()) {
			    throw new IFDException(paceResponse.getResult());
			}
			PACECapabilities cap = new PACECapabilities(paceResponse.getData());
			PACECapabilities = cap.getFeatures();
			result.addAll(PACECapabilities);
		    } catch (CardException e) {
			IFDException ex = new IFDException(e);
			throw ex;
		    }
		}
	    }
	} else {
	    result.addAll(PACECapabilities);
	}

	return Collections.unmodifiableList(result);
    }

    public synchronized Integer getPinCompareCtrlCode() throws IFDException {
	if (isConnected()) {
	    try {
		Map<Integer,Integer> features = getCard().getFeatureCodes();
		if (features.containsKey(PCSCFeatures.VERIFY_PIN_DIRECT)) {
		    return features.get(PCSCFeatures.VERIFY_PIN_DIRECT);
		}
	    } catch (CardException ex) {
		throw new IFDException(ex);
	    }
	}
	return null;
    }

    public synchronized boolean supportsPinCompare() throws IFDException {
	return getPinCompareCtrlCode() != null;
    }


}
