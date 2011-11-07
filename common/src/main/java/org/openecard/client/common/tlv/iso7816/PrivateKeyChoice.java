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
public class PrivateKeyChoice extends TLVType {

    private GenericPrivateKeyObject<PrivateRSAKeyAttribute> privateRSAKey;
    private GenericPrivateKeyObject<PrivateECKeyAttribute> privateECKey;
    private GenericPrivateKeyObject<TLV> privateDHKey;
    private GenericPrivateKeyObject<TLV> privateDSAKey;
    private GenericPrivateKeyObject<TLV> privateKEAKey;
    private GenericPrivateKeyObject<TLV> genericPrivateKey;
    private TLV ext;

    public PrivateKeyChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    privateRSAKey = new GenericPrivateKeyObject<PrivateRSAKeyAttribute>(p.next(0), PrivateRSAKeyAttribute.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    privateECKey = new GenericPrivateKeyObject<PrivateECKeyAttribute>(p.next(0), PrivateECKeyAttribute.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    privateDHKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    privateDSAKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    privateKEAKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    genericPrivateKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else {
	    ext = p.next(0);
	}
    }

}
