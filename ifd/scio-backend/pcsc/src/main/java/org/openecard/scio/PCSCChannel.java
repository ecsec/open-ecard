package org.openecard.scio;

import java.nio.ByteBuffer;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC/SC channel implementation of the SCIOChannel.
 * 
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public class PCSCChannel implements SCIOChannel {

    private static final Logger logger = LoggerFactory.getLogger(PCSCChannel.class);
    private final CardChannel channel;
    
    public PCSCChannel(CardChannel channel) {
        this.channel = channel;
    }

    @Override
    public PCSCCard getCard() {
        Card ecard = channel.getCard();
        return new PCSCCard(ecard);
    }

    @Override
    public int getChannelNumber() {
        return channel.getChannelNumber();
    }

    @Override
    public CardResponseAPDU transmit(byte[] command) throws SCIOException {
        try {
            ResponseAPDU response = channel.transmit(new CommandAPDU(command));
            return new CardResponseAPDU(response.getBytes());
        } catch (CardException ex) {
            logger.error(ex.getMessage(), ex);
            throw new SCIOException(ex);
        }
    }

    @Override
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException {
        try {
            CommandAPDU ConvertCommand = new CommandAPDU(apdu.toByteArray());
            ResponseAPDU Response = channel.transmit(ConvertCommand);
            return new CardResponseAPDU(Response.getBytes());
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException {
        try {
            return channel.transmit(command, command);
        } catch (CardException ex) {
            logger.error(ex.getMessage(), ex);
            throw new SCIOException(ex);
        }
    }

    @Override
    public void close() throws SCIOException {
        try {
            channel.close();
        } catch (CardException ex) {
            logger.error(ex.getMessage(), ex);
            throw new SCIOException(ex);
        }
    }

}
