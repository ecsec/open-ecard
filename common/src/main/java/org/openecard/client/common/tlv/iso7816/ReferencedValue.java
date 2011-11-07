package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ReferencedValue extends TLVType {

    private Path path;
    private TLV url;

    public ReferencedValue(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(Tag.SequenceTag)) {
	    path = new Path(p.next(0));
	} else if(p.match(new Tag(TagClass.UNIVERSAL, true, 19)) ||
		  p.match(new Tag(TagClass.UNIVERSAL, true, 22)) ||
		  p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    url = p.next(0); // TODO: create URL type
	} else {
	    throw new TLVException("Unexpected element in ObjectValue.");
	}
    }


}
