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
public class PrivateECKeyAttribute extends TLVType {

    private Path value;
    // KeyInfo sequence
    // ECDomainParameters - Choice
    private TLV implicitCA; // NULL
    private TLV named; // ObjectIdentifier
    private TLV specified; // SpecifiedECDomain
    // PublicKeyOperations alias for Operations
    private TLVBitString operations;

    public PrivateECKeyAttribute(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.SequenceTag)) {
	    value = new Path(p.next(0));
	} else {
	    throw new TLVException("No value element in structure.");
	}
	// only match sequence not Reference (historical)
	if (p.match(Tag.SequenceTag)) {
	    Parser p1 = new Parser(p.next(0).getChild());
	    if (p1.match(new Tag(TagClass.UNIVERSAL, true, 5))) {
		implicitCA = p1.next(0);
	    } else if(p1.match(new Tag(TagClass.UNIVERSAL, true, 6))) {
		named = p1.next(0);
	    } else if (p1.match(Tag.SequenceTag)) {
		specified = p1.next(0);
	    } else {
		throw new TLVException("No parameters element in structure.");
	    }
	    if (p1.match(new Tag(TagClass.UNIVERSAL, true, 3))) {
		operations = new TLVBitString(p1.next(0), new Tag(TagClass.UNIVERSAL, true, 3).getTagNumWithClass());
	    }
	}
    }


}
