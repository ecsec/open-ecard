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
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.apdu.GeneralAuthenticate;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.apdu.exception.APDUException;
import org.openecard.client.common.ifd.protocol.exception.ProtocolException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.ifd.protocol.pace.apdu.MSESetATPACE;
import org.openecard.client.ifd.protocol.pace.crypto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEImplementation {

    private static final Logger logger = LoggerFactory.getLogger(PACEImplementation.class);
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
	logger.trace(LoggingConstants.ENTER, "mseSetAT");
	// </editor-fold>

	byte[] oid = ObjectIdentifierUtils.getValue(psi.getPACEInfo().getProtocol());
	CardCommandAPDU mseSetAT = new MSESetATPACE(oid, chat, passwordType);

	try {
	    response = mseSetAT.transmit(dispatcher, slotHandle);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    logger.trace(LoggingConstants.EXIT, "mseSetAT");
	    // </editor-fold>
	    // Continue with step 2
	    generalAuthenticateEncryptedNonce();
	} catch (APDUException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    int sw = e.getResponseAPDU().getSW();

	    if (sw == PACEConstants.PASSWORD_DEACTIVATED) {
		// Password is deactivated
		throw new ProtocolException(ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED);
	    } else if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
		retryCounter = (byte) (sw & (short) 0x000F);
		if (retryCounter == (byte) 0x00) {
		    // The password is blocked
		    logger.warn("The password is blocked. The password MUST be unblocked.");
		    if (passwordType == PACEConstants.PASSWORD_PUK) {
			generalAuthenticateEncryptedNonce();
		    } else {
			throw new ProtocolException(
				ECardConstants.Minor.IFD.PASSWORD_BLOCKED,
				"The password is blocked. The password MUST be unblocked.");
		    }
		} else if (retryCounter == (byte) 0x01) {
		    // The password is suspended
		    logger.warn("The password is suspended. The password MUST be resumed.");
		    if (passwordType == PACEConstants.PASSWORD_CAN) {
			generalAuthenticateEncryptedNonce();
		    } else {
			throw new ProtocolException(
				ECardConstants.Minor.IFD.PASSWORD_SUSPENDED,
				"The password is suspended. The password MUST be resumed.");
		    }
		} else if (retryCounter == (byte) 0x02) {
		    // The password is suspended
		    logger.warn("The password is wrong.");
		    generalAuthenticateEncryptedNonce();
		}
	    }
	} catch (ProtocolException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw e;
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(ECardConstants.Minor.IFD.UNKNOWN_ERROR, e.getMessage());
	}
    }

    /**
     * Step 2: Encrypted nonce
     */
    private void generalAuthenticateEncryptedNonce() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.ENTER, "generalAuthenticateEncryptedNonce");
	// </editor-fold>

	CardCommandAPDU gaEncryptedNonce = new GeneralAuthenticate();
	gaEncryptedNonce.setChaining();

	// Derive key PI
	byte[] keyPI = kdf.derivePI(password);

	try {
	    response = gaEncryptedNonce.transmit(dispatcher, slotHandle);
	    s = cryptoSuite.decryptNonce(keyPI, response.getData());
	    
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    logger.trace(LoggingConstants.EXIT, "generalAuthenticateEncryptedNonce");
	    // </editor-fold>

	    // Continue with Step 3
	    generalAuthenticateMapNonce();
	} catch (APDUException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 3: Mapping nonce
     */
    private void generalAuthenticateMapNonce() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.ENTER, "generalAuthenticateMapNonce");
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
	} catch (APDUException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	}

	if (mapping instanceof PACEGenericMapping) {
	    PACEGenericMapping gm = (PACEGenericMapping) mapping;
	    PACEKey keyMapPICC = new PACEKey(domainParameter);
	    keyMapPICC.decodePublicKey(response.getData());
	    byte[] pkMapPICC = keyMapPICC.getEncodedPublicKey();

	    if (ByteUtils.compare(pkMapPICC, pkMapPCD)) {
		throw new GeneralSecurityException("PACE security violation: equal keys");
	    }

	    domainParameter = gm.map(pkMapPICC, s);

	} else if (mapping instanceof PACEIntegratedMapping) {
	    throw new UnsupportedOperationException("Not implemented yet.");
	}

	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.EXIT, "generalAuthenticateMapNonce");
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
	logger.trace(LoggingConstants.ENTER, "generalAuthenticateKeyAgreement");
	// </editor-fold>

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
		logger.trace(LoggingConstants.EXIT, "generalAuthenticateKeyAgreement");
		// </editor-fold>
		// Continue with Step 5
		generalAuthenticateMutualAuthentication();
	    } else {
		throw new GeneralSecurityException("PACE security violation: equal keys");
	    }
	} catch (APDUException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 5: Mutual authentication
     */
    private void generalAuthenticateMutualAuthentication() throws Exception {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.ENTER, "generalAuthenticateMutualAuthentication");
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

		// <editor-fold defaultstate="collapsed" desc="log trace">
		logger.trace(LoggingConstants.EXIT, "generalAuthenticateMutualAuthentication");
		// </editor-fold>
	    } else {
		throw new GeneralSecurityException("Cannot verify authentication token.");
	    }
	} catch (APDUException e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    int sw = e.getResponseAPDU().getSW();

	    if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
		retryCounter = (byte) (sw & (short) 0x000F);
		if (retryCounter == (byte) 0x00) {
		    // The password is blocked.
		    logger.warn("The password is blocked. The password MUST be unblocked.");
		    throw new ProtocolException(
			    ECardConstants.Minor.IFD.PASSWORD_BLOCKED,
			    "The password is blocked. The password MUST be unblocked.");
		} else if (retryCounter == (byte) 0x01) {
		    // The password is suspended.
		    logger.warn("The password is suspended. The password MUST be resumed.");
		    throw new ProtocolException(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED,
			    "The password is suspended. The password MUST be resumed.");
		} else if (retryCounter == (byte) 0x02) {
		    // The password is wrong.
		    logger.warn("The password is wrong.");
		    throw new ProtocolException(
			    ECardConstants.Minor.IFD.PASSWORD_ERROR,
			    "The password is wrong.");
		}
	    } else {
		throw new ProtocolException(
			ECardConstants.Minor.IFD.AUTHENTICATION_FAILED, "Authentication failed.");
	    }
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new ProtocolException(ECardConstants.Minor.IFD.UNKNOWN_ERROR, e.getMessage());
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
     * Returns the card identifier ID_PICC = Comp(PK_PICC).
     *
     * @return ID_PICC
     */
    public byte[] getIDPICC() {
	return ByteUtils.cutLeadingNullByte(keyPICC.getEncodedCompressedPublicKey());
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
