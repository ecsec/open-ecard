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
public class Path {

    private final TLV tlv;

    private byte[] efIdOrPath;
    private Integer index;
    private Integer length;

    public Path(TLV tlv) throws TLVException {
	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    efIdOrPath = p.next(0).getValue();
	} else {
	    throw new TLVException("No efIdOrPath given.");
	}
	index = null;
	length = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2)) && p.matchLA(1, new Tag(TagClass.CONTEXT, true, 0))) {
	    index = new Integer(Helper.convertByteArrayToInt(p.next(0).getValue()));
	    length = new Integer(Helper.convertByteArrayToInt(p.next(0).getValue()));
	}
    }

    public Path(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public byte[] efIdOrPath() {
	return efIdOrPath;
    }

    public Integer getIndex() {
	// optional
	return index;
    }

    public Integer getLength() {
	// optional
	return length;
    }

}
