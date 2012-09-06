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

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Certificate extends TLVType {

    private TLV toBeSigned;          // CertificateContent
    private TLV algorithmIdentifier; // AlgorithmIdentifier
    private TLVBitString encrypted;


    public Certificate(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	if (p.match(Tag.SequenceTag)) {
	    toBeSigned = p.next(0);
	} else {
	    throw new TLVException("toBeSigned element missing.");
	}
	if (p.match(Tag.SequenceTag)) {
	    algorithmIdentifier = p.next(0);
	} else {
	    throw new TLVException("algorithmIdentifier element missing.");
	}
	if (p.match(Tag.BitstringTag)) {
	    encrypted = new TLVBitString(p.next(0));
	} else {
	    throw new TLVException("encrypted element missing.");
	}
    }

}
