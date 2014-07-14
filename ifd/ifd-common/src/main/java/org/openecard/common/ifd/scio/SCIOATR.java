package org.openecard.common.ifd.scio;

/**
 * Represent an ISO 7618 Answer To Reset (ATR) or Answer To Select (ATS).
 *
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public final class SCIOATR {

    private final byte[] atr;

    public SCIOATR(byte[] atr) {
	this.atr = atr.clone();
    }

    public byte[] getBytes() {
	return atr.clone();
    }

}
