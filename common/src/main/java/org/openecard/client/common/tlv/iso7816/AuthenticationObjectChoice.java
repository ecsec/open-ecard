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
public class AuthenticationObjectChoice extends TLVType {

    private GenericAuthenticationObject<PasswordAttributes> pwd; // PasswordAttributes
    private GenericAuthenticationObject<TLV> biometricTemplate; // BiometricAttributes
    private GenericAuthenticationObject<TLV> authKey; // AuthKeyAttributes
    private GenericAuthenticationObject<TLV> external; // ExternalAuthObjectAttributes
    private TLV ext;


    public AuthenticationObjectChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    pwd = new GenericAuthenticationObject<PasswordAttributes>(p.next(0), PasswordAttributes.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    biometricTemplate = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    authKey = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    external = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else {
	    ext = p.next(0);
	}

    }

}
