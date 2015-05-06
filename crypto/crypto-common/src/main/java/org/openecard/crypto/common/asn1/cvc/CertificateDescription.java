/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import java.nio.charset.Charset;
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
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class CertificateDescription {

    private static final Logger _logger = LoggerFactory.getLogger(CertificateDescription.class);

    private String descriptionType;
    private String issuerName;
    private String issuerURL;
    private String subjectName;
    private String subjectURL;
    private String termsOfUsage;
    private byte[] termsOfUsageBytes;
    private String redirectURL;
    private ArrayList<byte[]> commCertificates;
    private byte[] encoded;
    private String termsOfUsageMimeType;

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
			switch (descriptionType) {
			    case CVCertificatesObjectIdentifier.id_plainFormat:
				termsOfUsageMimeType = "text/plain";
				termsOfUsage = ((ASN1String) obj).getString();
				break;
			    case CVCertificatesObjectIdentifier.id_htmlFormat:
				termsOfUsageMimeType = "text/html";
				termsOfUsage = ((ASN1String) obj).getString();
				break;
			    case CVCertificatesObjectIdentifier.id_pdfFormat:
				termsOfUsageMimeType = "application/pdf";
				termsOfUsageBytes = ((ASN1OctetString) obj).getOctets();
				break;
			}
			break;
		    case 6:
			redirectURL = ((ASN1String) obj).getString();
			break;
		    case 7:
			Enumeration<?> commCerts = ((ASN1Set) obj).getObjects();
			commCertificates = new ArrayList<>();

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
    @Deprecated
    public Object getTermsOfUsage() {
	return termsOfUsage;
    }

    /**
     * Get the terms of usage as String.
     *
     * @return The terms of usage as string.
     * @throws IllegalStateException If the mimeType of the terms of usage is application/pdf.
     */
    public String getTermsOfUsageString() {
	if (termsOfUsage != null) {
	    return termsOfUsage;
	}

	throw new IllegalStateException("Terms of usage are not available in a string type.");
    }

    /**
     * Get the terms of usage as byte array.
     * <br/>
     * The intension of this method is to serve the bytes of the terms of usage in case they are in pdf format. If the
     * terms of usage are in {@code plain text} or {@code HTML} format (represented by a String) the getBytes method of
     * the String object is invoked with the UTF-8 charset.
     *
     * @return The terms of usage as byte array.
     */
    public byte[] getTermsOfUsageBytes() {
	if (termsOfUsageBytes != null) {
	    return termsOfUsageBytes;
	} else {
	    return termsOfUsage.getBytes(Charset.forName("UTF-8"));
	}
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

    /**
     * Get the MimeType of the Terms of Usage in the Certificate Description.
     *
     * @return The MimeType of the terms of usage. The possible values are:
     * <br/>
     * <ul>
     *	<li>text/plain</li>
     *	<li>text/html</li>
     *	<li>application/pdf</li>
     * </ul>
     */
    public String getTermsOfUsageMimeType() {
	return termsOfUsageMimeType;
    }

    /**
     * Indicates whether the Terms of Usage are in PDF format.
     *
     * @return {@code TRUE} if the Terms of Usage are in PDF format else {@code FALSE}.
     */
    public boolean isTermsOfUsagePdf() {
	return termsOfUsageMimeType.equals("application/pdf");
    }

    /**
     * Indicates whether the Terms of Usage are in HTML format.
     *
     * @return {@code TRUE} if the Terms of Usage are in HTML format else {@code FALSE}.
     */
    public boolean isTermsOfUsageHtml() {
	return termsOfUsageMimeType.equals("text/html");
    }

    /**
     * Indicates whether the Terms of Usage are in plain text format.
     *
     * @return {@code TRUE} if the Terms of Usage are in plain text format else {@code FALSE}.
     */
    public boolean isTermsOfUsageText() {
	return termsOfUsageMimeType.equals("text/plain");
    }

}
