/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.crypto.common.asn1.cvc;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1OctetString;
import org.openecard.bouncycastle.asn1.ASN1Primitive;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.ASN1Set;
import org.openecard.bouncycastle.asn1.ASN1String;
import org.openecard.bouncycastle.asn1.ASN1TaggedObject;
import org.openecard.bouncycastle.asn1.DERTaggedObject;
import org.openecard.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * <pre>
 * CertificateDescription ::= SEQUENCE {
 * descriptionType OBJECT IDENTIFIER,
 * issuerName [1] UTF8String,
 * issuerURL [2] PrintableString OPTIONAL,
 * subjectName [3] UTF8String,
 * subjectURL [4] PrintableString OPTIONAL,
 * termsOfUsage [5] ANY DEFINED BY descriptionType,
 * redirectURL [6] PrintableString OPTIONAL,
 * commCertificates [7] SET OF OCTET STRING OPTIONAL
 * }
 * </pre>
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CertificateDescription {

    private static final Logger _logger = LoggerFactory.getLogger(CertificateDescription.class);

    private String descriptionType;
    private String issuerName;
    private String issuerURL;
    private String subjectName;
    private String subjectURL;
    private Object termsOfUsage;
    private String redirectURL;
    private ArrayList<byte[]> commCertificates;
    private byte[] encoded;

    /**
     * Creates a new CertificateDescription.
     *
     * @param obj Encoded CertificateDescription
     * @return CertificateDescription
     * @throws CertificateException
     */
    public static CertificateDescription getInstance(Object obj) throws CertificateException {
	if (obj instanceof CertificateDescription) {
	    return (CertificateDescription) obj;
	} else if (obj instanceof ASN1Set) {
	    return new CertificateDescription((ASN1Sequence) obj);
	} else if (obj instanceof byte[]) {
	    try {
		return new CertificateDescription((ASN1Sequence) ASN1Sequence.fromByteArray((byte[]) obj));
	    } catch (IOException e) {
		_logger.error("Cannot parse CertificateDescription", e);
		throw new IllegalArgumentException("Cannot parse CertificateDescription");
	    }
	}
	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass());
    }

    /**
     * Creates a new CertificateDescription.
     *
     * @param seq Encoded CertificateDescription
     */
    private CertificateDescription(ASN1Sequence seq) throws CertificateException {
	try {
	    encoded = seq.getEncoded();
	    Enumeration<?> elements = seq.getObjects();
	    descriptionType = ASN1ObjectIdentifier.getInstance(elements.nextElement()).toString();

	    while (elements.hasMoreElements()) {
		ASN1TaggedObject taggedObject = DERTaggedObject.getInstance(elements.nextElement());
		int tag = taggedObject.getTagNo();
		ASN1Primitive obj = taggedObject.getObject();

		switch (tag) {
		    case 1:
			issuerName = ((ASN1String) obj).getString();
			break;
		    case 2:
			issuerURL = ((ASN1String) obj).getString();
			break;
		    case 3:
			subjectName = ((ASN1String) obj).getString();
			break;
		    case 4:
			subjectURL = ((ASN1String) obj).getString();
			break;
		    case 5:
			if (CVCertificatesObjectIdentifier.id_plainFormat.equals(descriptionType)) {
			    termsOfUsage = ((ASN1String) obj).getString();
			} else if (CVCertificatesObjectIdentifier.id_htmlFormat.equals(descriptionType)) {
			    termsOfUsage = ((ASN1String) obj).getString();
			} else if (CVCertificatesObjectIdentifier.id_pdfFormat.equals(descriptionType)) {
			    termsOfUsage = ((ASN1OctetString) obj).getOctets();
			}
			break;
		    case 6:
			redirectURL = ((ASN1String) obj).getString();
			break;
		    case 7:
			Enumeration<?> commCerts = ((ASN1Set) obj).getObjects();
			commCertificates = new ArrayList<byte[]>();

			while (commCerts.hasMoreElements()) {
			    commCertificates.add(((ASN1OctetString) commCerts.nextElement()).getOctets());
			}
			break;
		    default:
			throw new IllegalArgumentException("Unknown object in CertificateDescription.");
		}

	    }
	} catch (IOException e) {
	    _logger.error("Cannot parse CertificateDescription.", e);
	    throw new CertificateException("Cannot parse CertificateDescription.");
	}
    }

    /**
     * Returns DescriptionType.
     *
     * @return DescriptionType
     */
    public String getDescriptionType() {
	return descriptionType;
    }

    /**
     * Returns the IssuerName.
     *
     * @return IssuerName
     */
    public String getIssuerName() {
	return issuerName;
    }

    /**
     * Returns the IssuerURL.
     *
     * @return IssuerURL
     */
    public String getIssuerURL() {
	return issuerURL;
    }

    /**
     * Returns the SubjectName.
     *
     * @return SubjectName
     */
    public String getSubjectName() {
	return subjectName;
    }

    /**
     * Returns the SubjectURL.
     *
     * @return SubjectURL
     */
    public String getSubjectURL() {
	return subjectURL;
    }

    /**
     * Returns the TermsOfUsage.
     *
     * @return TermsOfUsage
     */
    public Object getTermsOfUsage() {
	return termsOfUsage;
    }

    /**
     * Returns the RedirectURL.
     *
     * @return RedirectURL
     */
    public String getRedirectURL() {
	return redirectURL;
    }

    /**
     * Returns the CommCertificates.
     *
     * @return CommCertificates
     */
    public ArrayList<byte[]> getCommCertificates() {
	return commCertificates;
    }

    /**
     * Returns the certificate description as a byte array.
     *
     * @return Certificate description as a byte array
     */
    public byte[] getEncoded() {
	return encoded;
    }

}
