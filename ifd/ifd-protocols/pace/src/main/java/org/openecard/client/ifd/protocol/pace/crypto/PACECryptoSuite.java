/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.ifd.protocol.pace.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;
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
	byte[] nonce = ByteUtils.copy(nonceData, 4, nonceData.length - 4);
	try {
	    Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
	    SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
	    IvParameterSpec params = new IvParameterSpec(new byte[16]);
	    c.init(Cipher.DECRYPT_MODE, skeySpec, params);
	    c.doFinal(nonce, 0, nonce.length, ret);
	} catch (Throwable e) {
	    logger.error("Exception", e);
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
