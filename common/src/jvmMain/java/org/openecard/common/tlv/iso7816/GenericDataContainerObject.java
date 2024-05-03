/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;

/**
 *
 * @author Hans-Martin Haase
 */
public class GenericDataContainerObject <ObjectType>{

    /**
     * The TLV which represents this object
     */
    private TLV tlv;

    // from CIO
    private CommonObjectAttributes commonObjectAttributes;
    private TLV classAttributes;          // CommonDataContainerObjectAttributes
    private TLV subClassAttributes;       // NULL
    private ObjectType typeAttributes; // DataObjectAttributes

    public GenericDataContainerObject(TLV tlv, Class<ObjectType> clazz) throws TLVException {
	Constructor<ObjectType> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).");
	}

	this.tlv = tlv;

	// parse the tlv
	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.SEQUENCE_TAG)) {
	    commonObjectAttributes = new CommonObjectAttributes(p.next(0));
	} else {
	    throw new TLVException("CommonDataContainerObjectAttributes not present.");
	}
	if (p.match(Tag.SEQUENCE_TAG)) {
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

    public CommonObjectAttributes getCommonObjectAttributes() {
	return commonObjectAttributes;
    }

    public TLV getClassAttributes() {
	return classAttributes;
    }

    public TLV getSubClassAttributes() {
	return subClassAttributes;
    }

    public ObjectType getTypeAttributes() {
	return typeAttributes;
    }

}
