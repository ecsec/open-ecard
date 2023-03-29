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
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.asn1.ASN1Encoding;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.DERNull;
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.asn1.x509.DigestInfo;
import org.openecard.bouncycastle.tls.HashAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.tls.TlsUtils;
import org.openecard.bouncycastle.tls.crypto.TlsSigner;
import org.openecard.bouncycastle.tls.crypto.TlsStreamSigner;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
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
public class SmartCardSignerCredential implements TlsSigner {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardSignerCredential.class);

    private final DidInfo did;


    public SmartCardSignerCredential(@Nonnull DidInfo info) {
	this.did = info;
    }


    @Override
    public byte[] generateRawSignature(SignatureAndHashAlgorithm algorithm, byte[] hash) throws IOException {
	return genSig(algorithm, hash);
    }

    private byte[] genSig(SignatureAndHashAlgorithm algorithm, byte[] sigData)
	    throws IOException {
	SignatureAlgorithms didAlg = getDidAlgorithm();
	LOG.debug("Using DID with algorithm={}.", didAlg.getJcaAlg());

	if (algorithm != null) {
	    String reqAlgStr = String.format("%s-%s", SignatureAlgorithm.getText(algorithm.getSignature()),
		    HashAlgorithm.getText(algorithm.getHash()));
	    LOG.debug("Performing TLS 1.2 signature for algorithm={}.", reqAlgStr);

	    if (isRawSignature(didAlg)) {
		if (algorithm.getSignature() == SignatureAlgorithm.rsa) {
		    // TLS >= 1.2 needs a PKCS#1 v1.5 signature and no raw RSA signature
		    ASN1ObjectIdentifier hashAlgId = TlsUtils.getOIDForHashAlgorithm(algorithm.getHash());
		    DigestInfo digestInfo = new DigestInfo(new AlgorithmIdentifier(hashAlgId, DERNull.INSTANCE), sigData);
		    sigData = digestInfo.getEncoded(ASN1Encoding.DER);
		    LOG.debug("Signing DigestInfo with algorithm={}.", hashAlgId);
		} else if (SignatureAlgorithm.isRSAPSS(algorithm.getSignature())) {
		    throw new UnsupportedOperationException("Not supported yet.");
		} else if (algorithm.getSignature() == SignatureAlgorithm.ecdsa) {
		    throw new UnsupportedOperationException("Not supported yet.");
		} else{
		    throw new UnsupportedOperationException("Not supported yet.");
		}
	    }
	} else {
	    LOG.debug("Performing pre-TLS 1.2 signature.");
	}

	try {
	    did.authenticateMissing();
	    LOG.debug("Raw Signature of data={}.", ByteUtils.toHexString(sigData));
	    byte[] signature = did.sign(sigData);
	    LOG.debug("Raw Signature={}.", ByteUtils.toHexString(signature));
	    return signature;
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

    private static boolean isRawSignature(SignatureAlgorithms alg) {
	return isRawRSA(alg) || isRawECDSA(alg);
    }

    private static boolean isRawRSA(SignatureAlgorithms alg) {
	return alg == SignatureAlgorithms.CKM_RSA_PKCS;
    }
    private static boolean isRawECDSA(SignatureAlgorithms alg) {
	return alg == SignatureAlgorithms.CKM_ECDSA;
    }


    public SignatureAlgorithms getDidAlgorithm() {
	try {
	    AlgorithmInfoType algInfo = did.getGenericCryptoMarker().getAlgorithmInfo();
	    SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
	    return alg;
	} catch (UnsupportedAlgorithmException | WSHelper.WSException ex) {
	    throw new RuntimeException("Error evaluating algorithm", ex);
	}
    }

    private boolean supportsExternalHashing() throws WSHelper.WSException {
	CryptoMarkerType cryptoMarker = did.getGenericCryptoMarker();
	return cryptoMarker.getHashGenerationInfo() == HashGenerationInfoType.NOT_ON_CARD;
    }

    @Override
    public TlsStreamSigner getStreamSigner(final SignatureAndHashAlgorithm algorithm) throws IOException {
	try {
	    if (algorithm != null && supportsExternalHashing()) {
		String digestName = HashAlgorithm.getName(algorithm.getHash());
		MessageDigest md = MessageDigest.getInstance(digestName);
		OutputStream os = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			    md.update((byte) b);
			}
		    };
		// create stream signer for use with real data, not the hash of it
		return new TlsStreamSigner() {
		    @Override
		    public OutputStream getOutputStream() throws IOException {
			return os;
		    }

		    @Override
		    public byte[] getSignature() throws IOException {
			return genSig(algorithm, md.digest());
		    }
		};
	    } else {
		return null;
	    }
	} catch (NoSuchAlgorithmException | WSHelper.WSException ex) {
	    throw new IOException("Failed to create stream signer.", ex);
	}
    }

}
