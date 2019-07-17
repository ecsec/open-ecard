/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ElGamalParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.SecureRandomFactory;
import org.openecard.crypto.common.asn1.eac.CADomainParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements an abstract key for chip authentication.
 *
 * @author Moritz Horsch
 */
public final class CAKey {

    private static final Logger LOG = LoggerFactory.getLogger(CAKey.class);
    private static final SecureRandom RAND = SecureRandomFactory.create(32);
    private static long counter = 0;

    private static void reseed() {
	counter++;
	RAND.setSeed(counter);
	RAND.setSeed(System.nanoTime());
    }

    private AsymmetricKeyParameter sk;
    private AsymmetricKeyParameter pk;
    private final CADomainParameter cdp;

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
     * @throws TLVException
     * @throws IllegalArgumentException
     */
    public byte[] decodePublicKey(byte[] data) throws TLVException {
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
	    //TODO
	    LOG.error("Not implemented yet.");
	    throw new UnsupportedOperationException("Not implemented yet.");
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Generate a key pair.
     */
    public void generateKeyPair() {
	reseed();
	if (cdp.isDH()) {
	    ElGamalParameterSpec p = (ElGamalParameterSpec) cdp.getParameter();
	    int numBits = p.getG().bitLength();
	    BigInteger d = new BigInteger(numBits, RAND);
	    ElGamalParameters egp = new ElGamalParameters(p.getP(), p.getG());

	    sk = new ElGamalPrivateKeyParameters(d, egp);
	    pk = new ElGamalPublicKeyParameters(egp.getG().multiply(d), egp);

	} else if (cdp.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) cdp.getParameter();
	    int numBits = p.getN().bitLength();
	    BigInteger d = new BigInteger(numBits, RAND);
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
	    return ((ECPublicKeyParameters) pk).getQ().getEncoded(false);
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
	    } catch (NoSuchAlgorithmException e) {
		LOG.error(e.getMessage(), e);
		throw new RuntimeException(e);
	    }
	} else if (cdp.isECDH()) {
	    byte[] compKey = ((ECPublicKeyParameters) pk).getQ().getAffineXCoord().toBigInteger().toByteArray();
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
