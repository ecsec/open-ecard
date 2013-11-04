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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class CommonAuthenticationObjectAttributes extends TLVType {

    private byte[] authId = null;
    private Integer authReference = null;
    private Integer securityEnvironmentID = null;

    public CommonAuthenticationObjectAttributes(TLV tlv) {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 4))) {
	    authId = p.next(0).getValue();
	}

	if (p.match(new Tag(TagClass.UNIVERSAL, true, 2))) {
	    authReference = ByteUtils.toInteger(p.next(0).getValue());
	}

	if (p.match(new Tag(TagClass.CONTEXT, true, 0))) {
	    securityEnvironmentID = ByteUtils.toInteger(p.next(0).getValue());
	}
    }

    public byte[] getAuthId() {
	return authId;
    }

    public int getAuthReference() {
	if (authReference != null) {
	    return authReference;
	} else {
	    return -1;
	}
    }

    public int getSecurityEnvironmentID() {
	if (securityEnvironmentID != null) {
	    return securityEnvironmentID;
	} else {
	    return -1;
	}
    }

}
