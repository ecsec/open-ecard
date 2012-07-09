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

package org.openecard.client.sal.protocol.eac.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.openecard.bouncycastle.crypto.params.*;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.jce.spec.ElGamalParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.CADomainParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements an abstract key for chip authentication.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CAKey {

    private static final Logger logger = LoggerFactory.getLogger(CAKey.class);
    private AsymmetricKeyParameter sk;
    private AsymmetricKeyParameter pk;
    private CADomainParameter cdp;

    /**
     * Creates a new key for CA.
     *
     * @param cdp CADomainParameter
     */
    public CAKey(CADomainParameter cdp) {
	this.cdp = cdp;
    }

    /**
     * Decodes a public key from a byte array.
     *
     * @param data Encoded key
     * @return Decoded key
     * @throws Exception
     */
    public byte[] decodePublicKey(byte[] data) throws Exception {
	byte[] keyBytes;

	if (data[0] == (byte) 0x7C) {
	    keyBytes = TLV.fromBER(data).getChild().getValue();
	} else if (data[0] != 04) {
	    keyBytes = ByteUtils.concatenate((byte) 0x04, data);
	} else {
	    keyBytes = data;
	}

	if (cdp.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) cdp.getParameter();
	    ECDomainParameters ecp = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());

	    ECPoint q = p.getCurve().decodePoint(keyBytes);
	    pk = new ECPublicKeyParameters(q, ecp);

	    return getEncodedPublicKey();
	} else if (cdp.isDH()) {
	    logger.error("Not implemented yet.");
	    throw new UnsupportedOperationException("Not implemented yet.");
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Generate a key pair.
     */
    public void generateKeyPair() {
	if (cdp.isDH()) {
	    ElGamalParameterSpec p = (ElGamalParameterSpec) cdp.getParameter();
	    int numBits = p.getG().bitLength();
	    BigInteger d = new BigInteger(numBits, new SecureRandom());
	    ElGamalParameters egp = new ElGamalParameters(p.getP(), p.getG());

	    sk = new ElGamalPrivateKeyParameters(d, egp);
	    pk = new ElGamalPublicKeyParameters(egp.getG().multiply(d), egp);

	} else if (cdp.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) cdp.getParameter();
	    int numBits = p.getN().bitLength();
	    BigInteger d = new BigInteger(numBits, new SecureRandom());
	    ECDomainParameters ecp = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());

	    sk = new ECPrivateKeyParameters(d, ecp);
	    pk = new ECPublicKeyParameters(ecp.getG().multiply(d), ecp);
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Returns the public key.
     *
     * @return Public key
     */
    public AsymmetricKeyParameter getPublicKey() {
	return pk;
    }

    /**
     * Returns the byte encoded public key.
     *
     * @return Public key
     */
    public byte[] getEncodedPublicKey() {
	if (cdp.isDH()) {
	    return ((ElGamalPublicKeyParameters) pk).getY().toByteArray();
	} else if (cdp.isECDH()) {
	    return ((ECPublicKeyParameters) pk).getQ().getEncoded();
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Returns the byte encoded compressed public key.
     *
     * @return Public key
     */
    public byte[] getEncodedCompressedPublicKey() {
	if (cdp.isDH()) {
	    try {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] input = ((ElGamalPublicKeyParameters) pk).getY().toByteArray();
		byte[] compKey = md.digest(input);

		return compKey;
	    } catch (NoSuchAlgorithmException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		logger.error("Exception", ex);
		// </editor-fold>
		throw new RuntimeException(ex);
	    }
	} else if (cdp.isECDH()) {
	    byte[] compKey = ((ECPublicKeyParameters) pk).getQ().getX().toBigInteger().toByteArray();
	    return ByteUtils.cutLeadingNullByte(compKey);
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Returns the private key.
     *
     * @return Private key
     */
    public AsymmetricKeyParameter getPrivateKey() {
	return sk;
    }

    /**
     * Returns the byte encoded private key.
     *
     * @return Private key
     */
    public byte[] getEncodedPrivateKey() {
	if (cdp.isDH()) {
	    return ((ElGamalPrivateKeyParameters) sk).getX().toByteArray();
	} else if (cdp.isECDH()) {
	    return ((ECPrivateKeyParameters) sk).getD().toByteArray();
	} else {
	    throw new IllegalArgumentException();
	}
    }

}
