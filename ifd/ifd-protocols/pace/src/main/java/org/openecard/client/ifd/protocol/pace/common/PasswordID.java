package org.openecard.client.ifd.protocol.pace.common;

/**
 * Implements the different password identifier.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public enum PasswordID {

    MRZ((byte) 0x01),
    CAN((byte) 0x02),
    PIN((byte) 0x03),
    PUK((byte) 0x04);
    private byte b;

    private PasswordID(byte type) {
	this.b = type;
    }

    /**
     * Parses a string to the password ID.
     *
     * @param type Type
     * @return PasswordID
     */
    public static PasswordID parse(String type) {
	if (type.matches("[1-4]")) {
	    return PasswordID.parse(Integer.valueOf(type).byteValue());
	} else {
	    return PasswordID.valueOf(type);
	}
    }

    /**
     * Parses a byte to the password ID.
     *
     * @param type Type
     * @return PasswordID
     */
    public static PasswordID parse(byte type) {
	switch (type) {
	    case (byte) 0x01:
		return PasswordID.MRZ;
	    case (byte) 0x02:
		return PasswordID.CAN;
	    case (byte) 0x03:
		return PasswordID.PIN;
	    case (byte) 0x04:
		return PasswordID.PUK;
	    default:
		return null;
	}
    }

    /**
     * Returns the string representation of the password ID.
     *
     * @return String representation
     */
    public String getString() {
	return this.name();
    }

    /**
     * Returns the byte representation of the password ID.
     *
     * @return Byte representation
     */
    public byte getByte() {
	return b;
    }

    /**
     * Returns the byte representation of the password ID as a string.
     *
     * @return Byte representation as a string
     */
    public String getByteAsString() {
	return getByte() + "";
    }
}
