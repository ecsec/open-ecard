/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import java.security.SignatureException;
import java.security.cert.CertificateException;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.asn1.ASN1Encoding;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.asn1.x509.DigestInfo;
import org.openecard.bouncycastle.crypto.tls.AbstractTlsSignerCredentials;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsContext;
import org.openecard.bouncycastle.crypto.tls.TlsUtils;
import org.openecard.crypto.common.sal.CredentialPermissionDenied;
import org.openecard.crypto.common.sal.GenericCryptoSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Signing credential delegating all calls to a wrapped GenericCryptoSigner.
 *
 * @see GenericCryptoSigner
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class SmartCardSignerCredential extends AbstractTlsSignerCredentials implements ContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SmartCardSignerCredential.class);

    private final GenericCryptoSigner signerImpl;
    private TlsContext context;
    private Certificate certificate = Certificate.EMPTY_CHAIN;

    public SmartCardSignerCredential(@Nonnull GenericCryptoSigner signerImpl) {
	this.signerImpl = signerImpl;
    }

    @Override
    public void setContext(TlsContext context) {
	this.context = context;
    }

    @Override
    public byte[] generateCertificateSignature(@Nonnull byte[] hash) throws IOException {
	// Note: this check is necessary to avoid the pin dialog when the certificate is
	//       Certificate.EMPTY_CHAIN
	if (! certificate.equals(Certificate.EMPTY_CHAIN)) {
	    // When using TLS 1.2, a real PKCs#1 1.5 signature must be made, no raw RSA signature as in older versions
	    // see http://tools.ietf.org/html/rfc5246#section-4.7
	    if (TlsUtils.isTLSv12(context)) {
		SignatureAndHashAlgorithm sigAlg = getSignatureAndHashAlgorithm();
		if (sigAlg.getSignature() == SignatureAlgorithm.rsa) {
		    ASN1ObjectIdentifier hashAlgId = TlsUtils.getOIDForHashAlgorithm(sigAlg.getHash());
		    DigestInfo digestInfo = new DigestInfo(new AlgorithmIdentifier(hashAlgId), hash);
		    hash = digestInfo.getEncoded(ASN1Encoding.DER);
		}
	    }
	    // perform the signature on the card
	    try {
		return signerImpl.sign(hash);
	    } catch (SignatureException ex) {
		throw new IOException("Failed to create signature because of an unknown error.", ex);
	    } catch (CredentialPermissionDenied ex) {
		throw new IOException("Failed to create signature because of missing permissions.", ex);
	    }
	} else {
	    return new byte[]{};
	}
    }

    @Override
    public synchronized Certificate getCertificate() {
	if (certificate.equals(Certificate.EMPTY_CHAIN)) {
	    try {
		certificate = signerImpl.getBCCertificateChain();
	    } catch (IOException ex) {
		logger.error("Failed to read certificate due to an unknown error.", ex);
	    } catch (CredentialPermissionDenied ex) {
		logger.error("Failed to get certificate because of missing permissions.", ex);
	    } catch (CertificateException ex) {
		logger.error("Failed to deserialize certificate.", ex);
	    }
	}
	return certificate;
    }

    @Override
    public SignatureAndHashAlgorithm getSignatureAndHashAlgorithm() {
	return signerImpl.getSignatureAndHashAlgorithm();
    }

}
