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

import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;
import org.openecard.bouncycastle.math.ec.ECPoint;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.ifd.protocol.exception.ProtocolException;
import org.openecard.client.ifd.protocol.pace.crypto.KDF;
import org.openecard.client.ifd.protocol.pace.crypto.PACECryptoSuite;
import org.openecard.ws.IFD;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class PACEImplementation {

    private static final Logger logger = Logger.getLogger("PACE");
    private PACESecurityInfos psi;
    private PACECryptoSuite cSuite;
    private KDF kdf;
    private byte[] secret, keyMAC, keyENC;
    private ECPoint keyPKPICC;
    private IFD ifd;
    private byte[] slotHandle;
    private byte retryCounter;
    private ResponseAPDU response;

    /**
     *
     * @param connection
     * @param paceSecurityInfos
     */
    public PACEImplementation(IFD ifd, byte[] slotHandle, PACESecurityInfos paceSecurityInfos) {
        this.ifd = ifd;
        this.slotHandle = slotHandle;
        this.psi = paceSecurityInfos;

        cSuite = new PACECryptoSuite(psi);
        kdf = new KDF();
    }

    /**
     * Start PACE.
     *
     * @param password Password
     * @param chat CHAT
     * @param passwordType Password type (PIN, PUK, CAN, MRZ)
     */
    public void execute(byte[] password, byte[] chat, byte passwordType) throws ProtocolException {
        this.secret = password;
        mseSetAT(chat, passwordType);
    }

    /**
     * Step 1: Initialize PACE
     */
    private void mseSetAT(byte[] chat, byte passwordType) throws ProtocolException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "mseSetAT");
        }
        // </editor-fold>

        byte[] apdu = NPACardCommands.mseSetAT(psi, chat, passwordType);

        try {
            response = transmit(apdu);
            // <editor-fold defaultstate="collapsed" desc="log trace">
            if (logger.isLoggable(Level.FINER)) {
                logger.exiting(this.getClass().getName(), "mseSetAT");
            }
            // </editor-fold>
            // Continue with Step 2
            generalAuthenticateEncryptedNonce();
        } catch (WSException ex) {
            //TODO ckeck
            int sw = response.getSW();

            if (sw == APDUConstants.PASSWORD_DEACTIVATED) {
                // Password is deactivated
                throw new ProtocolException("Password is deactivated");
            } else if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
                retryCounter = (byte) (sw & (short) 0x000F);
                if (retryCounter == (byte) 0x00) {
                    // The password is blocked.
                    logger.log(Level.WARNING, "Password is blocked");
                    // GeneralAuthenticateEncryptedNonce();
                } else if (retryCounter == (byte) 0x01) {
                    // The password is suspended.
                    logger.log(Level.WARNING, "Password is suspended");
                    // GeneralAuthenticateEncryptedNonce();
                } else if (retryCounter == (byte) 0x02) {
                    // The password is suspended.
                    logger.log(Level.WARNING, "Password is wrong");
                    generalAuthenticateEncryptedNonce();
                }
            }
        }
    }

    /**
     * Step 2: Encrypted nonce
     */
    private void generalAuthenticateEncryptedNonce() throws ProtocolException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "generalAuthenticateEncryptedNonce");
        }
        // </editor-fold>

        byte[] gaEncryptedNonce = NPACardCommands.generalAuthenticate();
        // Derive key PI
        byte[] keyPI = kdf.derivePI(secret);

        try {
            response = transmit(gaEncryptedNonce);
            cSuite.decryptNonce(keyPI, response.getData());
            // <editor-fold defaultstate="collapsed" desc="log trace">
            if (logger.isLoggable(Level.FINER)) {
                logger.exiting(this.getClass().getName(), "generalAuthenticateEncryptedNonce");
            }
            // </editor-fold>
            // Continue with Step 3
            generalAuthenticateMapNonce();
        } catch (WSException ex) {
            //TODO
        }
    }

    /**
     * Step 3: Mapping nonce
     */
    private void generalAuthenticateMapNonce() throws ProtocolException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "generalAuthenticateMapNonce");
        }
        // </editor-fold>

        ECPoint keyMapPKPCD = cSuite.getMapPK_PCD();
        byte[] gaMapNonce = NPACardCommands.generalAuthenticate((byte) 0x81, keyMapPKPCD.getEncoded());

        try {
            response = transmit(gaMapNonce);
            ECPoint keyMapPKPICC = cSuite.getPublicKey(response.getData());
            if (!keyMapPKPCD.equals(keyMapPKPICC)) {
                // <editor-fold defaultstate="collapsed" desc="log trace">
                if (logger.isLoggable(Level.FINER)) {
                    logger.exiting(this.getClass().getName(), "generalAuthenticateMapNonce");
                }
                // </editor-fold>
                // Continue with Step 4
                generalAuthenticateKeyAgreement(keyMapPKPICC);
            } else {
                throw new SecurityException("PACE security violation: equal keys");
            }
        } catch (WSException ex) {
            //TODO
        }
    }

    /**
     * Step 4: Key agreement
     *
     * @param mapPK_PICC
     */
    private void generalAuthenticateKeyAgreement(ECPoint keyMapPKPICC) throws ProtocolException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "generalAuthenticateKeyAgreement");
        }
        // </editor-fold>

        ECPoint keyPKPCD = cSuite.mapGenericMapping(keyMapPKPICC);
        byte[] gaKeyAgreement = NPACardCommands.generalAuthenticate((byte) 0x83, keyPKPCD.getEncoded());

        try {
            response = transmit(gaKeyAgreement);
            keyPKPICC = cSuite.getPublicKey(response.getData());
            if (!keyPKPCD.equals(keyPKPICC)) {
                // <editor-fold defaultstate="collapsed" desc="log trace">
                if (logger.isLoggable(Level.FINER)) {
                    logger.exiting(this.getClass().getName(), "generalAuthenticateKeyAgreement");
                }
                // </editor-fold>
                // Continue with Step 5
                generalAuthenticateMutualAuthentication(keyPKPCD);
            } else {
                throw new SecurityException("PACE security violation: equal keys");
            }
        } catch (WSException ex) {
            //TODO
        }
    }

    /**
     * Step 5: Mutual authentication
     */
    private void generalAuthenticateMutualAuthentication(ECPoint keyPKPCD) throws ProtocolException {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "generalAuthenticateMutualAuthentication");
        }
        // </editor-fold>
        // Calculate shared key k
        byte[] k = cSuite.getSharedSecret(keyPKPICC);
        // Derive key MAC
        keyMAC = kdf.deriveMAC(k);
        // Derive key ENC
        keyENC = kdf.deriveENC(k);
        // Calculate token T_PCD
        byte[] tPCD = cSuite.generateAuthenticationToken(keyMAC, keyPKPICC);
        byte[] gaMutualAuth = NPACardCommands.generalAuthenticate((byte) 0x00, (byte) 0x85, tPCD);
        // Calculate token T_PICC
        byte[] tPICC = cSuite.generateAuthenticationToken(keyMAC, keyPKPCD);

        try {
            response = transmit(gaMutualAuth);
            if (cSuite.verifyAuthenticationToken(response.getData(), tPICC)) {
                logger.log(Level.FINER, "Authentication successful");
                // <editor-fold defaultstate="collapsed" desc="log trace">
                if (logger.isLoggable(Level.FINER)) {
                    logger.exiting(this.getClass().getName(), "generalAuthenticateMutualAuthentication");
                }
                // </editor-fold>
            } else {
                throw new ProtocolException("Authentication failed");
            }
        } catch (WSException ex) {
            //TODO
            int sw = response.getSW();
            if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
                retryCounter = (byte) (sw & (short) 0x000F);
                if (retryCounter == (byte) 0x00) {
                    // The password is blocked.
                    throw new ProtocolException("Password is blocked");
                } else if (retryCounter == (byte) 0x01) {
                    // The password is suspended.
                    throw new ProtocolException("Password is suspended");
                } else if (retryCounter == (byte) 0x02) {
                    // The password is wrong.
                    throw new ProtocolException("Password is wrong");
                }
            } else {
                throw new ProtocolException("Authentication failed");
            }
        } catch (Exception e) {
            throw new ProtocolException("Authentication failed");
        }
    }

    private ResponseAPDU transmit(byte[] apdu) throws WSException {
        //FIXME
        ArrayList responses = new ArrayList<byte[]>() {

            {
                add(new byte[]{(byte) 0x90, (byte) 0x00});
            }
        };

        Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
        TransmitResponse tr = ifd.transmit(t);

        return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }

    /**
     * Returns the current Certification Authority Reference (CAR).
     *
     * @return Current Certification Authority Reference (CAR)
     */
    public byte[] getCurrentCAR() {
        return cSuite.getCurrentCAR();
    }

    /**
     * Returns the previous Certification Authority Reference (CAR).
     *
     * @return Previous Certification Authority Reference (CAR)
     */
    public byte[] getPreviousCAR() {
        return cSuite.getPreviousCAR();
    }

    /**
     * Returns the key for message authentication.
     *
     * @return KeyMAC
     */
    public byte[] getKeyMAC() {
        return keyMAC;
    }

    /**
     * Returns the key for message encryption.
     *
     * @return KeyENC
     */
    public byte[] getKeyENC() {
        return keyENC;
    }

    /**
     * Returns the card identifier ID_PICC = Comp(PK_PICC). Returns only the x-coordinate of the ECPoint!!!
     *
     * @return ID_PICC
     */
    public byte[] getIDPICC() {
        return ByteUtils.cutLeadingNullByte(keyPKPICC.getX().toBigInteger().toByteArray());
    }

    /**
     * Returns the retry counter.
     *
     * @return Retry counter
     */
    public byte getRetryCounter() {
        return retryCounter;
    }
}
