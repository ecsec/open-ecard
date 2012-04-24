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

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements a Key Derivation Function (KDF).
 * See BSI-TR-03110, version 2.10, part 3, section A.2.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class KDF {

    private MessageDigest md;
    private int keyLength;

    /**
     * Create a new Key Derivation Function.
     *
     * @throws GeneralSecurityException
     */
    public KDF() throws GeneralSecurityException {
        try {
            md = MessageDigest.getInstance("SHA1");
            keyLength = 16;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KDF.class.getName()).log(Level.SEVERE, "Exception", ex);
            throw new GeneralSecurityException(ex);
        }
    }

    /**
     * Key Derivation Function.
     *
     * @param md MessageDigest
     * @param keyLength Key length
     */
    public KDF(MessageDigest md, int keyLength) {
        this.md = md;
        this.keyLength = keyLength;
    }

    /**
     * Derive key for encryption.
     *
     * @param secret Secret
     * @return Key for message en/decryption (Key_PI)
     */
    public byte[] derivePI(byte[] secret) {
        return derive(secret, (byte) 3, null);
    }

    /**
     * Derive key for message authentication.
     *
     * @param secret Secret
     * @return Key for message authentication (Key_MAC)
     */
    public byte[] deriveMAC(byte[] secret) {
        return derive(secret, (byte) 2, null);
    }

    /**
     * Derive key for message authentication.
     *
     * @param secret Secret
     * @param nonce Nonce
     * @return Key for message authentication (Key_MAC)
     */
    public byte[] deriveMAC(byte[] secret, byte[] nonce) {
        return derive(secret, (byte) 2, nonce);
    }

    /**
     * Derive key for message encryption.
     *
     * @param secret Secret
     * @return Key for message encryption (Key_ENC)
     */
    public byte[] deriveENC(byte[] secret) {
        return derive(secret, (byte) 1, null);
    }

    /**
     * Derive key for message encryption.
     *
     * @param secret Secret
     * @param nonce Nonce
     * @return Key for message encryption (Key_ENC)
     */
    public byte[] deriveENC(byte[] secret, byte[] nonce) {
        return derive(secret, (byte) 1, nonce);
    }

    private byte[] derive(byte[] secret, byte counter, byte[] nonce) {
        byte[] c = {(byte) 0x00, (byte) 0x00, (byte) 0x00, counter};
        byte[] key = new byte[keyLength];

        md.reset();
        md.update(secret, 0, secret.length);
        if (nonce != null) {
            md.update(nonce, 0, nonce.length);
        }
        md.update(c, 0, c.length);

        byte[] hash = md.digest();

        System.arraycopy(hash, 0, key, 0, key.length);

        return key;
    }

}
