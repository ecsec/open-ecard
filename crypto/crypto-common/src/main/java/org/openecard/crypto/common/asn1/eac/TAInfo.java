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
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 *
 * @author Moritz Horsch
 */
public final class TAInfo extends SecurityInfo {

    private String protocol;
    private int version;
    private FileID efCVCA;
    private static final String[] protocols = new String[] {
	TAObjectIdentifier.id_TA_ECDSA_SHA_1,
	TAObjectIdentifier.id_TA_ECDSA_SHA_224,
	TAObjectIdentifier.id_TA_ECDSA_SHA_256,
	TAObjectIdentifier.id_TA_ECDSA_SHA_384,
	TAObjectIdentifier.id_TA_ECDSA_SHA_512,
	TAObjectIdentifier.id_TA_RSA_PSS_SHA_1,
	TAObjectIdentifier.id_TA_RSA_PSS_SHA_256,
	TAObjectIdentifier.id_TA_RSA_PSS_SHA_512,
	TAObjectIdentifier.id_TA_RSA_v1_5_SHA_1,
	TAObjectIdentifier.id_TA_RSA_v1_5_SHA_256,
	TAObjectIdentifier.id_TA_RSA_v1_5_SHA_512
    };

    /**
     * Creates a new TAInfo object. See TR-03110 Section A.1.1.3.
     *
     * @param seq ANS1 encoded sequence
     */
    public TAInfo(ASN1Sequence seq) {
	super(seq);

	protocol = getIdentifier();
	version = ((ASN1Integer) getRequiredData()).getValue().intValue();
	if (seq.size() == 3) {
	    efCVCA = FileID.getInstance(getOptionalData());
	}
    }

    /**
     * Checks if the protocol identifier indicates ECDSA.
     *
     * @return True if ECDSA is used, otherwise false
     */
    public boolean isECDSA() {
	if (protocol.startsWith(TAObjectIdentifier.id_TA_ECDSA)) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if the protocol identifier indicates elliptic curve RSA.
     *
     * @return True if elliptic curve RSA is used, otherwise false
     */
    public boolean isRSA() {
	if (protocol.startsWith(TAObjectIdentifier.id_TA_RSA)) {
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
     * @return version
     */
    public int getVersion() {
	return version;
    }

    /**
     * Returns the EF.CVCA.
     *
     * @return EF.CVCA
     */
    public FileID getEFCVCA() {
	return efCVCA;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if o is a TA object identifier; false otherwise
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
