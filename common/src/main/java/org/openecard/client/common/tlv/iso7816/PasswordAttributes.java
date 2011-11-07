package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;
import org.openecard.client.common.util.Helper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PasswordAttributes extends TLVType {

    private TLVBitString passwordFlags;
    private int passwordType; // enum PasswordType
    private int minLength;
    private int storedLength;
    private Integer maxLength;
    private Integer passwordReference;
    private Byte padChar;
    private TLV lastPasswordChange;
    private Path path;


    public PasswordAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.BitstringTag)) {
	    passwordFlags = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("passwordFlags element missing.");
	}
	if (p.match(Tag.EnumeratedTag)) {
	    passwordType = Helper.convertByteArrayToInt(p.next(0).getValue());
	} else {
	    throw new TLVException("passwordType element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    minLength = Helper.convertByteArrayToInt(p.next(0).getValue());
	} else {
	    throw new TLVException("minLength element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    storedLength = Helper.convertByteArrayToInt(p.next(0).getValue());
	} else {
	    throw new TLVException("storedLength element missing.");
	}
	if (p.match(Tag.IntegerTag)) {
	    maxLength = Helper.convertByteArrayToInt(p.next(0).getValue());
	}
	if (p.match(new Tag(TagClass.CONTEXT, true, 0))) {
	    passwordReference = Helper.convertByteArrayToInt(p.next(0).getValue());
	}
	if (p.match(Tag.OctetstringTag)) {
	    padChar = p.next(0).getValue()[0];
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 24))) {
	    lastPasswordChange = p.next(0);
	}
	if (p.match(Tag.SequenceTag)) {
	    path = new Path(p.next(0));
	}
    }

}
