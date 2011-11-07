package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLVList {

    protected final TLV tlv;

    protected TLVList(TLV tlv, long expectedTagNum) throws TLVException {
	if (tlv.getTagNumWithClass() != expectedTagNum) {
	    throw new TLVException("Not of type TLVList.");
	}
	this.tlv = tlv;
    }

    public TLVList(List<TLV> children) throws TLVException {
	tlv = new TLV();
	tlv.setTagNumWithClass(Tag.SequenceTag.getTagNumWithClass());
	// link in children
	if (! children.isEmpty()) {
	    TLV first = children.get(0);
	    tlv.setChild(first);
	    for (int i=1; i < children.size(); i++) {
		first.addToEnd(children.get(i));
	    }
	}
    }

    public TLVList(TLV tlv) throws TLVException {
	this(tlv, 0x61);
    }

    public TLVList(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public List<TLV> getContent() {
	if (tlv.hasChild()) {
	    return tlv.getChild().asList();
	}
	return new LinkedList<TLV>();
    }

}
