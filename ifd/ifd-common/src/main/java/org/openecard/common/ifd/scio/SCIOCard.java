package org.openecard.common.ifd.scio;

/**
 * Represents a ISO 7816 smart card.
 *
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public interface SCIOCard {

    public SCIOATR getATR();

    public String getProtocol();

    public SCIOChannel getBasicChannel();

    public SCIOChannel openLogicalChannel() throws SCIOException;

    public void beginExclusive() throws SCIOException;

    public void endExclusive() throws SCIOException;

    public byte[] transmitControlCommand(int controlCode, byte[] command) throws SCIOException;

    public void disconnect(boolean reset) throws SCIOException;

}
