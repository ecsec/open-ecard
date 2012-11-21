/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.tlv.iso7816;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


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
