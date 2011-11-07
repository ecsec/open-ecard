package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Certificate extends TLVType {

    private TLV toBeSigned;          // CertificateContent
    private TLV algorithmIdentifier; // AlgorithmIdentifier
    private TLVBitString encrypted;


    public Certificate(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.SequenceTag)) {
	    toBeSigned = p.next(0);
	} else {
	    throw new TLVException("toBeSigned element missing.");
	}
	if (p.match(Tag.SequenceTag)) {
	    algorithmIdentifier = p.next(0);
	} else {
	    throw new TLVException("algorithmIdentifier element missing.");
	}
	if (p.match(Tag.BitstringTag)) {
	    encrypted = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("encrypted element missing.");
	}
    }

}
