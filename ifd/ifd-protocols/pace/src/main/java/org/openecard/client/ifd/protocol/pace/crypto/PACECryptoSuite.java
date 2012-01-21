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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.crypto.engines.AESEngine;
import org.openecard.bouncycastle.crypto.macs.CMac;
import org.openecard.bouncycastle.crypto.params.KeyParameter;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.*;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.crypto.common.asn1.utils.TLV;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class PACECryptoSuite {

    private static final Logger logger = Logger.getLogger("crypto");
    private ECParameterSpec domainParameter;
    private PACESecurityInfos psi;
    // Keys for Generic Mapping
    private BigInteger SK_PCD;
    private BigInteger mapSK_PCD;
    private ECPoint mapPK_PCD;
    // Encrypted nonce
    public BigInteger s;
    // Certificate Authority Reference
    private byte[] currentCAR, previousCAR;

    /**
     * Create a new crypto suite for PACE.
     *
     * @param domainParameter PACE domain parameter
     */
    public PACECryptoSuite(PACESecurityInfos psi) {
        this.psi = psi;
        loadDomainParameters();
    }

    private void loadDomainParameters() {
        PACEInfo pi = psi.getPACEInfo();

        // If PACEInfo parameterID is present use standardized domain parameters
        if (pi.getParameterID() != -1) {
            int index = pi.getParameterID();
            domainParameter = new StandardizedDomainParameters(index).getDomainParameter();
        } // else load proprietary domain parameters from PACEDomainParameterInfo
        else {
            PACEDomainParameterInfo pdp = psi.getPACEDomainParameterInfo();
            domainParameter = new ProprietaryDomainParameters((ASN1Sequence) pdp.getDomainParameter().getParameters()).getDomainParameter();
        }

        if (domainParameter == null) {
            throw new IllegalArgumentException("Cannot load domain parameter");
        }
    }

    /**
     * Decrypt nonce
     *
     * @param keyData Key (Key_PI)
     * @param nonceData Nonce
     */
    public void decryptNonce(byte[] keyData, byte[] nonceData) {
        byte[] ret = new byte[16];
        byte[] nonce = ByteUtils.cut(nonceData, 0, 4);

        try {
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
            IvParameterSpec params = new IvParameterSpec(new byte[16]);
            c.init(Cipher.DECRYPT_MODE, skeySpec, params);
            c.doFinal(nonce, 0, nonce.length, ret);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception", e);
        }
        s = new BigInteger(1, ret);
    }

    /**
     * Returns the public ephemeral key for mapping.
     *
     * @return Public ephemeral key
     */
    public ECPoint getMapPK_PCD() {
        // Generate secret keys
        int numBits = domainParameter.getN().bitLength();
        mapSK_PCD = new BigInteger(numBits, new SecureRandom());
        SK_PCD = new BigInteger(numBits, new SecureRandom());

        // Generate public key
        mapPK_PCD = domainParameter.getG().multiply(mapSK_PCD);

        return mapPK_PCD;
    }

    /**
     * Creates a public key form a byte array.
     *
     * @param data Byte encoded public key
     * @return Public key
     */
    public ECPoint getPublicKey(byte[] data) {
        byte[] keyBytes = ByteUtils.cut(data, 0, 4);
        ECPoint publicKey = domainParameter.getCurve().decodePoint(keyBytes);

        return publicKey;
    }

    /**
     * Generic Mapping for PACE.
     *
     * @param mapPK_PICC Public Key
     * @return PK_PCD Public key
     */
    public ECPoint mapGenericMapping(ECPoint mapPK_PICC) {

        //TODO change calculation methode
        ECPoint H = mapPK_PICC.multiply(domainParameter.getH().multiply(mapSK_PCD));
        ECPoint newG = domainParameter.getG().multiply(s).add(H);
        ECPoint PK_PCD = newG.multiply(SK_PCD);

        domainParameter = new ECParameterSpec(domainParameter.getCurve(), newG, domainParameter.getN(), domainParameter.getH());

        return PK_PCD;
    }

    /**
     * Perform an Diffie-Hellman key agreement.
     *
     * @param PK_PICC Public key
     * @return Shared secret k
     */
    public byte[] getSharedSecret(ECPoint PK_PICC) {
        if (domainParameter.getH().intValue() == 1) {
            ECPoint k = PK_PICC.multiply(SK_PCD);
            byte[] key = k.getX().toBigInteger().toByteArray();

            return ByteUtils.cutLeadingNullBytes(key);
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    /**
     * Calculate the authentication token.
     *
     * @param keyMac Key for message authentication
     * @param key PACE session key (PCD or PICC)
     * @return Authentication token
     */
    public byte[] generateAuthenticationToken(byte[] keyMac, ECPoint key) {
        byte[] authToken = new byte[8];
        byte[] ret = new byte[16];

        try {
            CMac cMAC = new CMac(new AESEngine());
            byte[] macData = getMACObject(key);
            cMAC.init(new KeyParameter(keyMac));
            cMAC.update(macData, 0, macData.length);
            cMAC.doFinal(ret, 0);

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception", e);
        }

        System.arraycopy(ret, 0, authToken, 0, 8);
        return authToken;
    }

    /**
     * Calculates the data for the authentication token.
     *
     * @param key Key
     * @return data object for token
     */
    private byte[] getMACObject(ECPoint key) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            baos.write(ObjectIdentifierUtils.toByteArray(psi.getPACEInfo().getProtocol()));
            baos.write(TLV.encode((byte) 0x86, ByteUtils.cutLeadingNullBytes(key.getEncoded())));
            baos.close();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception", e);
        }

        TLV macObject = new TLV(new byte[]{0x7F, 0x49}, baos.toByteArray());
        return macObject.encode();
    }

    /**
     * Verify the authentication token by the PICC and extract Certificate Authority Reference (CAR). See TR-03110 2.05 Appendix B.1
     *
     * @param T_PICC Token from the PICC
     * @param myT_PICC Self-calculate token
     * @return true if T_PICC == myT_PICC
     */
    public boolean verifyAuthenticationToken(byte[] T_PICC, byte[] myT_PICC) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(T_PICC);

        byte tag = (byte) bais.read();
        byte size = (byte) bais.read();

        // Verify tag and size
        if (tag == (byte) 0x7C && (size & 0xFF) == bais.available()) {
            tag = (byte) bais.read();
            size = (byte) bais.read();
        } else {
            throw new SecurityException("Malformed authentication token");
        }

        // Verify authentication token T_PICC
        if (tag == (byte) 0x86 && (size & 0xFF) == 8) {
            byte[] buf = new byte[8];
            bais.read(buf, 0, 8);
            if (!ByteUtils.compare(buf, myT_PICC)) {
                throw new SecurityException("Cannot verify authentication token");
            }
            // Read next bytes
            tag = (byte) bais.read();
            size = (byte) bais.read();
        } else {
            throw new SecurityException("Malformed authentication token");
        }

        // Read current CAR
        if (tag == (byte) 0x87 && size == (byte) 0x0E) {
            currentCAR = new byte[size];
            bais.read(currentCAR, 0, size);

            // Read next bytes
            tag = (byte) bais.read();
            size = (byte) bais.read();
        } else {
            throw new SecurityException("Malformed authentication token");
        }

        // Read optional previous CAR
        if (bais.available() > 0) {
            if (tag == (byte) 0x88 && size == (byte) 0x0E) {
                previousCAR = new byte[size];
                bais.read(previousCAR, 0, size);
            } else {
                throw new SecurityException("Malformed authentication token");
            }
        }

        return true;
    }

    /**
     * Returns the current Certificate Authority Reference (CAR). See TR-03110 2.05 Appendix B.1
     *
     * @return current CAR
     */
    public byte[] getCurrentCAR() {
        return currentCAR;
    }

    /**
     * Returns the previous Certificate Authority Reference (CAR). See TR-03110 2.05 Appendix B.1
     *
     * @return previous CAR
     */
    public byte[] getPreviousCAR() {
        return previousCAR;
    }
}
