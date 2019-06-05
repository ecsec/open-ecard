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

package org.openecard.ifd.protocol.pace.crypto;

import java.security.GeneralSecurityException;
import java.util.List;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.TagClass;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.asn1.eac.PACEInfo;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See BSI-TR-03110, version 2.10, part 3, section B.1.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class AuthenticationToken {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationToken.class.getName());

    private final PACEInfo pi;
    // Byte encoded token
    private final byte[] token = new byte[8];
    // Certificate Authority Reference (CAR)
    private byte[] currentCAR, previousCAR;

    /**
     * Creates a new AuthenticationToken.
     *
     * @param pi PACEInfo
     */
    public AuthenticationToken(PACEInfo pi) {
	this.pi = pi;
    }

    /**
     * Generate an authentication token.
     *
     * @param keyMac Key for message authentication
     * @param key Key
     * @throws GeneralSecurityException
     */
    public void generateToken(byte[] keyMac, byte[] key) throws GeneralSecurityException {
	byte[] tmp = new byte[16];
	byte[] macData = getMACObject(key);

	CMac cMAC = new CMac(new AESEngine());
	cMAC.init(new KeyParameter(keyMac));
	cMAC.update(macData, 0, macData.length);
	cMAC.doFinal(tmp, 0);

	System.arraycopy(tmp, 0, token, 0, 8);
    }

    /**
     * Verify the authentication token by the PICC and extract Certificate Authority Reference (CAR).
     *
     * @param token Token
     * @param specifiedCHAT true if PACE is used with a CHAT
     * @return true if token is equal to my token
     * @throws GeneralSecurityException
     */
    public boolean verifyToken(AuthenticationToken token, boolean specifiedCHAT) throws GeneralSecurityException {
	return verifyToken(token.toByteArray(), specifiedCHAT);
    }

    /**
     * Verify the authentication token by the PICC and extract Certificate Authority Reference (CAR).
     *
     * @param T_PICC Token from the PICC
     * @param specifiedCHAT true if PACE is used with a CHAT
     * @return true if T_PICC is equal to my T_PICC
     * @throws GeneralSecurityException
     */
    public boolean verifyToken(byte[] T_PICC, boolean specifiedCHAT) throws GeneralSecurityException {
	try {
	    TLV dataSet = TLV.fromBER(T_PICC);
	    // set of dynamic authentication data
	    if (dataSet.getTagNumWithClass() != 0x7C) {
		throw new GeneralSecurityException("The returned object is not a set of dynamic authentication data.");
	    }

	    // Authentication Token
	    List<TLV> authTokens = dataSet.findChildTags(0x86);
	    if (authTokens.isEmpty()) {
		String msg = "Authentication Token is missing in set of dynamic authentication data.";
		throw new GeneralSecurityException(msg);
	    } else if (authTokens.size() > 1) {
		String msg = "Authentication Token is present multiple times in set of dynamic authentication data.";
		throw new GeneralSecurityException(msg);
	    } else {
		byte[] newToken = authTokens.get(0).getValue();
		if (! ByteUtils.compare(newToken, token)) {
		    throw new GeneralSecurityException("Can not verify authentication token.");
		}
	    }

	    // CAR
	    if (specifiedCHAT) {
		// current CAR
		List<TLV> car1 = dataSet.findChildTags(0x87);
		if (car1.isEmpty()) {
		    String msg = "Current CAR is missing in set of dynamic authentication data.";
		    throw new GeneralSecurityException(msg);
		} else if (car1.size() > 1) {
		    String msg = "Current CAR is present multiple times in set of dynamic authentication data.";
		    throw new GeneralSecurityException(msg);
		} else {
		    currentCAR = car1.get(0).getValue();
		    verifyCAR("Current CAR", currentCAR);
		}

		// last CAR
		List<TLV> car2 = dataSet.findChildTags(0x88);
		if (car2.size() > 1) {
		    String msg = "Previous CAR is present multiple times in set of dynamic authentication data.";
		    throw new GeneralSecurityException(msg);
		} else if (car2.size() == 1) {
		    previousCAR = car2.get(0).getValue();
		    verifyCAR("Previous CAR", previousCAR);
		}
	    }
	} catch (TLVException ex) {
	    throw new GeneralSecurityException("Given data is not a valid ASN.1 object.", ex);
	}

	return true;
    }

    /**
     *
     * @return
     */
    public byte[] toByteArray() {
	return token;
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

    /**
     * Calculates the data for the authentication token.
     *
     * @param key Key
     * @return data object for token
     */
    private byte[] getMACObject(byte[] key) throws GeneralSecurityException {
	byte[] ret = null;

	try {
	    TLV keyObject = new TLV();
	    keyObject.setTagNumWithClass((byte) 0x86);
	    keyObject.setValue(ByteUtils.cutLeadingNullBytes(key));

	    TLV oidObject = new TLV();
	    oidObject.setTagNumWithClass((byte) 0x06);
	    oidObject.setValue(ObjectIdentifierUtils.getValue(pi.getProtocol()));
	    oidObject.addToEnd(keyObject);

	    TLV macObject = new TLV();
	    macObject.setTagNum((byte) 0x49);
	    macObject.setTagClass(TagClass.APPLICATION);
	    macObject.setChild(oidObject);

	    ret = macObject.toBER(true);

	} catch (Throwable e) {
	    logger.error(e.getMessage(), e);
	    throw new GeneralSecurityException(e);
	}

	return ret;
    }

    private static void verifyCAR(String name, byte[] car) throws GeneralSecurityException {
	int s = car.length;
	if (! ((8 <= s) && (s <= 16))) {
	    throw new GeneralSecurityException(String.format("%s is not withing specified size.", name));
	}
    }

}
