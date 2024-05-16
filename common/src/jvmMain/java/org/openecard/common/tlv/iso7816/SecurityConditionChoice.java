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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Hans-Martin Haase
 */
public class SecurityConditionChoice extends TLVType {

    boolean always;
    private byte[] authIdentifier;
    private AuthReference authReference;
    private SecurityConditionChoice not;
    private List<TLV> and;
    private List<TLV> or;

    public SecurityConditionChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 5))) {
	    always = true;
	    p.next(0);
	}

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    authIdentifier = p.next(0).getValue();
	}

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    try {
		authReference = new AuthReference(p.next(0));
	    } catch (TLVException ex) {
		Logger.getLogger(SecurityConditionChoice.class.getName()).log(Level.SEVERE, null, ex);
		throw new TLVException("Malformed authReference");
	    }
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    not = new SecurityConditionChoice(p.next(0));
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    and = p.next(0).asList();
	}

	if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    or = p.next(0).asList();
	}
    }

    public boolean isAlways() {
	return always;
    }

    public byte[] getAuthIdentifier() {
	return authIdentifier;
    }

    public AuthReference getAuthReference() {
	return authReference;
    }

    public SecurityConditionChoice getNot() {
	return not;
    }

    public List<TLV> getAnd() {
	return and;
    }

    public List<TLV> getOr() {
	return or;
    }

}
