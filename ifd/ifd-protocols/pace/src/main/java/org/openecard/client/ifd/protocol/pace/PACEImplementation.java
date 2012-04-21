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

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.apdu.GeneralAuthenticate;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.ifd.protocol.exception.ProtocolException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.ifd.protocol.pace.apdu.MSESetATPACE;
import org.openecard.client.ifd.protocol.pace.crypto.*;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEImplementation {

    private static final Logger logger = LogManager.getLogger(PACEImplementation.class.getName());

    // Communication
    private Dispatcher dispatcher;
    private KDF kdf;
    private byte[] slotHandle;
    private CardResponseAPDU response;
    // Crypto
    private PACEDomainParameter domainParameter;
    private PACESecurityInfos psi;
    private PACECryptoSuite cryptoSuite;
    // Keys
    private PACEKey keyPCD, keyPICC;
    private byte[] keyMAC, keyENC;
    private byte[] password, s;
    // Certificate Authority Reference (CAR)
    private byte[] currentCAR, previousCAR;
    private byte retryCounter = 3;

    /**
     * Creates a new instance of the pace protocol.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     * @param paceSecurityInfos PACESecurityInfos
     * @throws Exception Exception
     */
    public PACEImplementation(Dispatcher dispatcher, byte[] slotHandle, PACESecurityInfos paceSecurityInfos) throws Exception {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
	this.psi = paceSecurityInfos;

	domainParameter = new PACEDomainParameter(psi);
	cryptoSuite = new PACECryptoSuite(psi, domainParameter);
	kdf = new KDF();
    }

    /**
     * Start PACE.
     *
     * @param password Password
     * @param chat CHAT
     * @param passwordType Password type (PIN, PUK, CAN, MRZ)
     * @throws Exception Exception
     */
    public void execute(byte[] password, byte passwordType, byte[] chat) throws Exception {
	this.password = password;

	mseSetAT(passwordType, chat);
    }

    /**
     * Step 1: Initialise PACE
     */
    private void mseSetAT(byte passwordType, byte[] chat) throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "mseSetAT");
	}
	// </editor-fold>

	byte[] oid = ObjectIdentifierUtils.getValue(psi.getPACEInfo().getProtocol());
	CardCommandAPDU mseSetAT = new MSESetATPACE(oid, chat, passwordType);

	try {
	    response = mseSetAT.transmit(dispatcher, slotHandle);

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

	    if (sw == PACEConstants.PASSWORD_DEACTIVATED) {
		// Password is deactivated
		throw new ProtocolException("The password is deactivated.");
	    } else if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
		retryCounter = (byte) (sw & (short) 0x000F);
		if (retryCounter == (byte) 0x00) {
		    // The password is blocked.
		    logger.log(Level.WARNING, "The password is blocked. The password MUST be unblocked.");
		    // GeneralAuthenticateEncryptedNonce();
		} else if (retryCounter == (byte) 0x01) {
		    // The password is suspended.
		    logger.log(Level.WARNING, "The password is suspended. The password MUST be resumed.");
		    //FIXME test me1
//                    if (passwordType != (byte) 0x03) {
//                        GeneralAuthenticateEncryptedNonce();
//                    }
		} else if (retryCounter == (byte) 0x02) {
		    // The password is suspended.
		    logger.log(Level.WARNING, "The password is wrong.");
		    generalAuthenticateEncryptedNonce();
		}
	    }
	}
    }

    /**
     * Step 2: Encrypted nonce
     */
    private void generalAuthenticateEncryptedNonce() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "generalAuthenticateEncryptedNonce");
	}
	// </editor-fold>

	CardCommandAPDU gaEncryptedNonce = new GeneralAuthenticate();
	gaEncryptedNonce.setChaining();

	// Derive key PI
	byte[] keyPI = kdf.derivePI(password);

	try {
	    response = gaEncryptedNonce.transmit(dispatcher, slotHandle);
	    s = cryptoSuite.decryptNonce(keyPI, response.getData());
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (logger.isLoggable(Level.FINER)) {
		logger.exiting(this.getClass().getName(), "generalAuthenticateEncryptedNonce");
	    }
	    // </editor-fold>

	    // Continue with Step 3
	    generalAuthenticateMapNonce();
	} catch (WSException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.logp(Level.SEVERE, this.getClass().getName(), "generalAuthenticateEncryptedNonce", e.getMessage(), e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.logp(Level.SEVERE, this.getClass().getName(), "generalAuthenticateEncryptedNonce", e.getMessage(), e);
	    // </editor-fold>
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 3: Mapping nonce
     */
    private void generalAuthenticateMapNonce() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "generalAuthenticateMapNonce");
	}
	// </editor-fold>

	byte[] pkMapPCD = null;
	PACEMapping mapping = cryptoSuite.getMapping();

	if (mapping instanceof PACEGenericMapping) {
	    PACEGenericMapping gm = (PACEGenericMapping) mapping;
	    pkMapPCD = gm.getMappingKey().getEncodedPublicKey();

	} else if (mapping instanceof PACEIntegratedMapping) {
	    throw new UnsupportedOperationException("Not implemented yet.");
	}

	CardCommandAPDU gaMapNonce = new GeneralAuthenticate((byte) 0x81, pkMapPCD);
	gaMapNonce.setChaining();

	try {
	    response = gaMapNonce.transmit(dispatcher, slotHandle);
	} catch (WSException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.logp(Level.SEVERE, this.getClass().getName(), "generalAuthenticateMapNonce", e.getMessage(), e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	}

	if (mapping instanceof PACEGenericMapping) {
	    PACEGenericMapping gm = (PACEGenericMapping) mapping;
	    PACEKey keyMapPICC = new PACEKey(domainParameter);
	    keyMapPICC.decodePublicKey(response.getData());
	    byte[] pkMapPICC = keyMapPICC.getEncodedPublicKey();

	    if (ByteUtils.compare(pkMapPICC, pkMapPCD)) {
		throw new ProtocolException("PACE security violation: equal keys");
	    }

	    domainParameter = gm.map(pkMapPICC, s);

	} else if (mapping instanceof PACEIntegratedMapping) {
	    throw new UnsupportedOperationException("Not implemented yet.");
	}

	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.exiting(this.getClass().getName(), "generalAuthenticateMapNonce");
	}
	// </editor-fold>

	// Continue with Step 4
	generalAuthenticateKeyAgreement();
    }

    /**
     * Step 4: Key agreement
     *
     * @param mapPK_PICC
     */
    private void generalAuthenticateKeyAgreement() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "generalAuthenticateKeyAgreement");
	}
	// </editor-fold>

	// genera key !!
	keyPCD = new PACEKey(domainParameter);
	keyPCD.generateKeyPair();

	byte[] keyPKPCD = keyPCD.getEncodedPublicKey();

	CardCommandAPDU gaKeyAgreement = new GeneralAuthenticate((byte) 0x83, keyPKPCD);
	gaKeyAgreement.setChaining();

	try {
	    response = gaKeyAgreement.transmit(dispatcher, slotHandle);
	    keyPICC = new PACEKey(domainParameter);
	    byte[] keyPKPICC = keyPICC.decodePublicKey(response.getData());

	    if (!ByteUtils.compare(keyPKPCD, keyPKPICC)) {
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (logger.isLoggable(Level.FINER)) {
		    logger.exiting(this.getClass().getName(), "generalAuthenticateKeyAgreement");
		}
		// </editor-fold>
		// Continue with Step 5
		generalAuthenticateMutualAuthentication();
	    } else {
		throw new GeneralSecurityException("PACE security violation: equal keys");
	    }
	} catch (WSException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.logp(Level.SEVERE, this.getClass().getName(), "generalAuthenticateKeyAgreement", e.getMessage(), e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.logp(Level.SEVERE, this.getClass().getName(), "generalAuthenticateKeyAgreement", e.getMessage(), e);
	    // </editor-fold>
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 5: Mutual authentication
     */
    private void generalAuthenticateMutualAuthentication() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "generalAuthenticateMutualAuthentication");
	}
	// </editor-fold>

	// Calculate shared key k
	byte[] k = cryptoSuite.generateSharedSecret(keyPCD.getEncodedPrivateKey(), keyPICC.getEncodedPublicKey());
	// Derive key MAC
	keyMAC = kdf.deriveMAC(k);
	// Derive key ENC
	keyENC = kdf.deriveENC(k);

	// Calculate token T_PCD
	AuthenticationToken tokenPCD = new AuthenticationToken(psi);
	tokenPCD.generateToken(keyMAC, keyPICC.getEncodedPublicKey());

	CardCommandAPDU gaMutualAuth = new GeneralAuthenticate((byte) 0x85, tokenPCD.toByteArray());

	// Calculate token T_PICC
	AuthenticationToken tokenPICC = new AuthenticationToken(psi);
	tokenPICC.generateToken(keyMAC, keyPCD.getEncodedPublicKey());


	try {
	    response = gaMutualAuth.transmit(dispatcher, slotHandle);

	    if (tokenPICC.verifyToken(response.getData())) {

		currentCAR = tokenPICC.getCurrentCAR();
		previousCAR = tokenPCD.getPreviousCAR();

		logger.log(Level.FINER, "Authentication successful");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (logger.isLoggable(Level.FINER)) {
		    logger.exiting(this.getClass().getName(), "generalAuthenticateMutualAuthentication");
		}
		// </editor-fold>
	    } else {
		throw new GeneralSecurityException("Cannot verify authentication token.");
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
	} catch (Throwable e) {
	    throw new ProtocolException("Authentication failed");
	}
    }

    /**
     * Returns the current Certification Authority Reference (CAR).
     *
     * @return Current Certification Authority Reference (CAR)
     */
    public byte[] getCurrentCAR() {
	return currentCAR;
    }

    /**
     * Returns the previous Certification Authority Reference (CAR).
     *
     * @return Previous Certification Authority Reference (CAR)
     */
    public byte[] getPreviousCAR() {
	return previousCAR;
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
	return ByteUtils.cutLeadingNullByte(keyPICC.getEncodedPublicKey());
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
