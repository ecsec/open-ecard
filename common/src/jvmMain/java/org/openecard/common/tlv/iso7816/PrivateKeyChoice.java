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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class PrivateKeyChoice extends TLVType {

    private GenericPrivateKeyObject<PrivateRSAKeyAttributes> privateRSAKey = null;
    private GenericPrivateKeyObject<PrivateECKeyAttributes> privateECKey = null;
    private GenericPrivateKeyObject<TLV> privateDHKey = null;
    private GenericPrivateKeyObject<TLV> privateDSAKey = null;
    private GenericPrivateKeyObject<TLV> privateKEAKey = null;
    private GenericPrivateKeyObject<TLV> genericPrivateKey = null;
    private TLV ext = null;

    public PrivateKeyChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    privateRSAKey = new GenericPrivateKeyObject<PrivateRSAKeyAttributes>(p.next(0), PrivateRSAKeyAttributes.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    privateECKey = new GenericPrivateKeyObject<PrivateECKeyAttributes>(p.next(0), PrivateECKeyAttributes.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    privateDHKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    privateDSAKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    privateKEAKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    genericPrivateKey = new GenericPrivateKeyObject<TLV>(p.next(0), TLV.class);
	} else {
	    ext = p.next(0);
	}
    }

    public GenericPrivateKeyObject<PrivateRSAKeyAttributes> getPrivateRSAKey() {
	return privateRSAKey;
    }

    public GenericPrivateKeyObject<PrivateECKeyAttributes> getPrivateECKey() {
	return privateECKey;
    }

    public GenericPrivateKeyObject<TLV> getPrivateDHKey() {
	return privateDHKey;
    }

    public GenericPrivateKeyObject<TLV> getPrivateDSAKey() {
	return privateDSAKey;
    }

    public GenericPrivateKeyObject<TLV> getPrivateKEAKey() {
	return privateKEAKey;
    }

    public GenericPrivateKeyObject<TLV> getGenericPrivateKey() {
	return genericPrivateKey;
    }

    public TLV getExt() {
	return ext;
    }

}
