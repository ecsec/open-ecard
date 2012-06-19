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

package org.openecard.client.ifd.protocol.pace.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.openecard.bouncycastle.crypto.engines.AESEngine;
import org.openecard.bouncycastle.crypto.macs.CMac;
import org.openecard.bouncycastle.crypto.params.KeyParameter;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TagClass;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See BSI-TR-03110, version 2.10, part 3, section B.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class AuthenticationToken {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationToken.class.getName());
    // Byte encoded token
    private byte[] token = new byte[8];
    // Certificate Authority Reference (CAR)
    private byte[] currentCAR, previousCAR;
    private PACESecurityInfos psi;

    /**
     * Creates a new AuthenticationToken.
     *
     * @param psi PACESecurityInfos
     */
    public AuthenticationToken(PACESecurityInfos psi) {
	this.psi = psi;
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
     * @return true if token is equal to my token
     * @throws GeneralSecurityException
     */
    public boolean verifyToken(AuthenticationToken token) throws GeneralSecurityException {
	return verifyToken(token.toByteArray());
    }

    /**
     * Verify the authentication token by the PICC and extract Certificate Authority Reference (CAR).
     *
     * @param T_PICC Token from the PICC
     * @return true if T_PICC is equal to my T_PICC
     * @throws GeneralSecurityException
     */
    public boolean verifyToken(byte[] T_PICC) throws GeneralSecurityException {
	final ByteArrayInputStream bais = new ByteArrayInputStream(T_PICC);

	byte tag = (byte) bais.read();
	byte size = (byte) bais.read();

	// Verify tag and size
	if (tag == (byte) 0x7C && (size & 0xFF) == bais.available()) {
	    tag = (byte) bais.read();
	    size = (byte) bais.read();
	} else {
	    throw new GeneralSecurityException("Malformed authentication token");
	}

	// Verify authentication token T_PICC
	if (tag == (byte) 0x86 && (size & 0xFF) == 8) {
	    byte[] buf = new byte[8];
	    bais.read(buf, 0, 8);
	    if (!ByteUtils.compare(buf, token)) {
		throw new GeneralSecurityException("Cannot verify authentication token");
	    }
	    // Read next bytes
	    tag = (byte) bais.read();
	    size = (byte) bais.read();
	} else {
	    throw new GeneralSecurityException("Malformed authentication token");
	}

	// Read current CAR
	if (tag == (byte) 0x87 && size == (byte) 0x0E) {
	    currentCAR = new byte[size];
	    bais.read(currentCAR, 0, size);

	    // Read next bytes
	    tag = (byte) bais.read();
	    size = (byte) bais.read();
	} else {
	    throw new GeneralSecurityException("Malformed authentication token");
	}

	// Read optional previous CAR
	if (bais.available() > 0) {
	    if (tag == (byte) 0x88 && size == (byte) 0x0E) {
		previousCAR = new byte[size];
		bais.read(previousCAR, 0, size);
	    } else {
		throw new GeneralSecurityException("Malformed authentication token");
	    }
	}

	try {
	    bais.close();
	} catch (IOException ignore) {
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
	    oidObject.setValue(ObjectIdentifierUtils.getValue(psi.getPACEInfo().getProtocol()));
	    oidObject.addToEnd(keyObject);

	    TLV macObject = new TLV();
	    macObject.setTagNum((byte) 0x49);
	    macObject.setTagClass(TagClass.APPLICATION);
	    macObject.setChild(oidObject);

	    ret = macObject.toBER(true);

	} catch (Throwable e) {
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    throw new GeneralSecurityException(e);
	}

	return ret;
    }

}
