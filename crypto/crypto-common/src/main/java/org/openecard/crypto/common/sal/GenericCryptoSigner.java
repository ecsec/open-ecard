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

package org.openecard.crypto.common.sal;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.openecard.common.interfaces.Dispatcher;


/**
 * Wrapper for the sign functionality of generic crypto DIDs.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class GenericCryptoSigner {

    private final Dispatcher dispatcher;
    private final ConnectionHandleType handle;
    private final String didName;

    private byte[] rawCertData;
    private Map<String, java.security.cert.Certificate[]> javaCerts;
    private org.openecard.bouncycastle.crypto.tls.Certificate bcCert;

    /**
     * Creates a Generic Crypto signer and defines the card, application and target DID through the parameters.
     *
     * @param dispatcher Dispatcher used to talk to the SAL.
     * @param handle Handle naming the SAL, card and application of the DID. The application connection can change over
     *   time and does not even have to be selected when this method is called.
     * @param didName Name of the DID this instance encapsulates.
     */
    public GenericCryptoSigner(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle,
	    @Nonnull String didName) {
	this.dispatcher = dispatcher;
	this.handle = handle;
	this.didName = didName;
	this.javaCerts = new HashMap<String, java.security.cert.Certificate[]>();
    }


    /**
     * Gets the certificate for this generic crypto DID.
     * This function returns the certificate in encoded form exactly as it is saved on the respective token. The
     * certificate is not converted whatsoever.
     *
     * @return Certificate of this DID in encoded form.
     * @throws CredentialPermissionDenied In case the certifcate could not be read from the token.
     * @throws IOException In case any other error occured during the reading of the certificate.
     */
    public synchronized byte[] getCertificateChain() throws CredentialPermissionDenied, IOException {
	// TODO: cache certificate
	throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * Gets the certificate for this generic crypto DID converted to a Java security certificate.
     * This method is just a convenience function to call the equivalent with the parameter {@code X.509}.
     *
     * @see #getJavaSecCertificateChain(java.lang.String)
     */
    public java.security.cert.Certificate[] getJavaSecCertificateChain() throws CredentialPermissionDenied,
	    CertificateException, IOException {
	return getJavaSecCertificateChain("X.509");
    }
    /**
     * Gets the certificate for this generic crypto DID converted to a Java security certificate.
     * The type parameter is used to determine the requested certificate type. Each certificate type is cached once it is
     * requested.
     *
     * @param certType Certificate typoe according to <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#CertificateFactory">
     *   CertificateFactory Types</a>
     * @return An array representing the certificate chain of this DID.
     * @throws CredentialPermissionDenied In case the certifcate could not be read from the token. See
     *   {@link #getCertificateChain()}.
     * @throws IOException In case any other error occured during the reading of the certificate. See
     *   {@link #getCertificateChain()}.
     * @throws CertificateException In case the certificate could not be converted.
     */
    @Nonnull
    public java.security.cert.Certificate[] getJavaSecCertificateChain(@Nonnull String certType)
	    throws CredentialPermissionDenied, CertificateException, IOException {
	// is the certificate already available in java.security form?
	if (! javaCerts.containsKey(certType)) {
	    byte[] certs = getCertificateChain();
	    CertificateFactory cf = CertificateFactory.getInstance(certType);
	    Collection<? extends java.security.cert.Certificate> javaCert;
	    javaCert = cf.generateCertificates(new ByteArrayInputStream(certs));
	    javaCerts.put(certType, javaCert.toArray(new java.security.cert.Certificate[javaCert.size()]));
	}

	return javaCerts.get(certType);
    }

    /**
     * Gets the certificate for this generic crypto DID converted to a BouncyCastle TLS certificate.
     *
     * @return The certificate chain in BouncyCastle format.
     * @throws CredentialPermissionDenied In case the certifcate could not be read from the token. See
     *   {@link #getCertificateChain()}.
     * @throws IOException In case any other error occured during the reading of the certificate. See
     *   {@link #getCertificateChain()}.
     * @throws CertificateException In case the certificate could not be converted.
     */
    @Nonnull
    public org.openecard.bouncycastle.crypto.tls.Certificate getBCCertificateChain() throws CredentialPermissionDenied,
	    CertificateException, IOException {
	// is the certificate already available in BC form?
	if (bcCert == null) {
	    byte[] certs = getCertificateChain();
	    try {
		bcCert = org.openecard.bouncycastle.crypto.tls.Certificate.parse(new ByteArrayInputStream(certs));
	    } catch (IOException ex) {
		throw new CertificateException(ex);
	    }
	}

	return bcCert;
    }

    /**
     * Signs the given hash with the DID represented by this instance.
     *
     * @param hash The hash that should be signed.
     * @return Signature of the given hash.
     * @throws SignatureException In case the signature could not be created. Causes are most likely problems in the
     *   SAL, such as a removed card.
     * @throws CredentialPermissionDenied In case the signature could not be performed by the token due to missing
     *   permissions.
     */
    public byte[] sign(@Nonnull byte[] hash) throws SignatureException, CredentialPermissionDenied {
	throw new UnsupportedOperationException("Not implemented yet!");
    }

}
