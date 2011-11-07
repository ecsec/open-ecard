package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;


/**
 * This class can't be used outside of the package as its definitifely not safe to use for arbitrary types.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
final class GenericPathOrObjects<KeyType extends TLVType> extends TLV {

    private Path path;
    private List<KeyType> objects;
    private TLV ext;


    public GenericPathOrObjects(TLV tlv, Class<KeyType> clazz) throws TLVException {
        super(tlv);

	Constructor<KeyType> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("KeyType supplied doesn't have a constructor KeyType(TLV).");
	}

	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.SequenceTag)) {
	    path = new Path(p.next(0));
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    Parser p1 = new Parser(p.next(0).getChild());
	    if (p1.match(Tag.SequenceTag)) {
		TLVList objectsList = new TLVList(p1.next(0));
		objects = new LinkedList<KeyType>();
		for (TLV nextT : objectsList.getContent()) {
		    try {
			objects.add(c.newInstance(nextT));
		    } catch (InvocationTargetException ex) {
			throw new TLVException(ex);
		    } catch (Exception ex) {
			throw new TLVException("KeyType supplied doesn't have a constructor KeyType(TLV).");
		    }
		}
	    }
	} else if ((ext = p.next(0)) != null) {
	    // fine already assigned
	} else {
	    throw new TLVException("No content in PathOrObject type.");
	}
    }


    public boolean hasPath() {
	return path != null;
    }
    public Path path() {
	return path;
    }

    public boolean hasObjects() {
	return objects != null;
    }
    public List<KeyType> objects() {
	return objects;
    }

}
