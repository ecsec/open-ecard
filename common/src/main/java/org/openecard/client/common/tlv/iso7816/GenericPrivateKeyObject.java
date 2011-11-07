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
class GenericPrivateKeyObject <KeyAttributes> {

    private TLV tlv;

    // from CIO
    private CommonObjectAttributes commonObjectAttributes;
    private CommonKeyAttributes classAttributes;          // CommonKeyAttributes
    private TLV subClassAttributes;       // CommonPrivateKeyAttributes
    private KeyAttributes typeAttributes; // KeyAttributes


    public GenericPrivateKeyObject(TLV tlv, Class<KeyAttributes> clazz) throws TLVException {
	Constructor<KeyAttributes> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("KeyAttributes supplied doesn't have a constructor KeyAttributes(TLV).");
	}

	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.SequenceTag)) {
	    commonObjectAttributes = new CommonObjectAttributes(p.next(0));
	} else {
	    throw new TLVException("CommonObjectAttributes not present.");
	}
	if (p.match(Tag.SequenceTag)) {
	    classAttributes = new CommonKeyAttributes(p.next(0));
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
		throw new TLVException("KeyAttributes supplied doesn't have a constructor KeyAttributes(TLV).");
	    }
	}
    }

}
