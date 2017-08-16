/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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

package org.openecard.crypto.tls.auth;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.AbstractTlsSignerCredentials;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.util.io.pem.PemObject;
import org.openecard.bouncycastle.util.io.pem.PemWriter;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Signing credential delegating all calls to a wrapped GenericCryptoSigner.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class SmartCardSignerCredential extends AbstractTlsSignerCredentials {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardSignerCredential.class);

    private final DidInfo did;
    private final List<X509Certificate> chain;
    private final SignatureAndHashAlgorithm sigAlg;

    private Certificate certificate;

    public SmartCardSignerCredential(@Nonnull DidInfo info, List<X509Certificate> chain,
	    SignatureAndHashAlgorithm sigAlg) {
	this.did = info;
	this.chain = chain;
	this.sigAlg = sigAlg;
    }

    @Override
    public byte[] generateCertificateSignature(@Nonnull byte[] hash) throws IOException {
	LOG.debug("Signing hash={}.", ByteUtils.toHexString(hash));
	try {
	    did.authenticateMissing();
	    byte[] sigData = did.sign(hash);
	    return sigData;
	} catch (WSHelper.WSException ex) {
	    String msg = "Failed to create signature because of an unknown error.";
	    LOG.warn(msg, ex);
	    throw new IOException(msg, ex);
	} catch (SecurityConditionUnsatisfiable ex) {
	    String msg = "Access to the signature DID could not be obtained.";
	    LOG.warn(msg, ex);
	    throw new IOException(msg, ex);
	} catch (NoSuchDid ex) {
	    String msg = "Signing DID not available anymore.";
	    LOG.warn(msg, ex);
	    throw new IOException(msg, ex);
	}
    }

    @Override
    public synchronized Certificate getCertificate() {
	if (certificate == null) {
	    certificate = Certificate.EMPTY_CHAIN;

	    try {
		org.openecard.bouncycastle.asn1.x509.Certificate[] bcCerts;
		bcCerts = new org.openecard.bouncycastle.asn1.x509.Certificate[chain.size()];
		int i = 0;
		for (X509Certificate next : chain) {
		    byte[] encCert = next.getEncoded();
		    org.openecard.bouncycastle.asn1.x509.Certificate bcCert;
		    bcCert = org.openecard.bouncycastle.asn1.x509.Certificate.getInstance(encCert);
		    bcCerts[i] = bcCert;
		    i++;
		}

		certificate = new Certificate(bcCerts);
	    } catch (CertificateEncodingException ex) {
		LOG.error("Failed to deserialize certificate.", ex);
	    }

	    if (LOG.isDebugEnabled()) {
		StringWriter sw = new StringWriter();
		sw.write("Using the following certificate for authentication:\n");
		for (org.openecard.bouncycastle.asn1.x509.Certificate c : certificate.getCertificateList()) {
		    try (PemWriter pw = new PemWriter(sw)) {
			sw.append("\nSubject: ")
				.append(c.getSubject().toString())
				.append("\n");
			sw.append("Issuer:  ")
				.append(c.getIssuer().toString());
			pw.writeObject(new PemObject("CERTIFICATE", c.getEncoded()));
			sw.write("\n");
		    } catch (IOException ex) {
			LOG.error("Failed to encode certificate in PEM format.");
		    }
		}
		LOG.debug(sw.toString());
	    }
	}

	return certificate;
    }

    @Override
    public SignatureAndHashAlgorithm getSignatureAndHashAlgorithm() {
	return sigAlg;
    }

}
