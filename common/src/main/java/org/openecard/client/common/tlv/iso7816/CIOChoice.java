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
import org.openecard.client.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CIOChoice extends TLV {

    private TLV actualElement;
    private String elementName;

    private GenericPathOrObjects<PrivateKeyChoice> privateKeys           = null;
    private TLV publicKeys                                               = null;
    private TLV trustedPublicKeys                                        = null;
    private TLV secretKeys                                               = null;
    private GenericPathOrObjects<CertificateChoice> certificates         = null;
    private TLV trustedCertificates                                      = null;
    private TLV usefulCertificates                                       = null;
    private TLV dataContainerObjects                                     = null;
    private GenericPathOrObjects<AuthenticationObjectChoice> authObjects = null;
    private TLV futureExtension                                          = null;


    public CIOChoice(TLV tlv) throws TLVException {
        super(tlv);

	Parser p = new Parser(tlv);
	// process choice types
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    privateKeys = new GenericPathOrObjects(p.next(0), PrivateKeyChoice.class);
            actualElement = privateKeys;
            elementName = "privateKeys";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    publicKeys = p.next(0);
            actualElement = publicKeys;
            elementName = "publicKeys";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    trustedPublicKeys = p.next(0);
            actualElement = trustedPublicKeys;
            elementName = "trustedPublicKeys";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    secretKeys = p.next(0);
            actualElement = secretKeys;
            elementName = "secretKeys";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    certificates = new GenericPathOrObjects(p.next(0), CertificateChoice.class);
            actualElement = certificates;
            elementName = "certificates";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 5))) {
	    trustedCertificates = p.next(0);
            actualElement = trustedCertificates;
            elementName = "trustedCertificates";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 6))) {
	    usefulCertificates = p.next(0);
            actualElement = usefulCertificates;
            elementName = "usefulCertificates";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 7))) {
	    dataContainerObjects = p.next(0);
            actualElement = dataContainerObjects;
            elementName = "dataContainerObjects";
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 8))) {
	    authObjects = new GenericPathOrObjects(p.next(0), AuthenticationObjectChoice.class);
            actualElement = authObjects;
            elementName = "authObjects";
	} else { // extension
	    futureExtension = p.next(0);
            actualElement = futureExtension;
            elementName = "futureExtension";
	    if (futureExtension == null) {
		throw new TLVException("No element in CIOChoice");
	    }
	}
    }


    public String getElementName() {
        return elementName;
    }
    public TLV getElement() {
        return actualElement;
    }

}
