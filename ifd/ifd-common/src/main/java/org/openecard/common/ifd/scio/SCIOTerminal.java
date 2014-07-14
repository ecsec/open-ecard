package org.openecard.common.ifd.scio;

/**
 * Provides an interface for SCIO terminals.
 *
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public interface SCIOTerminal {

    public String getName();

    public SCIOCard connect(String protocol) throws SCIOException;

    public boolean isCardPresent() throws SCIOException;

    public boolean waitForCardPresent(long timeout) throws SCIOException;

    public boolean waitForCardAbsent(long timeout) throws SCIOException;

}
