/****************************************************************************
 * Copyright (C) 2013-2017 HS Coburg.
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
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.crypto.common.keystore.KeyStoreSigner;
import org.openecard.crypto.common.sal.did.CredentialPermissionDenied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Signer credential wrapping the given KeyStoreSigner.
 *
 * @see KeyStoreSigner
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class KeyStoreCredential implements TlsSignerCredentials {

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreCredential.class);

    private final KeyStoreSigner signer;
    private final SignatureAndHashAlgorithm sigHashAlg;
    private Certificate certificate = null;

    public KeyStoreCredential(@Nonnull KeyStoreSigner signer) {
	this(signer, new SignatureAndHashAlgorithm(HashAlgorithm.sha1, SignatureAlgorithm.rsa));
    }

    public KeyStoreCredential(@Nonnull KeyStoreSigner signer, SignatureAndHashAlgorithm sigHashAlg) {
	this.signer = signer;
	this.sigHashAlg = sigHashAlg;
    }

    @Override
    public Certificate getCertificate() {
	if (certificate == null) {
	    try {
		certificate = signer.getCertificateChain();
	    } catch (CertificateException ex) {
		LOG.error("Failed to deserialize certificate.", ex);
		certificate = Certificate.EMPTY_CHAIN;
	    }
	}
	return certificate;
    }

    @Override
    public byte[] generateCertificateSignature(byte[] hash) throws IOException {
	try {
	    return signer.sign(sigHashAlg, hash);
	}  catch (SignatureException ex) {
	    throw new IOException("Failed to create signature because of an unknown error.", ex);
	} catch (CredentialPermissionDenied ex) {
	    throw new IOException("Failed to create signature because of missing permissions.", ex);
	}
    }

    @Override
    public SignatureAndHashAlgorithm getSignatureAndHashAlgorithm() {
	return sigHashAlg;
    }

}
