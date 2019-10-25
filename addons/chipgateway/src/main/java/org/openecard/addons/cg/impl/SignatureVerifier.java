/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import org.openecard.addons.cg.ex.InvalidSubjectException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import org.openecard.addons.cg.activate.CGTrustStoreLoader;
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.cert.X509CertificateHolder;
import org.openecard.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.openecard.bouncycastle.cms.CMSException;
import org.openecard.bouncycastle.cms.CMSProcessable;
import org.openecard.bouncycastle.cms.CMSProcessableByteArray;
import org.openecard.bouncycastle.cms.CMSSignedData;
import org.openecard.bouncycastle.cms.CMSVerifierCertificateNotValidException;
import org.openecard.bouncycastle.cms.SignerInformation;
import org.openecard.bouncycastle.cms.SignerInformationStore;
import org.openecard.bouncycastle.cms.SignerInformationVerifier;
import org.openecard.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.openecard.bouncycastle.operator.DigestCalculatorProvider;
import org.openecard.bouncycastle.operator.OperatorCreationException;
import org.openecard.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.openecard.bouncycastle.util.Selector;
import org.openecard.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class SignatureVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureVerifier.class);

    private final KeyStore trustStore;
    private final byte[] challenge;

    public SignatureVerifier(@Nonnull KeyStore trustStore, @Nonnull byte[] challenge) {
	this.trustStore = trustStore;
	this.challenge = challenge;
    }

    public SignatureVerifier(@Nonnull byte[] challenge) throws IOException, KeyStoreException,
	    NoSuchAlgorithmException, CertificateException {
	this(new CGTrustStoreLoader().getTrustStore(), challenge);
    }

    public void validate(@Nonnull byte[] signature) throws KeyStoreException, SignatureInvalid {
	try {
	    // load BC provider, so that the algorithms are available for the signature verification
	    Security.addProvider(new BouncyCastleProvider());

	    CMSProcessable wrappedChallenge = new CMSProcessableByteArray(challenge);
	    CMSSignedData signedData = new CMSSignedData(wrappedChallenge, signature);

	    Store<X509CertificateHolder> certStore = signedData.getCertificates();
	    SignerInformationStore signerInfoStore = signedData.getSignerInfos();
	    Collection<SignerInformation> signers = signerInfoStore.getSigners();

	    Collection<X509Certificate> allCerts = convertCertificates(certStore.getMatches(new AllSelector()));

	    for (SignerInformation signer : signers) {
		Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());
		X509CertificateHolder cert = certCollection.iterator().next();

		DigestCalculatorProvider dp = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
		JcaSignerInfoVerifierBuilder verifBuilder = new JcaSignerInfoVerifierBuilder(dp).setProvider(BouncyCastleProvider.PROVIDER_NAME);
		verifBuilder.setSignatureAlgorithmFinder(new DefaultSignatureAlgorithmIdentifierFinder() {
		    @Override
		    public AlgorithmIdentifier find(String sigAlgName) {
			if (! AllowedSignatureAlgorithms.isKnownJcaAlgorithm(sigAlgName)) {
			    throw new IllegalArgumentException("Unsupported signature algorithm used.");
			} else {
			    return super.find(sigAlgName);
			}
		    }
		});
		SignerInformationVerifier verif = verifBuilder.build(cert);

		// verify the signature
		if (! signer.verify(verif)) {
		    throw new SignatureInvalid("Signer information could not be verified.");
		}

		// verify the path and certificate
		X509Certificate x509Cert = convertCertificate(cert);
		// TODO: verify that the signature is not too old. How old can it be at max? 1 minute?
		validatePath(x509Cert, allCerts, null);

		// check that the end certificate is under the admissable certificates
		if (ChipGatewayProperties.isUseSubjectWhitelist()) {
		    X500Principal subj = x509Cert.getSubjectX500Principal();
		    if (! AllowedSubjects.instance().isInSubjects(subj)) {
			String msg = "The certificate used in the signature has an invalid subject: " + subj.getName();
			throw new InvalidSubjectException(msg);
		    }
		}
	    }

	    // fail if there is no signature in the SignedData structure
	    if (signers.isEmpty()) {
		throw new SignatureInvalid("No signatures present in the given SignedData element.");
	    }
	} catch (CertificateException ex) {
	    throw new SignatureInvalid("Failed to read a certificate form the CMS data structure.", ex);
	} catch (CertPathBuilderException ex) {
	    throw new SignatureInvalid("Failed to build certificate path for PKIX validation.", ex);
	} catch (CMSVerifierCertificateNotValidException ex) {
	    throw new SignatureInvalid("Signer certificate was not valid when the signature was created.", ex);
	} catch (CMSException ex) {
	    throw new SignatureInvalid("Failed to validate CMS data structure.", ex);
	} catch (InvalidSubjectException ex) {
	    throw new SignatureInvalid("Certificate with invalid subject used in signature.", ex);
	} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | OperatorCreationException ex) {
	    throw new SignatureInvalid("Invalid or unsupported algorithm or algorithm parameter used in signature.", ex);
	} catch (IllegalArgumentException ex) {
	    throw new SignatureInvalid("Signature containes an invalid value.", ex);
	}
    }

    private PKIXCertPathBuilderResult validatePath(X509Certificate cert, Collection<X509Certificate> intermediateCerts,
	    @Nullable Date checkDate)
	    throws NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException, CertPathBuilderException {
	// enable downloading of missing certificates based on the AIA extension
	try {
	    System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
	} catch (SecurityException ex) {
	    LOG.warn("Failed to enable AIA evaluation. Skipping downloads of missing certificates.");
	}

	CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

	// configure path building
	X509CertSelector target = new X509CertSelector();
	target.setCertificate(cert);
	PKIXBuilderParameters params = new PKIXBuilderParameters(trustStore, target);
	CertStoreParameters intermediates = new CollectionCertStoreParameters(intermediateCerts);
	params.addCertStore(CertStore.getInstance("Collection", intermediates));
	params.setDate(checkDate);

	params.setRevocationEnabled(false);
	if (ChipGatewayProperties.isRevocationCheck()) {
	    PKIXRevocationChecker revChecker = (PKIXRevocationChecker) builder.getRevocationChecker();
	    Set<PKIXRevocationChecker.Option> revOpts = new HashSet<>();
	    //revOpts.add(PKIXRevocationChecker.Option.ONLY_END_ENTITY);
	    revChecker.setOptions(revOpts);
	    params.setCertPathCheckers(null);
	    params.addCertPathChecker(revChecker);
	}

	// try to build the path
	PKIXCertPathBuilderResult r = (PKIXCertPathBuilderResult) builder.build(params);
	return r;
    }


    private X509Certificate convertCertificate(X509CertificateHolder certHolder) throws CertificateException {
	return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

    private Collection<X509Certificate> convertCertificates(Collection<X509CertificateHolder> certHolders) {
	ArrayList<X509Certificate> certs = new ArrayList<>(certHolders.size());
	for (X509CertificateHolder next : certHolders) {
	    try {
		certs.add(convertCertificate(next));
	    } catch (CertificateException ex) {
		// just skip the certificate
	    }
	}
	return certs;
    }


    private static class AllSelector<T> implements Selector<T> {

	@Override
	public boolean match(T obj) {
	    return true;
	}

	@Override
	public Object clone() {
	    return new AllSelector();
	}

    }

}
