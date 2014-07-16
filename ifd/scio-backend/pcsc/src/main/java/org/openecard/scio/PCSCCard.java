package org.openecard.scio;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC/SC card implementation of the SCIOCard.

 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public class PCSCCard implements SCIOCard {

    private static final Logger logger = LoggerFactory.getLogger(PCSCCard.class);
    private final Card card;

    public PCSCCard(Card card) {
	this.card = card;
    }

    @Override
    public SCIOATR getATR() {
	ATR result = card.getATR();
	SCIOATR getRedult = new SCIOATR(result.getBytes());
	return getRedult;
    }

    @Override
    public String getProtocol() {
	return card.getProtocol();
    }

    @Override
    public PCSCChannel getBasicChannel() {
	return new PCSCChannel(card.getBasicChannel());
    }

    @Override
    public PCSCChannel openLogicalChannel() throws SCIOException {
	try {
	    return new PCSCChannel(card.openLogicalChannel());
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public void beginExclusive() throws SCIOException {
	try {
	    card.beginExclusive();
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public void endExclusive() throws SCIOException {
	try {
	    card.endExclusive();
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws SCIOException {
	try {
	    return card.transmitControlCommand(controlCode, command);
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	try {
	    card.disconnect(reset);
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }
}
