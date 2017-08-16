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

import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.security.auth.x500.X500Principal;
import org.openecard.bouncycastle.asn1.x500.X500Name;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.crypto.common.HashAlgorithms;
import org.openecard.crypto.common.KeyTypes;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.DataSetInfo;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.openecard.crypto.common.sal.did.DidInfos;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.openecard.crypto.common.sal.did.TokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class SmartCardCredentialFactory implements CredentialFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardCredentialFactory.class);

    private final TokenCache tokenCache;
    private final ConnectionHandleType handle;
    private final boolean filterAlwaysReadable;

    public SmartCardCredentialFactory(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle,
	    boolean filterAlwaysReadable) {
	this.tokenCache = new TokenCache(dispatcher);
	this.handle = handle;
	this.filterAlwaysReadable = filterAlwaysReadable;
    }

    @Override
    public List<TlsSignerCredentials> getClientCredentials(CertificateRequest cr) {
	ArrayList<TlsSignerCredentials> credentials = new ArrayList<>();

	try {
	    DidInfos didInfos = tokenCache.getInfo(null, handle);
	    List<DidInfo> infos = didInfos.getCryptoDidInfos();

	    for (DidInfo info : infos) {
		try {
		    // filter out dids having secret certificates
		    if (filterAlwaysReadable && isCertNeedsPin(info)) {
			continue;
		    }

		    AlgorithmInfoType algInfo = info.getGenericCryptoMarker().getAlgorithmInfo();
		    SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
		    SignatureAndHashAlgorithm bcAlg = convertSignatureAlgorithm(alg);

		    List<X509Certificate> chain = info.getRelatedCertificateChain();
		    if (isAlgAllowed(cr, info) && matchesCertReq(cr, chain) && isAuthCert(info, chain)) {
			SmartCardSignerCredential cred;
			// raw rsa signatures need special handling
			if (isRawRSA(info)) {
			    if (cr.getSupportedSignatureAlgorithms() == null) {
				// TLS < 1.2
				LOG.debug("Adding raw RSA signer.");
				cred = new RawRsaSmartCardSignerCredential(info, chain, bcAlg);
				credentials.add(cred);
			    } else {
				// TLS >= 1.2
				for (Object algObj : cr.getSupportedSignatureAlgorithms()) {
				    SignatureAndHashAlgorithm reqAlg = (SignatureAndHashAlgorithm) algObj;
				    if (reqAlg.getSignature() == SignatureAlgorithm.rsa) {
					switch (reqAlg.getHash()) {
					    case HashAlgorithm.sha1:
					    case HashAlgorithm.sha224:
					    case HashAlgorithm.sha256:
					    case HashAlgorithm.sha384:
					    case HashAlgorithm.sha512:
						LOG.debug("Adding raw {}withRSA signer.", HashAlgorithm.getName(reqAlg.getHash()).toUpperCase());
						cred = new RawRsaSmartCardSignerCredential(info, chain, reqAlg);
						credentials.add(cred);
					}
				    }
				}
			    }
			} else {
			    // DID names a specific algorithm
			    LOG.debug("Adding specific {} signer.", alg.getJcaAlg());
			    cred = new SmartCardSignerCredential(info, chain, bcAlg);
			    credentials.add(cred);
			}
		    }
		} catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException ex) {
		    LOG.error("Failed to read certificates from card. Skipping DID " + info.getDidName() + ".", ex);
		} catch (UnsupportedAlgorithmException ex) {
		    LOG.error("Unsupported algorithm used in CIF. Skipping DID " + info.getDidName() + ".", ex);
		} catch (WSHelper.WSException ex) {
		    LOG.error("Unknown error accessing DID " + info.getDidName() + ".", ex);
		}
	    }
	} catch (NoSuchDid | WSHelper.WSException ex) {
	    LOG.error("Failed to access DIDs of smartcard. Proceeding without client authentication.", ex);
	}

	return credentials;
    }

    private boolean isCertNeedsPin(DidInfo info) throws WSHelper.WSException, SecurityConditionUnsatisfiable {
	boolean needsPin = false;
	List<DataSetInfo> dsis = info.getRelatedDataSets();
	for (DataSetInfo dsi : dsis) {
	    needsPin = needsPin && dsi.needsPin();
	}
	return needsPin;
    }

    private boolean matchesCertReq(CertificateRequest cr, List<X509Certificate> chain) {
	// no issuers mean accept anything
	if (cr.getCertificateAuthorities().isEmpty()) {
	    LOG.debug("Any certificate matches.");
	    return true;
	}

	// check if any of the certificates has an issuer mathing the request
	for (Object issuerObj : cr.getCertificateAuthorities()) {
	    try {
		X500Name issuer = (X500Name) issuerObj;
		X500Principal reqIss = new X500Principal(issuer.getEncoded("DER"));
		for (X509Certificate cert : chain) {
		    if (cert.getIssuerX500Principal().equals(reqIss)) {
			LOG.debug("Issuer {} matched one of the certificate issuers.", issuer);
			return true;
		    }
		}
	    } catch (IOException ex) {
		LOG.error("Unencodable issuer in requested authorities. Skipping entry.");
	    }
	}

	// no issuer matched
	LOG.debug("No issuer matches.");
	return false;
    }

    private boolean isAuthCert(DidInfo info, List<X509Certificate> chain) throws WSHelper.WSException {
	AlgorithmInfoType algInfo = info.getGenericCryptoMarker().getAlgorithmInfo();
	if (! algInfo.getSupportedOperations().contains("Compute-signature")) {
	    LOG.debug("DID ({}): AlgorithmInfo does not provide Compute-signature flag.", info.getDidName());
	    return false;
	}

	// check authentication (digital signature) flag
	X509Certificate cert = chain.get(0);
	if (! cert.getKeyUsage()[0]) {
	    LOG.debug("DID ({}): Certificate key usage does not permit authentication signatures.", info.getDidName());
	    return false;
	}

	return true;
    }

    private boolean isAlgAllowed(CertificateRequest cr, DidInfo did) throws WSHelper.WSException,
	    UnsupportedAlgorithmException {
	boolean preTls12 = cr.getSupportedSignatureAlgorithms() == null;

	AlgorithmInfoType algInfo = did.getGenericCryptoMarker().getAlgorithmInfo();
	SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
	SignatureAndHashAlgorithm bcAlg = convertSignatureAlgorithm(alg);

	if (preTls12) {
	    return alg == SignatureAlgorithms.CKM_RSA_PKCS;
	}

	// match against whitelist
	switch (alg) {
	    case CKM_RSA_PKCS:
	    case CKM_ECDSA_SHA1:
	    case CKM_ECDSA_SHA224:
	    case CKM_ECDSA_SHA256:
	    case CKM_ECDSA_SHA384:
	    case CKM_ECDSA_SHA512:
	    case CKM_SHA1_RSA_PKCS:
	    case CKM_SHA224_RSA_PKCS:
	    case CKM_SHA256_RSA_PKCS:
	    case CKM_SHA384_RSA_PKCS:
	    case CKM_SHA512_RSA_PKCS:
		break;
	    default:
		return false;
	}

	// match against negotiated algorithms
	for (Object algObj : cr.getSupportedSignatureAlgorithms()) {
	    SignatureAndHashAlgorithm negAlg = (SignatureAndHashAlgorithm) algObj;
	    if (bcAlg.equals(algObj)) {
		return true;
	    }
	}

	return false;
    }

    private boolean isRawRSA(DidInfo info) throws WSHelper.WSException, UnsupportedAlgorithmException {
	AlgorithmInfoType algInfo = info.getGenericCryptoMarker().getAlgorithmInfo();
	SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
	return SignatureAlgorithms.CKM_RSA_PKCS == alg;
    }

    public static SignatureAndHashAlgorithm convertSignatureAlgorithm(SignatureAlgorithms alg) {
	    HashAlgorithms hashAlg = alg.getHashAlg();
	    KeyTypes keyType = alg.getKeyType();

	    short hash;
	    if (hashAlg != null) {
		switch (hashAlg) {
		    case CKM_SHA_1: hash = HashAlgorithm.sha1; break;
		    case CKM_SHA224: hash = HashAlgorithm.sha224; break;
		    case CKM_SHA256: hash = HashAlgorithm.sha256; break;
		    case CKM_SHA384: hash = HashAlgorithm.sha384; break;
		    case CKM_SHA512: hash = HashAlgorithm.sha512; break;
		    default: throw new IllegalArgumentException("Unsupported hash algorithm selected.");
		}
	    } else {
		hash = HashAlgorithm.none;
	    }

	    short sig;
	    switch (keyType) {
		case CKK_RSA: sig = SignatureAlgorithm.rsa; break;
		case CKK_EC: sig = SignatureAlgorithm.ecdsa; break;
		default: throw new IllegalArgumentException("Unsupported signature algorithm selected.");
	    }

	    return new SignatureAndHashAlgorithm(hash, sig);

    }

}
