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

package org.openecard.ifd.protocol.pace.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.openecard.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.openecard.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.openecard.bouncycastle.crypto.params.*;
import org.openecard.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.jce.spec.ElGamalParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;
import org.openecard.common.tlv.TLV;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.SecureRandomFactory;
import org.openecard.crypto.common.asn1.eac.PACEDomainParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 */
public final class PACEKey {

    private static final Logger logger;
    private static final SecureRandom rand;
    private static long counter;

    static {
	logger = LoggerFactory.getLogger(PACEKey.class);
	rand = SecureRandomFactory.create(32);
	counter = 0;
    }

    private static void reseed() {
	counter++;
	rand.setSeed(counter);
	rand.setSeed(System.nanoTime());
    }

    private AsymmetricKeyParameter sk;
    private AsymmetricKeyParameter pk;
    private final PACEDomainParameter pdp;

    /**
     * Creates a new key for PACE.
     *
     * @param pdp PACEDomainParameter
     */
    public PACEKey(PACEDomainParameter pdp) {
	this.pdp = pdp;
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

	if (pdp.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) pdp.getParameter();
	    ECDomainParameters ecp = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());

	    ECPoint q = p.getCurve().decodePoint(keyBytes);
	    pk = new ECPublicKeyParameters(q, ecp);

	    return getEncodedPublicKey();
	} else if (pdp.isDH()) {
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
	reseed();
	if (pdp.isDH()) {
	    ElGamalParameterSpec p = (ElGamalParameterSpec) pdp.getParameter();
	    int numBits = p.getG().bitLength();
	    BigInteger d = new BigInteger(numBits, rand);
	    ElGamalParameters egp = new ElGamalParameters(p.getP(), p.getG());

	    sk = new ElGamalPrivateKeyParameters(d, egp);
	    pk = new ElGamalPublicKeyParameters(egp.getG().multiply(d), egp);

	} else if (pdp.isECDH()) {
		var gen = new ECKeyPairGenerator();

		ECDomainParameters domainParams;
		var p = (ECParameterSpec) pdp.getParameter();
		if (p instanceof ECNamedCurveParameterSpec) {
			var pn = (ECNamedCurveParameterSpec) p;
			var curveOid = ECNamedCurveTable.getOID(pn.getName());
			domainParams = new ECNamedDomainParameters(curveOid, p.getCurve(), p.getG(), p.getN(), p.getH(), p.getSeed());
		} else {
			domainParams = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH(), p.getSeed());
		}

		var genParams = new ECKeyGenerationParameters(domainParams, rand);
		gen.init(genParams);
		var keyPair = gen.generateKeyPair();

	    sk = keyPair.getPrivate();
	    pk = keyPair.getPublic();
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
	if (pdp.isDH()) {
	    return ((ElGamalPublicKeyParameters) pk).getY().toByteArray();
	} else if (pdp.isECDH()) {
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
	if (pdp.isDH()) {
	    try {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] input = ((ElGamalPublicKeyParameters) pk).getY().toByteArray();
		byte[] compKey = md.digest(input);

		return compKey;
	    } catch (NoSuchAlgorithmException ex) {
		logger.error(ex.getMessage(), ex);
		throw new RuntimeException(ex);
	    }
	} else if (pdp.isECDH()) {
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
	if (pdp.isDH()) {
	    return ((ElGamalPrivateKeyParameters) sk).getX().toByteArray();
	} else if (pdp.isECDH()) {
	    return ((ECPrivateKeyParameters) sk).getD().toByteArray();
	} else {
	    throw new IllegalArgumentException();
	}
    }

}
