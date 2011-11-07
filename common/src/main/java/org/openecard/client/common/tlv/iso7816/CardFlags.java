package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardFlags extends TLVBitString {

    private boolean readOnly;
    private boolean authRequired;
    private boolean prnGeneration;

    public CardFlags(TLV tlv) throws TLVException {
	super(tlv, new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());

	readOnly = isSet(0);
	authRequired = isSet(1);
	prnGeneration = isSet(2);
    }

    public boolean readOnly() {
	return readOnly;
    }

    public boolean authRequired() {
	return authRequired;
    }

    public boolean prnGeneration() {
	return prnGeneration;
    }

}
