/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import java.util.Locale;

import org.openecard.bouncycastle.asn1.ASN1Encodable;
import org.openecard.bouncycastle.asn1.x500.RDN;
import org.openecard.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.openecard.bouncycastle.asn1.x509.Extension;
import org.openecard.bouncycastle.asn1.x509.Extensions;
import org.openecard.bouncycastle.asn1.x509.GeneralName;
import org.openecard.bouncycastle.asn1.x509.GeneralNames;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.util.IPAddress;
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

	private static final Logger logger = LoggerFactory.getLogger(HostnameVerifier.class);

	@Override
	public void isValid(Certificate chain, String hostOrIp) throws CertificateVerificationException {
		org.openecard.bouncycastle.asn1.x509.Certificate cert = chain.getCertificateAt(0);
		boolean success = false;
		boolean isIPAddr = IPAddress.isValid(hostOrIp);

		// check hostname against Subject CN
		if (! isIPAddr) {
			RDN[] cn = cert.getSubject().getRDNs(BCStrictStyle.CN);
			if (cn.length == 0) {
				throw new CertificateVerificationException("No CN entry in certificate's Subject.");
			}
			// CN is always a string type
			String hostNameReference = cn[0].getFirst().getValue().toString();
			success = checkWildcardName(hostOrIp, hostNameReference);
		} else {
			logger.debug("Given name is an IP Address. Validation relies solely on the SubjectAlternativeName.");
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
						logger.warn("IP Address verification not supported.");
					}
					break;
				default:
					logger.debug("Unsupported GeneralName ({}) tag in SubjectAlternativeName.", name.getTagNo());
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
		logger.debug("Comparing connection hostname against certificate hostname: [{}] [{}]", givenHost, wildcardHost);
		String[] givenToken = givenHost.toLowerCase(Locale.ENGLISH).split("\\.");
		String[] wildToken = wildcardHost.toLowerCase(Locale.ENGLISH).split("\\.");
		// error if number of token is different
		if (givenToken.length != wildToken.length) {
			return false;
		}
		// compare entries
		for (int i = 0; i < givenToken.length; i++) {
			if (wildToken[i].equals("*")) {
				// skip wildcard part
				continue;
			}
			if (! givenToken[i].equals(wildToken[i])) {
				return false;
			}
		}
		// each part processed and no error -> success
		return true;
	}

}
