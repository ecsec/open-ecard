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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class X509CertificateAttribute extends TLVType {

    private GenericObjectValue<Certificate> value;
    private TLV subject;
    private TLV issuer;
    private Integer serialNumber;


    public X509CertificateAttribute(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	// first value is validated by GenericObjectValue
	value = new GenericObjectValue<Certificate>(p.next(0), Certificate.class);

	if (p.match(Tag.SequenceTag)) {
	    subject = p.next(0);
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    issuer = p.next(0);
	}
	if (p.match(Tag.IntegerTag)) {
	    serialNumber = ByteUtils.toInteger(p.next(0).getValue());
	}
    }

}
