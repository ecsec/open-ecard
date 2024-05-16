/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
 * The class implements a data type which corresponds to the ASN.1 type AuthenticationObject in ISO7816-15.
 * 
 * @author Tobias Wich
 * @author Hans-Martin Haase
 * @param <AuthAttributes>
 */
public class GenericAuthenticationObject <AuthAttributes> {

    /**
     * The TLV which represents this object
     */
    private TLV tlv;

    // from CIO
    private CommonObjectAttributes commonObjectAttributes;
    private TLV classAttributes;           // CommonAuthObjectAttributes
    private TLV subClassAttributes;        // NULL
    private AuthAttributes typeAttributes; // AuthObjectAttributes

    /**
     * The constructor parses the input TLV and instantiates the generic part of the class.
     *
     * @param tlv The {@link TLV} which will be used to create the object.
     * @param clazz Class type of the generic attribute.
     * @throws TLVException
     */
    public GenericAuthenticationObject(TLV tlv, Class<AuthAttributes> clazz) throws TLVException {
	Constructor<AuthAttributes> c;
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
	    throw new TLVException("CommonObjectAttributes not present.");
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

    /**
     * Gets the {@link CommonObjectAttributes} of the object.
     *
     * @return The CommonObjectAttribute of the object.
     */
    public CommonObjectAttributes getCommonObjectAttributes() {
	return commonObjectAttributes;
    }

    /**
     * Gets the generic object of this datatype.
     * The returned data type depends on the specification in the constructor.
     *
     * @return The authentication attributes of the object.
     */
    public AuthAttributes getAuthAttributes() {
	return typeAttributes;
    }

    /**
     * Gets the class attributes of this object.
     *
     * @return The class attributes of the object as TLV.
     */
    public TLV getClassAttributes() {
	return classAttributes;
    }

    /**
     * Gets the sub class attributes of the object.
     *
     * @return The sub class attributes as TLV.
     */
    public TLV getSubClassAttributes() {
	return subClassAttributes;
    }

    /**
     * Gets the object itself as TLV.
     *
     * @return The object as TLV.
     */
    public TLV getGenericAuthenticationObjectTLV() {
	return tlv;
    }

}
