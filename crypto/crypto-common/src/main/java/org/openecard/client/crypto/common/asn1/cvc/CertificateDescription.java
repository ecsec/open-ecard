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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.crypto.common.asn1.cvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.ASN1Set;
import org.openecard.bouncycastle.asn1.ASN1TaggedObject;
import org.openecard.bouncycastle.asn1.DERIA5String;
import org.openecard.bouncycastle.asn1.DEROctetString;
import org.openecard.bouncycastle.asn1.DERPrintableString;
import org.openecard.bouncycastle.asn1.DERSet;
import org.openecard.bouncycastle.asn1.DERTaggedObject;
import org.openecard.bouncycastle.asn1.DERUTF8String;
import org.openecard.client.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier;


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
 */
public class CertificateDescription {

    private String descriptionType;
    private String issuerName;
    private String issuerURL;
    private String subjectName;
    private String subjectURL;
    private Object termsOfUsage;
    private String redirectURL;
    private ArrayList<byte[]> commCertificates;

    /**
     * Creates a new CertificateDescription.
     *
     * @param obj Encoded CertificateDescription
     * @return CertificateDescription
     */
    public static CertificateDescription getInstance(Object obj) {
	if (obj instanceof CertificateDescription) {
	    return (CertificateDescription) obj;
	} else if (obj instanceof ASN1Set) {
	    return new CertificateDescription((ASN1Sequence) obj);
	} else if (obj instanceof byte[]) {
	    try {
		return new CertificateDescription((ASN1Sequence) ASN1Sequence.fromByteArray((byte[]) obj));
	    } catch (IOException e) {
		Logger.getLogger(CertificateDescription.class.getName()).log(Level.SEVERE, "Cannot parse CertificateDescription", e.getMessage());
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
    public CertificateDescription(ASN1Sequence seq) {
	Enumeration elements = seq.getObjects();

	descriptionType = ASN1ObjectIdentifier.getInstance(elements.nextElement()).toString();

	while (elements.hasMoreElements()) {
	    ASN1TaggedObject taggedObject = DERTaggedObject.getInstance(elements.nextElement());
	    int tag = taggedObject.getTagNo();

	    try {
		switch (tag) {
		    case 1:
			issuerName = ((DERUTF8String) taggedObject.getObject()).getString();
			break;
		    case 2:
			issuerURL = ((DERPrintableString) taggedObject.getObject()).getString();
			break;
		    case 3:
			subjectName = ((DERUTF8String) taggedObject.getObject()).getString();
			break;
		    case 4:
			subjectURL = ((DERPrintableString) taggedObject.getObject()).getString();
			break;
		    case 5:
			if (descriptionType.equals(CVCertificatesObjectIdentifier.id_plainFormat)) {
			    termsOfUsage = ((DERUTF8String) taggedObject.getObject()).getString();
			} else if (descriptionType.equals(CVCertificatesObjectIdentifier.id_htmlFormat)) {
			    termsOfUsage = ((DERIA5String) taggedObject.getObject()).getString();
			} else if (descriptionType.equals(CVCertificatesObjectIdentifier.id_pdfFormat)) {
			    termsOfUsage = ((DEROctetString) taggedObject.getObject()).getEncoded();
			}
			break;
		    case 6:
			redirectURL = ((DERPrintableString) taggedObject.getObject()).getString();
			break;
		    case 7:
			Enumeration commCerts = ((DERSet) taggedObject.getObject()).getObjects();
			commCertificates = new ArrayList<byte[]>();

			while (commCerts.hasMoreElements()) {
			    commCertificates.add(((DEROctetString) commCerts.nextElement()).getEncoded());
			}
			break;
		    default:
			throw new IllegalArgumentException("Unknown object in CertificateDescription");
		}
	    } catch (IOException e) {
		Logger.getLogger(CertificateDescription.class.getName()).log(Level.SEVERE, "Cannot parse CertificateDescription", e.getMessage());
		throw new IllegalArgumentException("Cannot parse CertificateDescription");
	    }
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

}
