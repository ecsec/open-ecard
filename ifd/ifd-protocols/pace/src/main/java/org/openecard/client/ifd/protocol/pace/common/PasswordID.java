package org.openecard.client.ifd.protocol.pace.common;

/**
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

    public static PasswordID parse(String type) {
	if (type.matches("[1-4]")) {
	    return PasswordID.parse(Integer.valueOf(type).byteValue());
	} else {
	    return PasswordID.valueOf(type);
	}
    }

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

    public String getString() {
	return this.name();
    }

    public byte getByte() {
	return b;
    }

    public String getByteAsString() {
	return getByte() + "";
    }
}
