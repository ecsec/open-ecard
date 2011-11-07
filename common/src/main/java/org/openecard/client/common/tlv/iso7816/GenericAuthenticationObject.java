package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * This class can't be used outside of the package as its definitifely not safe to use for arbitrary types.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class GenericAuthenticationObject <AuthAttributes> {

    private TLV tlv;

    // from CIO
    private CommonObjectAttributes commonObjectAttributes;
    private TLV classAttributes;           // CommonAuthObjectAttributes
    private TLV subClassAttributes;        // NULL
    private AuthAttributes typeAttributes; // AuthObjectAttributes


    public GenericAuthenticationObject(TLV tlv, Class<AuthAttributes> clazz) throws TLVException {
	Constructor<AuthAttributes> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).");
	}

	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.SequenceTag)) {
	    commonObjectAttributes = new CommonObjectAttributes(p.next(0));
	} else {
	    throw new TLVException("CommonObjectAttributes not present.");
	}
	if (p.match(Tag.SequenceTag)) {
	    classAttributes = p.next(0);
	} else {
	    throw new TLVException("CommonObjectAttributes not present.");
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    subClassAttributes = p.next(0).getChild();
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    try {
		typeAttributes = c.newInstance(p.next(0).getChild());
	    } catch (InvocationTargetException ex) {
		throw new TLVException(ex);
	    } catch (Exception ex) {
		throw new TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).");
	    }
	}
    }

}
