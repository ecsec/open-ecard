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


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CertificateChoice extends TLVType {

    private GenericCertificateObject<X509CertificateAttribute> x509Certificate; // X509CertificateAttributes
    private GenericCertificateObject<TLV> x509AttributeCertificate; // X509AttributeCertificateAttributes
    private GenericCertificateObject<TLV> spkiCertificate; // SPKICertificateAttributes
    private GenericCertificateObject<TLV> pgpCertificate; // PGPCertificateAttributes
    private GenericCertificateObject<TLV> wtlsCertificate; // WTLSCertificateAttributes
    private GenericCertificateObject<TLV> x9_68Certificate; // X9-68CertificateAttributes
    private GenericCertificateObject<TLV> cvCertificate; // CVCertificateAttributes
    private GenericCertificateObject<TLV> genericCertificateObject; // GenericCertificateAttributes
    private TLV ext;


    public CertificateChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    x509Certificate = new GenericCertificateObject<X509CertificateAttribute>(p.next(0), X509CertificateAttribute.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    x509AttributeCertificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    spkiCertificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    pgpCertificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 3))) {
	    wtlsCertificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 4))) {
	    x9_68Certificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 5))) {
	    cvCertificate = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 6))) {
	    genericCertificateObject = new GenericCertificateObject<TLV>(p.next(0), TLV.class);
	} else {
	    ext = p.next(0);
	}
    }

}
