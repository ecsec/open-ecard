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
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SecretKeyChoice extends TLVType {

    private GenericSecretKeyObject<TLV> algIndependentKey = null;
    private GenericSecretKeyObject<TLV> genericSecretKey = null;
    private TLV extension = null;

    public SecretKeyChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());
	if (p.match(Tag.SEQUENCE_TAG)) {
	    algIndependentKey = new GenericSecretKeyObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 15))) {
	    genericSecretKey = new GenericSecretKeyObject<TLV>(p.next(0), TLV.class);
	} else {
	    extension = p.next(0);
	}
    }

    public GenericSecretKeyObject<TLV> getAlgIndependentKey() {
	return algIndependentKey;
    }

    public GenericSecretKeyObject<TLV> getGenericSecretKey() {
	return genericSecretKey;
    }

    public TLV getExtension() {
	return extension;
    }

}
