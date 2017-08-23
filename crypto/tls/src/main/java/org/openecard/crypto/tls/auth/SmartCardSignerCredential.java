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
import java.io.IOException;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.asn1.ASN1Encoding;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.asn1.x509.DigestInfo;
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.tls.TlsUtils;
import org.openecard.bouncycastle.tls.crypto.TlsSigner;
import org.openecard.bouncycastle.tls.crypto.TlsStreamSigner;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
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
	if (algorithm != null && isRawSignature()) {
	    // TLS >= 1.2 needs a PKCS#1 v1.5 signature and no raw RSA signature
	    ASN1ObjectIdentifier hashAlgId = TlsUtils.getOIDForHashAlgorithm(algorithm.getHash());
	    DigestInfo digestInfo = new DigestInfo(new AlgorithmIdentifier(hashAlgId), hash);
	    hash = digestInfo.getEncoded(ASN1Encoding.DER);
	    LOG.debug("Signing DigestInfo with algorithm={}.", hashAlgId);
	}

	LOG.debug("Signing hash={}.", ByteUtils.toHexString(hash));
	try {
	    did.authenticateMissing();
	    byte[] sigData = did.sign(hash);
	    return sigData;
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

    private boolean isRawSignature() {
	try {
	    AlgorithmInfoType algInfo = did.getGenericCryptoMarker().getAlgorithmInfo();
	    SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algInfo.getAlgorithmIdentifier().getAlgorithm());
	    return alg == SignatureAlgorithms.CKM_RSA_PKCS;
	} catch (UnsupportedAlgorithmException | WSHelper.WSException ex) {
	    throw new RuntimeException("Error evaluating algorithm", ex);
	}
    }

    @Override
    public TlsStreamSigner getStreamSigner(SignatureAndHashAlgorithm algorithm) throws IOException {
	return null;
    }

}
