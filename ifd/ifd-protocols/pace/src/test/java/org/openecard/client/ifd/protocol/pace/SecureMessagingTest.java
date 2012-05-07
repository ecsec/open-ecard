/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd.protocol.pace;

import org.junit.Assert;
import org.junit.Test;
import org.openecard.client.common.util.StringUtils;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class SecureMessagingTest {

    @Test
    public void testEncryptionAndDecryption() throws Exception {
        
        /*
         * setup Secure Messaging, values are taken from "Worked Example for
         * Extended Access Control (EAC), v1.02"
         */
        byte[] keyEnc = StringUtils.toByteArray("68 40 6B 41 62 10 05 63 D9 C9 01 A6 15 4D 29 01", true);
        byte[] keyMac = StringUtils.toByteArray("73 FF 26 87 84 F7 2A F8 33 FD C9 46 40 49 AF C9", true);
        SecureMessaging sm = new SecureMessaging(keyMac, keyEnc);

        /*
         * test encryption
         */
        byte[] plainAPDU = StringUtils.toByteArray("00 22 81 B6 0F 83 0D 44 45 43 56 43 41 41 54 30 30 30 30 31", true);
        byte[] encryptedAPDU = sm.encrypt(plainAPDU);
        byte[] expectedEncryptedAPDU = StringUtils.toByteArray("0C2281B61D871101BE90237EEB4BA0FF253EA246AE31C8B88E0892D21C73A1DFE99900");
        Assert.assertArrayEquals(expectedEncryptedAPDU, encryptedAPDU);

        /*
         * test decryption
         */
        byte[] apduToDecrypt = StringUtils.toByteArray("99 02 90 00 8E 08 A8 95 70 A6 86 64 A7 D6 90 00", true);
        byte[] decryptedAPDU = sm.decrypt(apduToDecrypt);
        byte[] expectedDecryptedAPDU = new byte[] { (byte) 0x90, 0x00 };
        Assert.assertArrayEquals(expectedDecryptedAPDU, decryptedAPDU);

        /*
         * test already encrypted apdu
         */
        try {
            sm.encrypt(expectedEncryptedAPDU);
            Assert.fail("Encrypting an already encrypted APDU should give an error.");
        } catch (Exception e) {
            /* expected */
        }

        /*
         * test already decrypted apdu
         */
        try {
            sm.encrypt(expectedEncryptedAPDU);
            Assert.fail("Decrypting an already decrypted APDU should give an error.");
        } catch (Exception e) {
            /* expected */
        }
    }
}
