/****************************************************************************
 * Copyright (C) 2012-2020 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace;

import java.security.GeneralSecurityException;
import java.util.List;
import org.openecard.common.ECardConstants;
import org.openecard.common.apdu.GeneralAuthenticate;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.ifd.PacePinStatus;
import org.openecard.common.ifd.protocol.exception.ProtocolException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.asn1.eac.PACEDomainParameter;
import org.openecard.crypto.common.asn1.eac.PACESecurityInfoPair;
import org.openecard.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.ifd.protocol.pace.apdu.MSESetATPACE;
import org.openecard.ifd.protocol.pace.crypto.AuthenticationToken;
import org.openecard.ifd.protocol.pace.crypto.KDF;
import org.openecard.ifd.protocol.pace.crypto.PACECryptoSuite;
import org.openecard.ifd.protocol.pace.crypto.PACEGenericMapping;
import org.openecard.ifd.protocol.pace.crypto.PACEIntegratedMapping;
import org.openecard.ifd.protocol.pace.crypto.PACEKey;
import org.openecard.ifd.protocol.pace.crypto.PACEMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class PACEImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(PACEImplementation.class);
    // Communication
    private final Dispatcher dispatcher;
    private final KDF kdf;
    private final byte[] slotHandle;
    // Crypto
    private final PACESecurityInfoPair psip;
    private final PACECryptoSuite cryptoSuite;
    private PACEDomainParameter domainParameter;
    // Keys
    private PACEKey keyPCD, keyPICC;
    private byte[] keyMAC, keyENC;
    private byte[] password, s;
    // Certification Authority Reference (CAR)
    private byte[] currentCAR, previousCAR;
    private byte retryCounter = 3;
    // true if PACE is used with a CHAT
    private boolean specifiedCHAT;

    /**
     * Creates a new instance of the pace protocol.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param paceSecurityInfos PACESecurityInfos
     * @throws Exception Exception
     */
    public PACEImplementation(Dispatcher dispatcher, byte[] slotHandle, PACESecurityInfos paceSecurityInfos)
	    throws Exception {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
	List<PACESecurityInfoPair> paceInfoPairs = paceSecurityInfos.getPACEInfoPairs(
		PACEConstants.SUPPORTED_PACE_PROTOCOLS,
		PACEConstants.SUPPORTED_PACE_DOMAIN_PARAMS);
	if (paceInfoPairs.isEmpty()) {
	    String msg = "No supported PACE keys found on the card.";
	    LOG.error(msg);
	    throw new ProtocolException(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, msg);
	}
	this.psip = paceInfoPairs.get(0);

	domainParameter = new PACEDomainParameter(this.psip);
	cryptoSuite = new PACECryptoSuite(this.psip.getPACEInfo(), domainParameter);
	kdf = new KDF(this.psip.getPACEInfo().getKdfLength());
    }

    /**
     * Start PACE.
     *
     * @param password Password
     * @param passwordID Password type (PIN, PUK, CAN, MRZ)
     * @param chat CHAT
     * @throws Exception Exception
     */
    public void execute(byte[] password, byte passwordID, byte[] chat) throws Exception {
	this.password = password;
	specifiedCHAT = (chat != null);

	mseSetAT(passwordID, chat);
    }

    /**
     * Initialize Chip Authentication. Sends an MSE:Set AT APDU. (S
     * Step 1: Initialise PACE.
     * See BSI-TR-03110, version 2.10, part 3, B.11.1.
     */
    private void mseSetAT(byte passwordID, byte[] chat) throws Exception {
	byte[] oID = ObjectIdentifierUtils.getValue(psip.getPACEInfo().getProtocol());
	CardCommandAPDU mseSetAT = new MSESetATPACE(oID, passwordID, psip.getPACEInfo().getParameterID(), chat);

	try {
	    mseSetAT.transmit(dispatcher, slotHandle);
	    // Continue with step 2
	    generalAuthenticateEncryptedNonce();
	} catch (APDUException e) {
	    if (e.getResponseAPDU() == null) {
		if (e.getCause() instanceof Exception) {
		    throw (Exception) e.getCause();
		} else {
		    throw new ProtocolException(e.getResultMinor(), e.getMessage());
		}
	    }

	    LOG.error(e.getMessage(), e);
	    short sw = e.getResponseAPDU().getSW();

	    if (sw == PACEConstants.PASSWORD_DEACTIVATED) {
		// Password is deactivated
		throw new ProtocolException(ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED);
	    } else if ((sw & (short) 0xFFF0) == (short) 0x63C0) {
		retryCounter = (byte) (sw & (short) 0x000F);
		if (retryCounter == (byte) 0x00) {
		    // The password is blocked
		    LOG.warn("The password is blocked. The password MUST be unblocked.");
		    if (passwordID == PACEConstants.PASSWORD_PUK) {
			generalAuthenticateEncryptedNonce();
		    } else {
			throw new ProtocolException(ECardConstants.Minor.IFD.PASSWORD_BLOCKED,
				"The password is blocked. The password MUST be unblocked.");
		    }
		} else if (retryCounter == (byte) 0x01) {
		    // The password is suspended
		    LOG.warn("The password is suspended. The password MUST be resumed.");
		    //TODO check for an existing SM-Channel with the CAN
		    // if (mseSetAT.isSecureMessaging()) {
		    generalAuthenticateEncryptedNonce();
		    /*} else {
			throw new ProtocolException(
				ECardConstants.Minor.IFD.PASSWORD_SUSPENDED,
				"The password is suspended. The password MUST be resumed.");
		    }*/
		} else if (retryCounter == (byte) 0x02) {
		    // The password is suspended
		    LOG.warn("The password is wrong.");
		    generalAuthenticateEncryptedNonce();
		}
	    }
	} catch (ProtocolException e) {
	    LOG.error(e.getMessage(), e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throw new ProtocolException(ECardConstants.Minor.IFD.UNKNOWN_ERROR, e.getMessage());
	}
    }

    /**
     * Step 2: Encrypted nonce
     */
    private void generalAuthenticateEncryptedNonce() throws Exception {
	CardCommandAPDU gaEncryptedNonce = new GeneralAuthenticate();
	gaEncryptedNonce.setChaining();

	// Derive key PI
	byte[] keyPI = kdf.derivePI(password);

	try {
	    var response = gaEncryptedNonce.transmit(dispatcher, slotHandle);
	    s = cryptoSuite.decryptNonce(keyPI, response.getData());
	    // Continue with Step 3
	    generalAuthenticateMapNonce();
	} catch (APDUException e) {
	    LOG.error(e.getMessage(), e);
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    LOG.error(e.getMessage(), e);
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 3: Mapping nonce
     */
    private void generalAuthenticateMapNonce() throws Exception {
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

	CardResponseAPDU response;
	try {
	    response = gaMapNonce.transmit(dispatcher, slotHandle);
	} catch (APDUException e) {
	    LOG.error(e.getMessage(), e);
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

	// Continue with Step 4
	generalAuthenticateKeyAgreement();
    }

    /**
     * Step 4: Key agreement
     */
    private void generalAuthenticateKeyAgreement() throws Exception {
	keyPCD = new PACEKey(domainParameter);
	keyPCD.generateKeyPair();

	byte[] keyPKPCD = keyPCD.getEncodedPublicKey();

	CardCommandAPDU gaKeyAgreement = new GeneralAuthenticate((byte) 0x83, keyPKPCD);
	gaKeyAgreement.setChaining();

	try {
	    var response = gaKeyAgreement.transmit(dispatcher, slotHandle);
	    keyPICC = new PACEKey(domainParameter);
	    byte[] keyPKPICC = keyPICC.decodePublicKey(response.getData());

	    if (!ByteUtils.compare(keyPKPCD, keyPKPICC)) {
		// Continue with Step 5
		generalAuthenticateMutualAuthentication();
	    } else {
		throw new GeneralSecurityException("PACE security violation: equal keys");
	    }
	} catch (APDUException e) {
	    LOG.error(e.getMessage(), e);
	    throw new ProtocolException(e.getResult());
	} catch (GeneralSecurityException e) {
	    LOG.error(e.getMessage(), e);
	    throw new ProtocolException(e.getMessage());
	}
    }

    /**
     * Step 5: Mutual authentication
     */
    private void generalAuthenticateMutualAuthentication() throws Exception {
	// Calculate shared key k
	byte[] k = cryptoSuite.generateSharedSecret(keyPCD.getEncodedPrivateKey(), keyPICC.getEncodedPublicKey());
	// Derive key MAC
	keyMAC = kdf.deriveMAC(k);
	// Derive key ENC
	keyENC = kdf.deriveENC(k);

	// Calculate token T_PCD
	AuthenticationToken tokenPCD = new AuthenticationToken(psip.getPACEInfo());
	tokenPCD.generateToken(keyMAC, keyPICC.getEncodedPublicKey());

	CardCommandAPDU gaMutualAuth = new GeneralAuthenticate((byte) 0x85, tokenPCD.toByteArray());

	// Calculate token T_PICC
	AuthenticationToken tokenPICC = new AuthenticationToken(psip.getPACEInfo());
	tokenPICC.generateToken(keyMAC, keyPCD.getEncodedPublicKey());

	try {
	    var response = gaMutualAuth.transmit(dispatcher, slotHandle);

	    if (tokenPICC.verifyToken(response.getData(), specifiedCHAT)) {
		currentCAR = tokenPICC.getCurrentCAR();
		previousCAR = tokenPICC.getPreviousCAR();
	    } else {
		throw new GeneralSecurityException("Cannot verify authentication token.");
	    }
	} catch (APDUException e) {
	    if (e.getResponseAPDU() == null) {
		if (e.getCause() instanceof Exception) {
		    throw (Exception) e.getCause();
		} else {
		    throw new ProtocolException(ECardConstants.Minor.IFD.UNKNOWN_ERROR, e.getMessage());
		}
	    }

	    LOG.error(e.getMessage(), e);
	    switch (PacePinStatus.fromCode(e.getResponseAPDU().getStatusBytes())) {
		case RC2:
		    // The password is wrong.
		    LOG.warn("The password is wrong.");
		    throw new ProtocolException(
			    ECardConstants.Minor.IFD.PASSWORD_ERROR,
			    "The password is wrong.");
		case RC1:
		    // The password is suspended.
		    LOG.warn("The password is suspended. The password MUST be resumed.");
		    throw new ProtocolException(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED,
			    "The password is suspended. The password MUST be resumed.");
		case BLOCKED:
		    // The password is blocked.
		    LOG.warn("The password is blocked. The password MUST be unblocked.");
		    throw new ProtocolException(
			    ECardConstants.Minor.IFD.PASSWORD_BLOCKED,
			    "The password is blocked. The password MUST be unblocked.");
		case DEACTIVATED:
		    // The password is dactivated.
		    LOG.warn("The password is deactivated. The password MUST be activated at the citizen bureau.");
		    throw new ProtocolException(
			    ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED,
			    "The password is deactivated. The password MUST be activated at the citizen bureau.");
		default:
		String msg = String.format("PACE Mutual Authentication failed (SW=0x%04X).", e.getResponseAPDU().getSW());
		LOG.warn(msg);
		throw new ProtocolException(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED, msg);
	    }

	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
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
	return keyPICC.getEncodedCompressedPublicKey();
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
