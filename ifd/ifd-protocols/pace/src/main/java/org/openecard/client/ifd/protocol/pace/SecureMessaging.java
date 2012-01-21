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
package org.openecard.client.ifd.protocol.pace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.openecard.bouncycastle.crypto.engines.AESFastEngine;
import org.openecard.bouncycastle.crypto.macs.CMac;
import org.openecard.bouncycastle.crypto.params.KeyParameter;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.utils.TLV;

/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Franziskus Kiefer
 */
public class SecureMessaging {

    // Secure Messaging header
    private final static byte[] HEADER = new byte[]{(byte) 0x8E, (byte) 0x08};
    // ISO/IEC 7816-4 padding tag
    private final static byte PAD = (byte) 0x80;
    private final static byte[] NULL = new byte[]{0x00};
    // Send Sequence Counter. See BSI-TR-03110 section F.3.
    private byte[] secureMessagingSSC;
    // Keys for encryption and message authentication.
    private byte[] keyMAC, keyENC;

    /**
     * Instantiates a new secure messaging.
     * 
     * @param keyMAC the key for message authentication
     * @param keyENC the key for encryption
     */
    public SecureMessaging(byte[] keyMAC, byte[] keyENC) {
        this.keyENC = keyENC;
        this.keyMAC = keyMAC;
        this.secureMessagingSSC = new byte[16];
    }

    /**
     * Encrypt the APDU.
     * 
     * @param apdu the APDU
     * @return the encrypted APDU
     * @throws Exception the exception
     */
    public byte[] encrypt(byte[] apdu) throws Exception {
        incrementSSC(secureMessagingSSC);
        byte[] commandAPDU = encrypt(apdu, secureMessagingSSC);
        incrementSSC(secureMessagingSSC);

        return commandAPDU;
    }

    /**
     * Encrypt the APDU.
     * 
     * @param apdu the APDU
     * @param secureMessagingSSC the Secure Messaging Send Sequence Counter
     * @return the encrypted APDU
     * @throws Exception the exception
     */
    private byte[] encrypt(byte[] apdu, byte[] secureMessagingSSC) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(apdu);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] header = new byte[4];
        byte[] data = new byte[0];
        int lc = -1, le = -1;

        /*
         * Parse APDU
         * Case 1: |CLA|INS|P1|P2|
         * Case 2: |CLA|INS|P1|P2|LE|
         * Case 3: |CLA|INS|P1|P2|LC|DATA|
         * Case 4: |CLA|INS|P1|P2|LC|DATA|LE|
         */
        if (bais.available() < 4) {
            throw new IllegalArgumentException("Invalid Secure Messaging APDU length");
        }
        // Read APDU header and indicate Secure Messaging
        bais.read(header);
        header[0] = (byte) ((byte) header[0] | (byte) 0x0C);

        final int length = bais.available();
        if (length == 1) {
            // Case 2
            le = bais.read();
        } else if (length > 1) {
            lc = bais.read();
            if (length == lc + 1) {
                // Case 3
                data = new byte[lc];
                bais.read(data);
            } else if (length == lc + 2) {
                // Case 4
                data = new byte[lc];
                bais.read(data);
                le = bais.read();
            } else {
                // Extended APDU Support!!!
                if (lc == 0) {
                    lc = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);
                    data = new byte[lc];
                    bais.read(data);
                    le = bais.read();
                }
            }
        }

        if (data.length > 0) {
            data = pad(data, 16);

            // Encrypt data
            Cipher c = getCipher(secureMessagingSSC, Cipher.ENCRYPT_MODE);
            byte[] data_encrypted = c.doFinal(data);

            // Add padding indicator 0x01
            data_encrypted = ByteUtils.concatenate((byte) 0x01, data_encrypted);

            baos.write(new TLV((byte) 0x87, data_encrypted).encode());
        }

        // Write protected LE
        if (le >= 0) {
            if (le == 0x100) {
                baos.write(new TLV((byte) 0x97, (byte) 0x00).encode());
            } else if (le > 0x100) {
                baos.write(new TLV((byte) 0x97, new byte[]{(byte) ((le >> 8) & 0xFF), (byte) (le & 0xFF)}).encode());
            } else {
                baos.write(new TLV((byte) 0x97, new byte[]{(byte) le}).encode());
            }
        }

        /*
         * Calculate MAC
         */
        byte[] mac = new byte[16];
        CMac cmac = getCMAC(secureMessagingSSC);

        byte[] paddedHeader = pad(header, 16);
        cmac.update(paddedHeader, 0, paddedHeader.length);

        if (baos.size() > 0) {
            byte[] paddedData = pad(baos.toByteArray(), 16);
            cmac.update(paddedData, 0, paddedData.length);

            lc = baos.size();
        }

        cmac.doFinal(mac, 0);
        mac = ByteUtils.copy(mac, 0, 8);

        /*
         * Build APDU
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Write header
        out.write(header);

        // Add MAC length to LC
        lc += 10;

        // Write LC field
        if ((lc > 0xFF) || (le > 0x100)) {
            out.write(NULL);
            out.write((lc >> 8) & 0xFF);
            out.write(lc & 0xFF);
        } else {
            out.write(lc & 0xFF);
        }

        // Write data if present
        if (baos.size() > 0) {
            out.write(baos.toByteArray());
        }
        // Write SM tag
        out.write(SecureMessaging.HEADER);

        // Write SM MAC
        out.write(mac);
        out.write(NULL);

        if ((lc > 0xFF) || (le > 0x100)) {
            out.write(NULL);
        }

        return out.toByteArray();
    }

    /**
     * Decrypt the APDU.
     * 
     * @param response the response
     * @return the byte[]
     * @throws Exception the exception
     */
    public byte[] decrypt(byte[] response) throws Exception {
        return decrypt(response, secureMessagingSSC);
    }

    /**
     * Decrypt the APDU.
     * 
     * @param response the response
     * @param secureMessagingSSC the secure messaging ssc
     * @return the byte[]
     * @throws Exception the exception
     */
    private byte[] decrypt(byte[] response, byte[] secureMessagingSSC) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(response);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(response.length - 10);

        // Status bytes of the response APDU. MUST be 2 bytes.
        byte[] statusBytes = new byte[2];
        // Padding-content indicator followed by cryptogram 0x87.
        byte[] dataObject = null;
        // Cryptographic checksum 0x8E. MUST be 8 bytes.
        byte[] macObject = new byte[8];

        /*
         * Read APDU structure
         * Case 1: DO99|DO8E|SW1SW2
         * Case 2: DO87|DO99|DO8E|SW1SW2
         * Case 3: DO99|DO8E|SW1SW2
         * Case 4: DO87|DO99|DO8E|SW1SW2
         */
        byte tag = (byte) bais.read();

        // Read data object
        if (tag == (byte) 0x87) {
            int size = bais.read();
            if (size > 0x80) {
                byte[] sizeBytes = new byte[size & 0x0F];
                bais.read(sizeBytes, 0, sizeBytes.length);
                size = new BigInteger(1, sizeBytes).intValue();
            }
            bais.skip(1); // Skip encryption header
            dataObject = new byte[size - 1];
            bais.read(dataObject, 0, dataObject.length);

            tag = (byte) bais.read();
        }

        // Read processing status
        if (tag == (byte) 0x99) {
            bais.skip(1); // Skip LE. MUST be 2 bytes
            bais.read(statusBytes, 0, 2);
        } else {
            throw new SecurityException("Malformed Secure Messaging APDU");
        }

        tag = (byte) bais.read();

        // Read MAC
        if (tag == (byte) 0x8E) {
            bais.skip(1); // Skip LE. MUST be 8 bytes
            bais.read(macObject, 0, 8);
        } else {
            throw new SecurityException("Malformed Secure Messaging APDU");
        }

        // Only 2 bytes status should be remain
        if (bais.available() != 2) {
            throw new SecurityException("Malformed Secure Messaging APDU");
        }

        // Calculate MAC for verification
        CMac cmac = getCMAC(secureMessagingSSC);
        byte[] mac = new byte[16];

        synchronized (cmac) {
            ByteArrayOutputStream macData = new ByteArrayOutputStream();

            // Write padding-content
            if (dataObject != null) {
                byte[] paddedDataObject = ByteUtils.concatenate((byte) 0x01, dataObject);
                macData.write(new TLV((byte) 0x87, paddedDataObject).encode());
            }
            // Write status bytes 
            macData.write(new byte[]{(byte) 0x99, (byte) 0x02});
            macData.write(statusBytes);
//			macData.write(new TLV((byte) 0x99, statusBytes).encode());

            byte[] paddedData = pad(macData.toByteArray(), 16);
            cmac.update(paddedData, 0, paddedData.length);

            cmac.doFinal(mac, 0);
            mac = ByteUtils.copy(mac, 0, 8);
        }

        // Verify MAC
        if (!ByteUtils.compare(mac, macObject)) {
            throw new SecurityException("Secure Messaging MAC verification failed");
        }

        // Decrypt data
        if (dataObject != null) {
            Cipher c = getCipher(secureMessagingSSC, Cipher.DECRYPT_MODE);
            byte[] data_decrypted = c.doFinal(dataObject);
            baos.write(unpad(data_decrypted));
        }

        // Add status code
        baos.write(statusBytes);

        return baos.toByteArray();
    }

    /**
     * Increment the Send Sequence Counter (SSC).
     * 
     * @param ssc the Send Sequence Counter (SSC)
     */
    public static void incrementSSC(byte[] ssc) {
        for (int i = ssc.length - 1; i > 0; i--) {
            ssc[i]++;
            if (ssc[i] != 0) {
                break;
            }
        }
    }

    /*
     * Cipher functions
     */
    /**
     * Gets the cipher for de/encryption.
     * 
     * @param smssc the Secure Messaging Send Sequence Counter
     * @param mode the mode indicating de/encryption
     * @return the cipher
     * @throws Exception the exception
     */
    private Cipher getCipher(byte[] smssc, int mode) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
        Key key = new SecretKeySpec(keyENC, "AES");
        byte[] iv = getCipherIV(smssc);
        AlgorithmParameterSpec algoPara = new IvParameterSpec(iv);

        c.init(mode, key, algoPara);

        return c;
    }

    /**
     * Gets the Initialization Vector (IV) for the cipher.
     * 
     * @param smssc the Secure Messaging Send Sequence Counter
     * @return the Initialization Vector
     * @throws Exception the exception
     */
    private byte[] getCipherIV(byte[] smssc) throws Exception {
        Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
        Key key = new SecretKeySpec(keyENC, "AES");

        c.init(Cipher.ENCRYPT_MODE, key);

        return c.doFinal(smssc);
    }

    /**
     * Gets the CMAC.
     * 
     * @param smssc Secure Messaging Send Sequence Counter
     * @return the CMAC
     */
    private CMac getCMAC(byte[] smssc) {
        CMac cmac = new CMac(new AESFastEngine());
        cmac.init(new KeyParameter(keyMAC));
        cmac.update(smssc, 0, smssc.length);

        return cmac;
    }

    /*
     * ISO/IEC 7816-4 padding functions
     */
    /**
     * Pad the data.
     * 
     * @param input the input
     * @param blockSize the block size
     * @return the padded data
     */
    private byte[] pad(byte[] input, int blockSize) {
        byte[] result = new byte[input.length + (blockSize - input.length % blockSize)];
        System.arraycopy(input, 0, result, 0, input.length);
        result[input.length] = PAD;

        return result;
    }

    /**
     * Unpad the data.
     * 
     * @param data the data
     * @return the unpadded data
     */
    private byte[] unpad(byte[] data) {
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] == PAD) {
                return ByteUtils.copy(data, 0, i);
            }
        }

        return data;
    }
}
