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
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


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
