/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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

package org.openecard.crypto.tls.verify;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.tls.TlsServerCertificate;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.util.IPAddress;
import org.openecard.common.util.DomainUtils;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Certificate verifier which only checks the hostname against the received certificate.
 *
 * @author Tobias Wich
 */
public class HostnameVerifier implements CertificateVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(HostnameVerifier.class);

    @Override
    public void isValid(TlsServerCertificate chain, String hostOrIp) throws CertificateVerificationException {
	try {
	    TlsCertificate tlsCert = chain.getCertificate().getCertificateAt(0);
	    Certificate cert = Certificate.getInstance(tlsCert.getEncoded());
	    validInt(cert, hostOrIp);
	} catch (IOException ex) {
	    throw new CertificateVerificationException("Invalid certificate received from server.", ex);
	}
    }

    private void validInt(Certificate cert, String hostOrIp) throws CertificateVerificationException {
	boolean success = false;
	boolean isIPAddr = IPAddress.isValid(hostOrIp);

	// check hostname against Subject CN
	if (! isIPAddr) {
	    RDN[] cn = cert.getSubject().getRDNs(BCStrictStyle.CN);
	    if (cn.length != 0) {
		// CN is always a string type
		String hostNameReference = cn[0].getFirst().getValue().toString();
		success = checkWildcardName(hostOrIp, hostNameReference);
	    } else {
		LOG.debug("No CN entry in certificate's Subject.");
	    }
	} else {
	    LOG.debug("Given name is an IP Address. Validation relies solely on the SubjectAlternativeName.");
	}
	// stop execution when we found a valid name
	if (success) {
	    return;
	}

	// evaluate subject alternative name
	Extensions ext = cert.getTBSCertificate().getExtensions();
	Extension subjAltExt = ext.getExtension(Extension.subjectAlternativeName);
	if (subjAltExt != null) {
	    // extract SubjAltName from Extensions
	    GeneralNames gns = GeneralNames.fromExtensions(ext, Extension.subjectAlternativeName);
	    GeneralName[] names = gns.getNames();
	    for (GeneralName name : names) {
		ASN1Encodable reference = name.getName();
		switch (name.getTagNo()) {
		    case GeneralName.dNSName:
			if (! isIPAddr) {
			    success = checkWildcardName(hostOrIp, reference.toString());
			}
			break;
		    case GeneralName.iPAddress:
			if (isIPAddr) {
			    // TODO: validate IP Addresses
			    LOG.warn("IP Address verification not supported.");
			}
			break;
		    default:
			LOG.debug("Unsupported GeneralName ({}) tag in SubjectAlternativeName.", name.getTagNo());
		}
		// stop execution when we found a valid name
		if (success) {
		    return;
		}
	    }
	}

	// evaluate result
	if (! success) {
	    String errorMsg = "Hostname in certificate differs from actually requested host.";
	    throw new CertificateVerificationException(errorMsg);
	}
    }

    private static boolean checkWildcardName(String givenHost, String wildcardHost)
	    throws CertificateVerificationException {
	LOG.debug("Comparing connection hostname against certificate hostname: [{}] [{}]", givenHost, wildcardHost);
	try {
	    return DomainUtils.checkHostName(wildcardHost, givenHost, true);
	} catch (IllegalArgumentException ex) {
	    String msg = "Invalid domain name found in certificate or requested hostname.";
	    throw new CertificateVerificationException(msg, ex);
	}
    }

}
