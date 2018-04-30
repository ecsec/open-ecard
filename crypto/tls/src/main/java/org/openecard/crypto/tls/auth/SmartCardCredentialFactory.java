/****************************************************************************
 * Copyright (C) 2013-2018 ecsec GmbH.
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
import java.io.StringWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import org.openecard.bouncycastle.asn1.x500.X500Name;
import org.openecard.bouncycastle.tls.Certificate;
import org.openecard.bouncycastle.tls.CertificateRequest;
import org.openecard.bouncycastle.tls.DefaultTlsCredentialedSigner;
import org.openecard.bouncycastle.tls.HashAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.tls.TlsContext;
import org.openecard.bouncycastle.tls.TlsCredentialedSigner;
import org.openecard.bouncycastle.tls.crypto.TlsCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
import org.openecard.bouncycastle.tls.crypto.TlsCryptoParameters;
import org.openecard.bouncycastle.tls.crypto.TlsSigner;
import org.openecard.bouncycastle.util.io.pem.PemObject;
import org.openecard.bouncycastle.util.io.pem.PemWriter;
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
public class SmartCardCredentialFactory implements CredentialFactory, ContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardCredentialFactory.class);

    private final TokenCache tokenCache;
    private final ConnectionHandleType handle;
    private final boolean filterAlwaysReadable;

    private TlsContext context;

    public SmartCardCredentialFactory(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle,
	    boolean filterAlwaysReadable) {
	this.tokenCache = new TokenCache(dispatcher);
	this.handle = handle;
	this.filterAlwaysReadable = filterAlwaysReadable;
    }

    @Override
    public void setContext(TlsContext context) {
	this.context = context;
    }

    @Override
    public List<TlsCredentialedSigner> getClientCredentials(CertificateRequest cr) {
	ArrayList<TlsCredentialedSigner> credentials = new ArrayList<>();
	TlsCryptoParameters tlsCrypto = new TlsCryptoParameters(context);

	LOG.debug("Selecting a suitable DID for the following requested algorithms:");
	ArrayList<SignatureAndHashAlgorithm> crSigAlgs = getCrSigAlgs(cr);
	removeUnsupportedAlgs(crSigAlgs);
	for (SignatureAndHashAlgorithm reqAlg : crSigAlgs) {
	    String reqAlgStr = String.format("%s-%s", SignatureAlgorithm.getText(reqAlg.getSignature()),
		    HashAlgorithm.getText(reqAlg.getHash()));
	    LOG.debug("  {}", reqAlgStr);
	}

	try {
	    DidInfos didInfos = tokenCache.getInfo(null, handle);
	    List<DidInfo> infos = didInfos.getCryptoDidInfos();

	    printCerts(infos);

	    // remove unsuitable DIDs
	    LOG.info("Sorting out DIDs not able to handle the TLS request.");
	    infos = removeSecretCertDids(infos);
	    infos = removeNonAuthDids(infos);
	    infos = removeUnsupportedAlgs(infos);
	    infos = removeUnsupportedCerts(cr, infos);

	    //infos = nonRawFirst(infos);

	    LOG.info("Creating signer instances for the TLS Client Certificate signature.");

	    // TLS < 1.2
	    if (crSigAlgs.isEmpty()) {
		LOG.info("Looking for a raw RSA DID.");

		for (DidInfo info : infos) {
		    try {
			LOG.debug("Checking DID= {}.", info.getDidName());

			TlsCredentialedSigner cred;
			List<X509Certificate> chain = info.getRelatedCertificateChain();
			Certificate clientCert = convertCert(context.getCrypto(), chain);

			if (isRawRSA(info)) {
			    LOG.debug("Adding raw RSA signer.");
			    TlsSigner signer = new SmartCardSignerCredential(info);
			    cred = new DefaultTlsCredentialedSigner(tlsCrypto, signer, clientCert, null);
			    credentials.add(cred);
			}
		    } catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException | IOException ex) {
			LOG.error("Failed to read certificates from card. Skipping DID " + info.getDidName() + ".", ex);
		    } catch (UnsupportedAlgorithmException ex) {
			LOG.error("Unsupported algorithm used in CIF. Skipping DID " + info.getDidName() + ".", ex);
		    } catch (WSHelper.WSException ex) {
			LOG.error("Unknown error accessing DID " + info.getDidName() + ".", ex);
		    }
		}

	    } else {
		// TLS >= 1.2
		LOG.info("Looking for most specific DIDs.");

		// looping over the servers alg list preserves its ordering
		for (SignatureAndHashAlgorithm reqAlg : crSigAlgs) {
		    for (DidInfo info : infos) {
			LOG.debug("Checking DID={}.", info.getDidName());

			try {
			    AlgorithmInfoType algInfo = info.getGenericCryptoMarker().getAlgorithmInfo();
			    SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());

			    TlsCredentialedSigner cred;
			    List<X509Certificate> chain = info.getRelatedCertificateChain();
			    Certificate clientCert = convertCert(context.getCrypto(), chain);

			    // find one DID for this problem, then continue with the next algorithm
			    if (matchesAlg(reqAlg, alg) && (alg.getHashAlg() != null || isSafeForNoneDid(reqAlg))) {
				LOG.debug("Adding {} signer.", alg.getJcaAlg());
				TlsSigner signer = new SmartCardSignerCredential(info);
				cred = new DefaultTlsCredentialedSigner(tlsCrypto, signer, clientCert, reqAlg);
				credentials.add(cred);
				//break;
				return credentials;
			    }
			} catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException | IOException ex) {
			    LOG.error("Failed to read certificates from card. Skipping DID " + info.getDidName() + ".", ex);
			} catch (UnsupportedAlgorithmException ex) {
			    LOG.error("Unsupported algorithm used in CIF. Skipping DID " + info.getDidName() + ".", ex);
			} catch (WSHelper.WSException ex) {
			    LOG.error("Unknown error accessing DID " + info.getDidName() + ".", ex);
			}
		    }
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
	boolean isAuthCert = cert.getKeyUsage()[0];
	boolean isSigCert = cert.getKeyUsage()[1];
	if (! isAuthCert || isSigCert) {
	    LOG.debug("DID ({}): Certificate key usage does not permit authentication signatures.", info.getDidName());
	    return false;
	}

	return true;
    }


    private boolean matchesAlg(SignatureAndHashAlgorithm reqAlg, SignatureAlgorithms alg) {
	try {
	    SignatureAndHashAlgorithm bcAlg = convertSignatureAlgorithm(alg);

	    // RAW signature
	    if (bcAlg == null) {
		// filter out unmatching signature type
		if (alg.getKeyType() != convertSigType(reqAlg.getSignature())) {
		    return false;
		}

		// Only allow a certain set of Hash algs. Some hashes are too large for the cards.
		switch (reqAlg.getHash()) {
		    case HashAlgorithm.sha1:
		    case HashAlgorithm.sha224:
		    case HashAlgorithm.sha256:
		    case HashAlgorithm.sha384:
		    case HashAlgorithm.sha512:
			return true;
		    default:
			return false;
		}
	    } else {
		// match everything else
		return reqAlg.equals(bcAlg);
	    }
	} catch (IllegalArgumentException ex) {
	    return false;
	}
    }

    private boolean isRawRSA(DidInfo info) throws WSHelper.WSException, UnsupportedAlgorithmException {
	AlgorithmInfoType algInfo = info.getGenericCryptoMarker().getAlgorithmInfo();
	SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
	return SignatureAlgorithms.CKM_RSA_PKCS == alg;
    }

    @Nullable
    private static SignatureAndHashAlgorithm convertSignatureAlgorithm(SignatureAlgorithms alg) {
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
	    return null;
	}

	short sig;
	switch (keyType) {
	    case CKK_RSA: sig = SignatureAlgorithm.rsa; break;
	    case CKK_EC: sig = SignatureAlgorithm.ecdsa; break;
	    default: throw new IllegalArgumentException("Unsupported signature algorithm selected.");
	}

	return new SignatureAndHashAlgorithm(hash, sig);
    }

    @Nullable
    private KeyTypes convertSigType(short sigType) {
	switch (sigType) {
	    case SignatureAlgorithm.rsa: return KeyTypes.CKK_RSA;
	    case SignatureAlgorithm.ecdsa: return KeyTypes.CKK_EC;
	    default: return null;
	}
    }

    private Certificate convertCert(TlsCrypto crypto, List<X509Certificate> chain) throws IOException,
	    CertificateEncodingException {
	TlsCertificate cert = crypto.createCertificate(chain.get(0).getEncoded());
	return new Certificate(new TlsCertificate[]{ cert });
    }

    private List<DidInfo> nonRawFirst(List<DidInfo> infos) {
	ArrayList<DidInfo> result = new ArrayList<>();

	// first add all the non raw RSA DIDs
	for (DidInfo info : infos) {
	    try {
		if (! isRawRSA(info)) {
		    result.add(info);
		}
	    } catch (UnsupportedAlgorithmException | WSHelper.WSException ex) {
		LOG.error("Invalid DID or error accessing the DID.", ex);
	    }
	}
	// then add all raw RSA DIDs
	for (DidInfo info : infos) {
	    try {
		if (isRawRSA(info)) {
		    result.add(info);
		}
	    } catch (UnsupportedAlgorithmException | WSHelper.WSException ex) {
		LOG.error("Invalid DID or error accessing the DID.", ex);
	    }
	}

	return result;
    }

    private List<DidInfo> removeNonAuthDids(List<DidInfo> infos) {
	ArrayList<DidInfo> result = new ArrayList<>();

	for (DidInfo next : infos) {
	    try {
		List<X509Certificate> certs = next.getRelatedCertificateChain();
		if (! certs.isEmpty()) {
		    boolean isAuthCert = isAuthCert(next, certs);
		    if (isAuthCert) {
			result.add(next);
		    }
		}
	    } catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException ex) {
		LOG.error("Failed to read certificates from card. Skipping DID " + next.getDidName() + ".", ex);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Unknown error accessing DID " + next.getDidName() + ".", ex);
	    }
	}

	return result;
    }

    private ArrayList<SignatureAndHashAlgorithm> getCrSigAlgs(CertificateRequest cr) {
	ArrayList<SignatureAndHashAlgorithm> result = new ArrayList<>();
	if (cr.getSupportedSignatureAlgorithms() != null) {
	    for (Object next : cr.getSupportedSignatureAlgorithms()) {
		result.add((SignatureAndHashAlgorithm) next);
	    }
	}
	return result;
    }

    private List<DidInfo> removeUnsupportedCerts(CertificateRequest cr, List<DidInfo> infos) {
	ArrayList<DidInfo> result = new ArrayList<>();

	for (DidInfo next : infos) {
	    try {
		List<X509Certificate> chain = next.getRelatedCertificateChain();
		if (matchesCertReq(cr, chain)) {
		    result.add(next);
		}
	    } catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException ex) {
		LOG.error("Failed to read certificates from card. Skipping DID " + next.getDidName() + ".", ex);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Unknown error accessing DID " + next.getDidName() + ".", ex);
	    }
	}

	return result;
    }

    private List<DidInfo> removeSecretCertDids(List<DidInfo> infos) {
	ArrayList<DidInfo> result = new ArrayList<>();

	for (DidInfo next : infos) {
	    try {
		// filter out dids having secret certificates
		if (! (filterAlwaysReadable && isCertNeedsPin(next))) {
		    result.add(next);
		}
	    } catch (SecurityConditionUnsatisfiable ex) {
		LOG.error("Failed to get ACL for certificates of DID " + next.getDidName() + ".", ex);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Unknown error accessing DID " + next.getDidName() + ".", ex);
	    }
	}

	return result;
    }

    private void printCerts(List<DidInfo> infos) {
	for (DidInfo next : infos) {
	    try {
		List<X509Certificate> chain = next.getRelatedCertificateChain();

		if (LOG.isDebugEnabled()) {
		    for (X509Certificate cert : chain) {
			StringWriter out = new StringWriter();
			PemWriter pw = new PemWriter(out);
			pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
			pw.close();
			LOG.debug("Certificate for DID {}\n{}", next.getDidName(), out);
			LOG.debug("Certificate details\n{}", cert);
		    }
		}
	    } catch (SecurityConditionUnsatisfiable | NoSuchDid | CertificateException | IOException ex) {
		LOG.error("Failed to read certificates from card. Skipping DID " + next.getDidName() + ".", ex);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Unknown error accessing DID " + next.getDidName() + ".", ex);
	    }
	}
    }

    private List<DidInfo> removeUnsupportedAlgs(List<DidInfo> infos) {
	ArrayList<DidInfo> result = new ArrayList<>();

	for (DidInfo next : infos) {
	    try {
		AlgorithmInfoType algInfo = next.getGenericCryptoMarker().getAlgorithmInfo();
		String algStr = algInfo.getAlgorithmIdentifier().getAlgorithm();
		SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algStr);

		switch (alg) {
		    case CKM_ECDSA:
//		    case CKM_ECDSA_SHA1: // too weak
		    case CKM_ECDSA_SHA256:
		    case CKM_ECDSA_SHA384:
		    case CKM_ECDSA_SHA512:
		    case CKM_RSA_PKCS:
//		    case CKM_SHA1_RSA_PKCS: // too weak
		    case CKM_SHA256_RSA_PKCS:
		    case CKM_SHA384_RSA_PKCS:
		    case CKM_SHA512_RSA_PKCS:
			result.add(next);
		}
	    } catch (UnsupportedAlgorithmException ex) {
		LOG.error("Unsupported algorithm used in CIF. Skipping DID " + next.getDidName() + ".", ex);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Unknown error accessing DID " + next.getDidName() + ".", ex);
	    }
	}

	return result;
    }

    private void removeUnsupportedAlgs(ArrayList<SignatureAndHashAlgorithm> crSigAlgs) {
	Iterator<SignatureAndHashAlgorithm> it = crSigAlgs.iterator();
	while (it.hasNext()) {
	    SignatureAndHashAlgorithm alg = it.next();
	    switch (alg.getSignature()) {
		// allowed sig algs
		case SignatureAlgorithm.ecdsa:
		case SignatureAlgorithm.rsa:
		    break;
		default:
		    it.remove();
		    continue;
	    }
	    switch (alg.getHash()) {
		// allowed hash algs
		case HashAlgorithm.sha512:
		case HashAlgorithm.sha384:
		case HashAlgorithm.sha256:
//		case HashAlgorithm.sha1: // too weak
		    break;
		default:
		    it.remove();
		    continue;
	    }
	}
    }


    private boolean isSafeForNoneDid(SignatureAndHashAlgorithm reqAlg) {
	switch (reqAlg.getHash()) {
	    case HashAlgorithm.sha1:
	    case HashAlgorithm.sha224:
	    case HashAlgorithm.sha256:
	    case HashAlgorithm.sha384:
	    case HashAlgorithm.sha512:
		return true;
	    default:
		return false;
	}
    }

}
