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
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 * @param <CertAttributes>
 */
public class GenericCertificateObject <CertAttributes> {

    private TLV tlv;

    // from CIO
    private CommonObjectAttributes commonObjectAttributes;
    private TLV classAttributes;           // CommonCertificateAttributes
    private TLV subClassAttributes;        // NULL
    private CertAttributes typeAttributes; // CertAttributes


    public GenericCertificateObject(TLV tlv, Class<CertAttributes> clazz) throws TLVException {
	Constructor<CertAttributes> c;
	try {
	    c = clazz.getConstructor(TLV.class);
	} catch (Exception ex) {
	    throw new TLVException("CertAttributes supplied doesn't have a constructor CertAttributes(TLV).");
	}

	this.tlv = tlv;

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
		throw new TLVException("CertAttributes supplied doesn't have a constructor CertAttributes(TLV).");
	    }
	}
    }

    /**
     * Gets the CommonObjectAttributes of the certificate object.
     *
     * @return The {@link CommonObjectAttributes} of the certificate.
     */
    public CommonObjectAttributes getCommonObjectAttributes() {
	return commonObjectAttributes;
    }

    /**
     * Gets the class attributes of the certificate here the CommonCertificateAttributes.
     *
     * @return The CommonCertificateAttributes of the certificate.
     */
    public TLV getClassAttributes() {
	return classAttributes;
    }

    /**
     * Gets the sub class attributes of the certificate. This value is always null.
     *
     * @return null.
     */
    public TLV getSubClassAttributes() {
	return subClassAttributes;
    }

    /**
     * Gets the attributes which are specific for the type of the certificate.
     * (The generic part of this data type.)
     *
     * @return The type attributes of the certificate.
     */
    public CertAttributes getTypeAttributes() {
	return typeAttributes;
    }

}
