package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class GenericObjectValue<Type> extends TLVType {

    private ReferencedValue indirect;
    private Type direct;

    public GenericObjectValue(TLV tlv, Class<Type> clazz) throws TLVException {
	super(tlv);

	Constructor<Type> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("Type supplied doesn't have a constructor Type(TLV).");
	}

	Parser p = new Parser(tlv);

	if (p.match(Tag.SequenceTag) ||
	    p.match(new Tag(TagClass.UNIVERSAL, true, 19)) ||
	    p.match(new Tag(TagClass.UNIVERSAL, true, 22)) ||
	    p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    indirect = new ReferencedValue(p.next(0));
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    try {
		direct = c.newInstance(p.next(0).getChild());
	    } catch (InvocationTargetException ex) {
		throw new TLVException(ex);
	    } catch (Exception ex) {
		throw new TLVException("Type supplied doesn't have a constructor Type(TLV).");
	    }
	} else {
	    throw new TLVException("Unexpected element in ObjectValue.");
	}
    }


}
