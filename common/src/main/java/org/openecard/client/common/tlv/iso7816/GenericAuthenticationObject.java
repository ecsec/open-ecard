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

package org.openecard.client.common.tlv.iso7816;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;


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
