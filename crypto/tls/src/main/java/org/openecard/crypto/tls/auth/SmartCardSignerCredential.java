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
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.crypto.common.sal.CredentialPermissionDenied;
import org.openecard.crypto.common.sal.GenericCryptoSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SmartCardSignerCredential implements TlsSignerCredentials {

    private static final Logger logger = LoggerFactory.getLogger(SmartCardSignerCredential.class);

    private final GenericCryptoSigner signerImpl;

    public SmartCardSignerCredential(@Nonnull GenericCryptoSigner signerImpl) {
	this.signerImpl = signerImpl;
    }

    @Override
    public byte[] generateCertificateSignature(@Nonnull byte[] md5andsha1) throws IOException {
	try {
	    return signerImpl.sign(md5andsha1);
	} catch (SignatureException ex) {
	    throw new IOException("Failed to create signature because of an unknown error.", ex);
	} catch (CredentialPermissionDenied ex) {
	    throw new IOException("Failed to create signature because of missing permissions.", ex);
	}
    }

    @Override
    public synchronized Certificate getCertificate() {
	try {
	    return signerImpl.getBCCertificateChain();
	} catch (IOException ex) {
	    logger.error("Failed to read certificate due to an unknown error.", ex);
	    return Certificate.EMPTY_CHAIN;
	} catch (CredentialPermissionDenied ex) {
	    logger.error("Failed to get certificate because of missing permissions.", ex);
	    return Certificate.EMPTY_CHAIN;
	} catch (CertificateException ex) {
	    logger.error("Failed to deserialize certificate.", ex);
	    return Certificate.EMPTY_CHAIN;
	}
    }

}
