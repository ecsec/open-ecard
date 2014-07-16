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

import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.Nonnull;
import org.openecard.common.ECardConstants;
import org.openecard.ifd.scio.EventListener;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCCard {

    private static final Logger _logger = LoggerFactory.getLogger(SCCard.class);

    private final SCIOCard card;
    private final SCTerminal terminal;

    private Map<Integer, Integer> featureCodes;

    private final ConcurrentSkipListMap<byte[], SCChannel> scChannels;

    public SCCard(SCIOCard card, SCTerminal terminal) {
	this.card = card;
	this.terminal = terminal;
	this.scChannels = new ConcurrentSkipListMap<byte[], SCChannel>(new ByteArrayComparator());
    }

    public byte[] controlCommand(int controlCode, byte[] commandData) throws SCIOException {
	// pause background threads talking to PCSC
	EventListener.pause();

	byte[] result = card.transmitControlCommand(controlCode, commandData);
	return result;
    }

    @Nonnull
    public Map<Integer, Integer> getFeatureCodes() throws SCIOException {
	if (featureCodes == null) {
	    int code = PCSCFeatures.GET_FEATURE_REQUEST_CTLCODE();
	    try {
		byte[] response = controlCommand(code, new byte[0]);
		featureCodes = PCSCFeatures.featureMapFromRequest(response);
	    } catch (SCIOException ex) {
		// TODO: remove this workaround by supporting feature requests under all systems and all readers
		_logger.warn("Unable to request features from reader.", ex);
		featureCodes = new HashMap<Integer, Integer>();
	    }
	}

	return featureCodes;
    }

    public String getProtocol() {
	String p = card.getProtocol();
	if (p.equals("T=0")) {
	    return ECardConstants.IFD.Protocol.T0;
	} else if (p.equals("T=1")) {
	    return ECardConstants.IFD.Protocol.T1;
	} else {
	    return null;
	}
    }

    public SCIOATR getATR() {
	return card.getATR();
    }

    public synchronized SCChannel getChannel(byte[] handle) throws IFDException {
	SCChannel ch = scChannels.get(handle);
	if (ch == null) {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "No such slot handle.");
	    _logger.debug(ex.getMessage(), ex);
	    throw ex;
	}
	return ch;
    }

    public synchronized void closeChannel(byte[] handle, boolean reset) throws IFDException {
	SCChannel ch = getChannel(handle);
	try {
	    ch.close();
	    scChannels.remove(handle);
	    // close card
	    if (scChannels.isEmpty()) {
		terminal.removeCard();
		card.disconnect(reset);
	    }
	} catch (SCIOException ex) {
	    IFDException ifdex = new IFDException(ex);
	    _logger.warn(ifdex.getMessage(), ifdex);
	    throw ifdex;
	}
    }

    public SCChannel addChannel(byte[] handle) throws SCIOException {
	SCIOChannel scioChannel = card.getBasicChannel();
	SCChannel scChannel = new SCChannel(scioChannel, handle);
	scChannels.put(handle, scChannel);
	return scChannel;
    }

    synchronized void disconnect() throws SCIOException {
	card.disconnect(true);
    }

    public void beginExclusive() throws SCIOException {
	card.beginExclusive();
    }

    public void endExclusive() throws SCIOException {
	card.endExclusive();
    }

    public boolean equalCardObj(SCIOCard other) {
	return card == other;
    }

}
