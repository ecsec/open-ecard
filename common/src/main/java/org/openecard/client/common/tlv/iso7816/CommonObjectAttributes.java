package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;
import org.openecard.client.common.util.Helper;
import java.nio.charset.Charset;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CommonObjectAttributes extends TLVType {

    private String label;
    private TLVBitString flags;
    private byte[] authId;
    private Integer userConsent; // 1..15
    private List<TLV> acls;


    public CommonObjectAttributes(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 12))) {
	    label = new String(p.next(0).getValue(), Charset.forName("UTF-8"));
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
	    flags = new TLVBitString(p.next(0), new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());
	}
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    authId = p.next(0).getValue();
	}
	if (p.match(Tag.IntegerTag)) {
	    userConsent = Helper.convertByteArrayToInt(p.next(0).getValue());
	}
	if (p.match(Tag.SequenceTag)) {
	    TLVList list = new TLVList(p.next(0));
	    acls = list.getContent();
	}
    }


}
