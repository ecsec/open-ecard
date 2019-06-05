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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;


/**
 *
 * @author Moritz Horsch
 */
public class SubjectPublicKeyInfo {

    private String algorithm;
    private byte[] subjectPublicKey;

    /**
     * Instantiates a new SubjectPublicKeyInfo.
     *
     * @param seq the ASN1 encoded sequence
     */
    public SubjectPublicKeyInfo(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    subjectPublicKey = DERBitString.getInstance(seq.getObjectAt(1)).getBytes();
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for SubjectPublicKeyInfo");
	}
    }

    /**
     * Gets the single instance of SubjectPublicKeyInfo.
     *
     * @param obj
     * @return single instance of SubjectPublicKeyInfo
     */
    public static SubjectPublicKeyInfo getInstance(Object obj) {
	if (obj == null || obj instanceof SubjectPublicKeyInfo) {
	    return (SubjectPublicKeyInfo) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new SubjectPublicKeyInfo((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the algorithm.
     *
     * @return the algorithm
     */
    public String getAlgorithm() {
	return algorithm;
    }

    /**
     * Gets the subject public key.
     *
     * @return the subject public key
     */
    public byte[] getSubjectPublicKey() {
	return subjectPublicKey;
    }

}
