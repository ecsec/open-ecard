/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

import org.openecard.crypto.tls.verify.CertificateVerifierBuilder;
import org.openecard.crypto.tls.verify.KeyLengthVerifier;
import org.openecard.crypto.tls.verify.HostnameVerifier;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsContext;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.TlsServerCertificate;
import org.openecard.crypto.tls.CertificateVerifier;
import org.openecard.crypto.tls.verify.ExpirationVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the TlsAuthentication interface for certificate verification.
 *
 * @author Tobias Wich
 */
public class DynamicAuthentication implements TlsAuthentication, ContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicAuthentication.class);

    private String hostname;
    private CertificateVerifier certVerifier;
    private CredentialFactory credentialFactory;
    private TlsServerCertificate lastCertChain;
    private TlsContext context;

    /**
     * Nullary constructor.
     * If no parameters are set later through setter functions, this instance will perform no server certificate checks
     * and return an empty client certificate list.
     *
     * @param hostName Name or IP of the host that will be used for certificate validation when a verifier is set.
     */
    public DynamicAuthentication(@Nonnull String hostName) {
	this(hostName, new CertificateVerifierBuilder()
		.and(new HostnameVerifier())
		.and(new KeyLengthVerifier())
		.and(new ExpirationVerifier())
		.build(),
		null);
    }

    /**
     * Create a new DynamicAuthentication using the given parameters.
     * They can later be changed using the setter functions.
     *
     * @param hostName Name or IP of the host that will be used for certificate validation when a verifier is set.
     * @param certVerifier Verifier used for server certificate checks.
     * @param credentialFactory Factory that provides client credentials when they are requested from the server.
     */
    public DynamicAuthentication(@Nonnull String hostName, @Nullable CertificateVerifier certVerifier,
	    @Nullable CredentialFactory credentialFactory) {
	this.hostname = hostName;
	this.certVerifier = certVerifier;
	this.credentialFactory = credentialFactory;
    }

    @Override
    public void setContext(TlsContext context) {
	this.context = context;
    }

    /**
     * Sets the host name for the certificate verification step.
     *
     * @see #notifyServerCertificate(org.bouncycastle.tls.TlsServerCertificate)
     * @param hostname Name or IP of the host that will be used for certificate validation, when a verifier is set.
     */
    public void setHostname(@Nonnull String hostname) {
	this.hostname = hostname;
    }

    /**
     * Sets the implementation for the certificate verification step.
     *
     * @see #notifyServerCertificate(org.bouncycastle.tls.TlsServerCertificate)
     * @see CertificateVerifier
     * @param certVerifier Verifier to use for server certificate checks.
     */
    public void setCertificateVerifier(CertificateVerifier certVerifier) {
	this.certVerifier = certVerifier;
    }

    /**
     * Adds a certificate verifier to the chain of the certificate verifiers.
     *
     * @param certVerifier The verifier to add.
     */
    public void addCertificateVerifier(@Nonnull CertificateVerifier certVerifier) {
	CertificateVerifierBuilder builder = new CertificateVerifierBuilder();
	if (this.certVerifier != null) {
	    builder = builder.and(this.certVerifier);
	}
	this.certVerifier = builder.and(certVerifier).build();
    }

    /**
     * Sets the factory which is used to find and create a credential reference for the authentication.
     *
     * @see #getClientCredentials(org.bouncycastle.crypto.tls.CertificateRequest)
     * @param credentialFactory Factory that provides client credentials when they are requested from the server.
     */
    public void setCredentialFactory(@Nullable CredentialFactory credentialFactory) {
	this.credentialFactory = credentialFactory;
    }


    /**
     * Verify the server certificate of the TLS handshake.
     * In case no implementation is set (via {@link #setCertificateVerifier(CertificateVerifier)}), no action is
     * performed.<br>
     * The actual implementation is responsible for the types of verification that are performed. Besides the usual
     * hostname and certificate chain verification, those types could also include CRL and OCSP checking.
     *
     * @see CertificateVerifier
     * @param serverCert Certificate chain of the server as transmitted in the TLS handshake.
     * @throws IOException when certificate verification failed.
     */
    @Override
    public void notifyServerCertificate(TlsServerCertificate serverCert) throws IOException {
	boolean noServerCert = serverCert == null || serverCert.getCertificate() == null
		|| serverCert.getCertificate().isEmpty();
	if (noServerCert) {
	    throw new TlsFatalAlert(AlertDescription.handshake_failure);
	} else {
	    // save server certificate
	    this.lastCertChain = serverCert;
	    // try to validate
	    if (certVerifier != null) {
		// perform validation depending on the available parameters
		certVerifier.isValid(serverCert, hostname);
	    } else {
		// no verifier available
		LOG.warn("No certificate verifier available, skipping certificate verification.");
	    }
	}
    }

    /**
     * Gets the client credentials based on the credential factory saved in this instance, or an empty credential.
     * From RFC 4346 sec. 7.4.6:
     * <p>If no suitable certificate is available, the client SHOULD send a certificate message containing no
     * certificates.</p>
     *
     * @param cr Certificate request as received in the TLS handshake.
     * @see CredentialFactory
     */
    @Override
    public TlsCredentials getClientCredentials(CertificateRequest cr) {
	if (credentialFactory != null) {
	    if (credentialFactory instanceof ContextAware) {
		((ContextAware) credentialFactory).setContext(context);
	    }
	    List<TlsCredentialedSigner> credentials = credentialFactory.getClientCredentials(cr);
	    if (! credentials.isEmpty()) {
		TlsCredentials cred = credentials.get(0);
		// in case the credential understands the context supply it
		if (cred instanceof ContextAware) {
		    ((ContextAware) cred).setContext(context);
		}
		return cred;
	    }
	}
	// fall back to no auth, when no credential is found
	return null;
    }

    /**
     * Returns the certificate chain which is processed during the TLS authentication.
     *
     * @return The certificate chain of the last certificate validation or null if none is available.
     */
    @Nullable
    public TlsServerCertificate getServerCertificate() {
	return lastCertChain;
    }

}
