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
public class X509CertificateAttribute extends TLVType {

    private GenericObjectValue<Certificate> value;
    private TLV subject;
    private TLV issuer;
    private Integer serialNumber;


    public X509CertificateAttribute(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	// first value is validated by GenericObjectValue
	value = new GenericObjectValue<Certificate>(p.next(0), Certificate.class);

	if (p.match(Tag.SequenceTag)) {
	    subject = p.next(0);
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    issuer = p.next(0);
	}
	if (p.match(Tag.IntegerTag)) {
	    serialNumber = new Integer(Helper.convertByteArrayToInt(p.next(0).getValue()));
	}
    }


}
