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

package org.openecard.crypto.tls.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.*;

import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCertificate;
import org.openecard.common.util.Pair;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Java Security based certificate verifier.
 * This implementation converts the BouncyCastle certificates to java.security certificates and uses the Java-bundled
 * PKIX mechanism to verify the certificate chain.
 *
 * @author Tobias Wich
 */
public class JavaSecVerifier implements CertificateVerifier {

    protected final boolean checkRevocation;
    protected final CertPathBuilder certPathValidator;

    /**
     * Create a JavaSecVerifier and load the internal certificate path validator.
     *
     * @throws RuntimeException Thrown in case the validator could not be loaded due to a missing algorithm.
     */
    public JavaSecVerifier() throws RuntimeException {
	this(false);
    }

    public JavaSecVerifier(boolean checkRevocation) throws RuntimeException {
	this.checkRevocation = checkRevocation;
	try {
	    certPathValidator = CertPathBuilder.getInstance("PKIX");
	} catch (NoSuchAlgorithmException ex) {
	    throw new RuntimeException("Failed to load CertPathBuilder");
	}
    }

    protected Set<TrustAnchor> getTrustStore() {
	return new TrustStoreLoader().getTrustAnchors();
    }

    @Override
    public void isValid(TlsServerCertificate chain, String hostname) throws CertificateVerificationException {
	validateCertificate(chain, hostname);
    }

    protected CertPathBuilderResult validateCertificate(TlsServerCertificate chain, String hostname) throws CertificateVerificationException {
	try {
	    Set<TrustAnchor> trustStore = getTrustStore();
	    Pair<X509Certificate, CertStore> path = buildChain(chain);

	    CertPathBuilder cpb = certPathValidator;
	    X509CertSelector targetSelector = new X509CertSelector();
	    targetSelector.setCertificate(path.p1);

	    // create the parameters for the validator
	    PKIXBuilderParameters cpp = new PKIXBuilderParameters(trustStore, targetSelector);
	    cpp.addCertStore(path.p2);
	    if (checkRevocation) {
		//				cpp.setRevocationEnabled(true);
		//				System.setProperty("com.sun.security.enableCRLDP", "true");
		cpp.setRevocationEnabled(false);
		PKIXRevocationChecker revChecker = (PKIXRevocationChecker) cpb.getRevocationChecker();
		Set<PKIXRevocationChecker.Option> revOpts = new HashSet<>();
		//revOpts.add(PKIXRevocationChecker.Option.ONLY_END_ENTITY);
		revChecker.setOptions(revOpts);
		// TODO: add OCSP responses
		//revChecker.setOcspResponses(responses);
		cpp.setCertPathCheckers(null);
		cpp.addCertPathChecker(revChecker);
	    } else {
		// disable CRL checking since we are not supplying any CRLs yet
		cpp.setRevocationEnabled(false);
	    }

	    // build path performs the validation - exception marks failure
	    CertPathBuilderResult result = cpb.build(cpp);
	    return result;

	} catch (CertPathBuilderException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (GeneralSecurityException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (IOException ex) {
	    throw new CertificateVerificationException("Error converting certificate chain to java.security format.");
	}
    }

    private Pair<X509Certificate, CertStore> buildChain(TlsServerCertificate chain) throws CertificateException, IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
	ArrayList<X509Certificate> auxCerts = new ArrayList<>();
	CertificateFactory cf = CertificateFactory.getInstance("X.509");

	for (TlsCertificate next : chain.getCertificate().getCertificateList()) {
	    X509Certificate nextConverted = convertCertificateInt(cf, next);
	    auxCerts.add(nextConverted);
	}

	X509Certificate eeCert = auxCerts.get(0);
	CertStore auxCertStore = buildAuxCertStore(auxCerts);

	return new Pair<>(eeCert, auxCertStore);
    }

    private CertStore buildAuxCertStore(Collection certs) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
	CollectionCertStoreParameters params = new CollectionCertStoreParameters(certs);
	CertStore store = CertStore.getInstance("Collection", params);
	return store;
    }


    public static CertPath convertChain(TlsServerCertificate chain) throws CertificateException, IOException {
	final int numCerts = chain.getCertificate().getCertificateList().length;
	ArrayList<java.security.cert.Certificate> result = new ArrayList<>(numCerts);
	CertificateFactory cf = CertificateFactory.getInstance("X.509");

	for (TlsCertificate next : chain.getCertificate().getCertificateList()) {
	    Certificate nextConverted = convertCertificateInt(cf, next);
	    result.add(nextConverted);
	}

	return cf.generateCertPath(result);
    }

    public static Certificate convertCertificate(TlsCertificate cert) throws CertificateException, IOException {
	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	return convertCertificateInt(cf, cert);
    }

    public static X509Certificate convertCertificateInt(CertificateFactory cf, TlsCertificate cert)
	throws CertificateException, IOException {
	byte[] nextData = cert.getEncoded();
	ByteArrayInputStream nextDataStream = new ByteArrayInputStream(nextData);
	X509Certificate nextConverted = (X509Certificate) cf.generateCertificate(nextDataStream);
	return nextConverted;
    }

}
