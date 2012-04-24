/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACECryptoSuite {

    private static final Logger logger = LoggerFactory.getLogger(PACECryptoSuite.class.getName());
    private PACESecurityInfos psi;
    private PACEDomainParameter domainParameter;

    /**
     * Create a new crypto suite for PACE.
     *
     * @param psi PACESecurityInfos
     * @param pdp PACEDomainParameter
     * @throws GeneralSecurityException
     */
    public PACECryptoSuite(PACESecurityInfos psi, PACEDomainParameter pdp) throws GeneralSecurityException {
	this.psi = psi;
	this.domainParameter = pdp;
    }

    /**
     * Decrypt nonce.
     *
     * @param keyData Key (Key_PI)
     * @param nonceData Nonce
     * @return Decrypted nonce
     * @throws GeneralSecurityException
     */
    public byte[] decryptNonce(byte[] keyData, byte[] nonceData) throws GeneralSecurityException {
	byte[] ret = new byte[16];
	byte[] nonce = ByteUtils.cut(nonceData, 4, nonceData.length - 4);
	try {
	    Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
	    SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
	    IvParameterSpec params = new IvParameterSpec(new byte[16]);
	    c.init(Cipher.DECRYPT_MODE, skeySpec, params);
	    c.doFinal(nonce, 0, nonce.length, ret);
	} catch (Throwable e) {
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    throw new GeneralSecurityException(e);
	}
	return ret;
    }

    /**
     * Perform an Diffie-Hellman key agreement.
     *
     * @param sk Secret key
     * @param pk Public key
     * @return Shared secret k
     */
    public byte[] generateSharedSecret(byte[] sk, byte[] pk) {
	if (domainParameter.isDH()) {
	    throw new UnsupportedOperationException("Not implemented yet!");
	} else if (domainParameter.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) domainParameter.getParameter();
	    if (p.getH().intValue() == 1) {
		BigInteger d = new BigInteger(1, sk);
		ECPoint q = p.getCurve().decodePoint(pk);
		ECPoint k = q.multiply(d);

		return ByteUtils.cutLeadingNullBytes(k.getX().toBigInteger().toByteArray());
	    } else {
		throw new UnsupportedOperationException("Not implemented yet!");
	    }
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Return the mapping algorithm for PACE.
     *
     * @return PACE mapping
     */
    public PACEMapping getMapping() {
	if (psi.getPACEInfo().isGM()) {
	    return new PACEGenericMapping(domainParameter);
	} else if (psi.getPACEInfo().isIM()) {
	    return new PACEIntegratedMapping(domainParameter);
	} else {
	    throw new IllegalArgumentException();
	}
    }
}
