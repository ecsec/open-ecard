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

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.openecard.crypto.common.asn1.eac.oid.PACEObjectIdentifier;


/**
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch
 */
public final class PACEInfo extends SecurityInfo {

    private String protocol;
    private int version;
    private int parameterID = -1;
    private static final String[] protocols = new String[] {
	PACEObjectIdentifier.id_PACE_DH_GM_3DES_CBC_CBC,
	PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_128,
	PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_192,
	PACEObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_256,
	PACEObjectIdentifier.id_PACE_DH_IM_3DES_CBC_CBC,
	PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_128,
	PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_192,
	PACEObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_256,
	PACEObjectIdentifier.id_PACE_ECDH_GM_3DES_CBC_CBC,
	PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
	PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
	PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
	PACEObjectIdentifier.id_PACE_ECDH_IM_3DES_CBC_CBC,
	PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_128,
	PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_192,
	PACEObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_256
    };

    /**
     * Creates a new PACEInfo object.
     *
     * @param seq ANS1 encoded data
     */
    public PACEInfo(ASN1Sequence seq) {
	super(seq);
	protocol = getIdentifier();
	version = ((ASN1Integer) getRequiredData()).getValue().intValue();
	if (seq.size() == 3) {
	    parameterID = ((ASN1Integer) getOptionalData()).getValue().intValue();
	}
    }

    /**
     * Returns the object identifier of the protocol.
     *
     * @return Protocol object identifier
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Returns the version of the protocol.
     *
     * @return version
     */
    public int getVersion() {
	return version;
    }

    /**
     * Returns the parameter identifier.
     *
     * @return parameter identifier
     */
    public int getParameterID() {
	return parameterID;
    }

    /**
     * Checks if the protocol identifier indicates generic mapping.
     *
     * @return True if generic mapping is used, otherwise false
     */
    public boolean isGM() {
	if (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_GM)
		|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_GM)) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if the protocol identifier indicates integrated mapping.
     *
     * @return True if integrated mapping is used, otherwise false
     */
    public boolean isIM() {
	if (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_IM)
		|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_IM)) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if the protocol identifier indicates Diffie-Hellman.
     *
     * @return True if Diffie-Hellman is used, otherwise false
     */
    public boolean isDH() {
	if (protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_GM)
		|| protocol.startsWith(PACEObjectIdentifier.id_PACE_DH_IM)) {
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
	if (protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_GM)
		|| protocol.startsWith(PACEObjectIdentifier.id_PACE_ECDH_IM)) {
	    return true;
	}
	return false;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if oid is a PACE object identifier; false otherwise
     */
    public static boolean isPACEObjectIdentifer(String oid) {
	for (int i = 0; i < protocols.length; i++) {
	    if (protocols[i].equals(oid)) {
		return true;
	    }
	}

	return false;
    }

}
