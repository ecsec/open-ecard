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

package org.openecard.ifd.scio.wrapper;

import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.*;
import javax.smartcardio.*;
import org.openecard.common.ECardConstants;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.util.ByteUtils;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.IFDUtils;
import org.openecard.ifd.scio.reader.ExecutePACERequest;
import org.openecard.ifd.scio.reader.ExecutePACEResponse;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCTerminal {

    private static final Logger _logger = LoggerFactory.getLogger(SCTerminal.class);

    private final CardTerminal terminal;
    private final SCWrapper scwrapper;

    // capabilities entries
    private Boolean acoustic = null;
    private Boolean optic    = null;
    private boolean dispCapRead = false;
    private DisplayCapabilityType dispCap = null;
    private boolean keyCapRead = false;
    private KeyPadCapabilityType keyCap = null;
    private List<PACECapabilities.PACECapability> PACECapabilities = null;
    // card if available
    private SCCard scCard = null;


    public SCTerminal(CardTerminal terminal, SCWrapper scwrapper) {
	this.terminal = terminal;
	this.scwrapper = scwrapper;
    }


    public String getName() {
	return terminal.getName();
    }

    public boolean isCardPresent() {
	try {
	    boolean result = terminal.isCardPresent();
	    // isCardPresent has a bug in OS X, we've to try to connect
	    // to check if a card is present
	    if (!result && System.getProperty("os.name").contains("OS X")) {
		if (scCard != null) {
		    result = true;
		} else {
		    try {
			terminal.connect("*");
			// card present
			result = true;
		    } catch (CardException ignore) {
			// no card present
		    }
		}
	    }
	    return result;
	} catch (CardException ex) {
	    return false;
	}
    }

    public boolean isConnected() {
	Boolean result = scCard != null;
	return result.booleanValue();
    }

    public synchronized SCCard getCard() throws IFDException {
	if (scCard == null) {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.NO_CARD, "No card inserted in terminal.");
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
	return scCard;
    }


    public synchronized IFDStatusType getStatus() throws IFDException {
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
	    if (cardPresent) {
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
	    // ifd status completely constructed
	    return status;
	} catch (Exception ex) {
	    IFDException ifdex = new IFDException(ex);
	    _logger.warn(ifdex.getMessage(), ifdex);
	    throw ifdex;
	}
    }


    public boolean equals(String ifdName) {
	Boolean result = terminal.getName().equals(ifdName);
	return result.booleanValue();
    }

    public boolean equals(CardTerminal other) {
	Boolean result = terminal.getName().equals(other.getName());
	return result.booleanValue();
    }


    ///
    /// state changing methods
    ///

    void updateTerminal() {
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
    }

    public synchronized SCChannel connect() throws IFDException {
	byte[] handle = scwrapper.createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE);
	// connect card if needed
	if (! isConnected()) {
	    try {
		Card c = terminal.connect("*");
		scCard = new SCCard(c, this);
	    } catch (CardNotPresentException ex) {
		IFDException ifdex = new IFDException(ECardConstants.Minor.IFD.NO_CARD, ex.getMessage());
		_logger.warn(ifdex.getMessage(), ifdex);
		throw ifdex;
	    } catch (CardException ex) {
		IFDException ifdex = new IFDException(ex);
		_logger.warn(ifdex.getMessage(), ifdex);
		throw ifdex;
	    }
	}
	try {
	    SCChannel scChannel = scCard.addChannel(handle);
	    return scChannel;
	} catch (CardException ex) {
	    IFDException ifdex = new IFDException(ex.getMessage());
	    _logger.warn(ifdex.getMessage(), ifdex);
	    throw ifdex;
	}
    }

    // for use in release context
    synchronized void disconnect() throws CardException {
	if (isConnected()) {
	    scCard.disconnect();
	}
    }

    synchronized void removeCard() {
	scCard = null;
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
			    int lineLength = ByteUtils.toInteger(Arrays.copyOfRange(data, 0, 2));
			    int numLines   = ByteUtils.toInteger(Arrays.copyOfRange(data, 2, 4));
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
			    int wcdLayout = ByteUtils.toInteger(Arrays.copyOfRange(data, 0, 2));
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

    public synchronized byte[] executeCtrlCode(int featureCode, byte[] command) throws IFDException {
	if (isConnected()) {
	    try {
		Map<Integer,Integer> features = getCard().getFeatureCodes();
		if (features.containsKey(featureCode)) {
		    Integer code = features.get(featureCode);
		    byte[] result = getCard().controlCommand(code, command);
		    return result;
		} else {
		    throw new IFDException("The requested control code is not supported by the terminal");
		}
	    } catch (CardException ex) {
		throw new IFDException(ex);
	    }
	}
	throw new IFDException(ECardConstants.Minor.Disp.INVALID_CHANNEL_HANDLE, "No connection is established with the reader.");
    }

    private synchronized Integer getPaceCtrlCode() throws IFDException {
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

    public List<PACECapabilities.PACECapability> getPACECapabilities() throws IFDException {
	List<PACECapabilities.PACECapability> result = new LinkedList<PACECapabilities.PACECapability>();

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
			PACECapabilities = cap.getFeaturesEnum();
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

    private synchronized Integer getPinCompareCtrlCode() throws IFDException {
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
