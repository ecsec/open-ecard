package org.openecard.client.common.tlv;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public enum TagClass {

    UNIVERSAL((byte)0), APPLICATION((byte)1), CONTEXT((byte)2), PRIVATE((byte)3);

    public final byte num;

    private TagClass(byte num) {
	this.num = num;
    }

    public static TagClass getTagClass(byte octet) {
	byte classByte = (byte) ((octet >> 6) & 0x03);
	switch (classByte) {
	case 0: return UNIVERSAL;
	case 1: return APPLICATION;
	case 2: return CONTEXT;
	case 3: return PRIVATE;
	default: return null; // what possible values are there in 2 bits?!?
	}
    }

}
