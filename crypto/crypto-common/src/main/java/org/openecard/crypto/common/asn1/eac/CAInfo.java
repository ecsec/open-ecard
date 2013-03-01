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

package org.openecard.crypto.common.asn1.eac;

import org.openecard.bouncycastle.asn1.ASN1Integer;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.crypto.common.asn1.eac.oid.CAObjectIdentifier;


/**
 * See BSI-TR-03110, version 2.05, section A.1.1.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CAInfo extends SecurityInfo {

    private String protocol;
    private int version;
    private int keyID;
    private static final String[] protocols = new String[]{
	CAObjectIdentifier.id_CA_DH_3DES_CBC_CBC,
	CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_128,
	CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_192,
	CAObjectIdentifier.id_CA_DH_AES_CBC_CMAC_256,
	CAObjectIdentifier.id_CA_ECDH_3DES_CBC_CBC,
	CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128,
	CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_192,
	CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_256
    };

    /**
     * Creates a new ChipAuthenticationInfo object.
     *
     * @param seq ANS1 encoded data
     */
    public CAInfo(ASN1Sequence seq) {
	super(seq);
	protocol = getIdentifier();
	version = ((ASN1Integer) getRequiredData()).getValue().intValue();
	if (seq.size() == 3) {
	    keyID = ((ASN1Integer) getOptionalData()).getValue().intValue();
	}
    }

    /**
     * Checks if the protocol identifier indicates Diffie-Hellman.
     *
     * @return True if Diffie-Hellman is used, otherwise false
     */
    public boolean isDH() {
	if (protocol.startsWith(CAObjectIdentifier.id_CA_DH)) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
     *
     * @return True if elliptic curve Diffie-Hellman is used, otherwise false
     */
    public boolean isECDH() {
	if (protocol.startsWith(CAObjectIdentifier.id_CA_ECDH)) {
	    return true;
	}
	return false;
    }

    /**
     * Returns the object identifier of the protocol.
     *
     * @return Protocol
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Returns the version of the protocol.
     *
     * @return Version
     */
    public int getVersion() {
	return version;
    }

    /**
     * Returns the key identifier.
     *
     * @return KeyID
     */
    public int getKeyID() {
	return keyID;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if o is a ChipAuthentication object identifier; false otherwise
     */
    public static boolean isObjectIdentifier(String oid) {
	for (int i = 0; i < protocols.length; i++) {
	    if (protocols[i].equals(oid)) {
		return true;
	    }
	}
	return false;
    }

}
