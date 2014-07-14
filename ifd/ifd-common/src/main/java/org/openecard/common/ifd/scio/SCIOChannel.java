package org.openecard.common.ifd.scio;

import java.nio.ByteBuffer;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.common.CardCommandAPDU;

/**
 * Represents a channel to a smart card.
 *
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public interface SCIOChannel {

    public SCIOCard getCard();

    public int getChannelNumber();

    public CardResponseAPDU transmit(byte[] command) throws SCIOException;

    public CardResponseAPDU transmit(CardCommandAPDU command) throws SCIOException;

    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException;

    public void close() throws SCIOException;

}
