/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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
import java.security.cert.X509Certificate;
import java.util.List;
import org.openecard.bouncycastle.asn1.ASN1Encoding;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.asn1.x509.DigestInfo;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsUtils;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class RawRsaSmartCardSignerCredential extends SmartCardSignerCredential {
    
    private static final Logger LOG = LoggerFactory.getLogger(RawRsaSmartCardSignerCredential.class);

    public RawRsaSmartCardSignerCredential(DidInfo info, List<X509Certificate> chain,
	    SignatureAndHashAlgorithm sigAlg) {
	super(info, chain, sigAlg);
    }

    @Override
    public byte[] generateCertificateSignature(byte[] hash) throws IOException {
	SignatureAndHashAlgorithm sigAlg = getSignatureAndHashAlgorithm();
	if (sigAlg.getHash() != HashAlgorithm.none) {
	    // TLS >= 1.2 needs a PKCS#1 v1.5 signature and no raw RSA signature
	    ASN1ObjectIdentifier hashAlgId = TlsUtils.getOIDForHashAlgorithm(sigAlg.getHash());
	    DigestInfo digestInfo = new DigestInfo(new AlgorithmIdentifier(hashAlgId), hash);
	    hash = digestInfo.getEncoded(ASN1Encoding.DER);
	    LOG.debug("Signing DigestInfo with algorithm={}.", hashAlgId);
	}

	return super.generateCertificateSignature(hash);
    }

}
