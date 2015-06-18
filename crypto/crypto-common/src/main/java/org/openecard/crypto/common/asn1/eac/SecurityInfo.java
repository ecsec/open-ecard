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

import org.openecard.bouncycastle.asn1.ASN1Encodable;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;


/**
 *
 * @author Moritz Horsch
 */
public class SecurityInfo {

    private ASN1ObjectIdentifier identifier;
    private ASN1Encodable requiredData;
    private ASN1Encodable optionalData;

    /**
     * Gets the single instance of SecurityInfo.
     *
     * @param obj
     * @return single instance of SecurityInfo
     */
    public static SecurityInfo getInstance(Object obj) {
	if (obj == null || obj instanceof SecurityInfo) {
	    return (SecurityInfo) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new SecurityInfo((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Instantiates a new security info.
     *
     * @param seq
     */
    public SecurityInfo(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    identifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
	    requiredData = seq.getObjectAt(1);

	} else if (seq.size() == 3) {
	    identifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
	    requiredData = seq.getObjectAt(1);
	    optionalData = seq.getObjectAt(2);
	} else {
	    throw new IllegalArgumentException("sequence wrong size for CertificateList");
	}
    }

    /**
     * Instantiates a new security info.
     *
     * @param contentType the content type
     * @param requiredData the required data
     */
    public SecurityInfo(ASN1ObjectIdentifier contentType, ASN1Encodable requiredData) {
	this.identifier = contentType;
	this.requiredData = requiredData;
    }

    /**
     * Instantiates a new security info.
     *
     * @param contentType the content type
     * @param requiredData the required data
     * @param optionalData the optional data
     */
    public SecurityInfo(ASN1ObjectIdentifier contentType, ASN1Encodable requiredData, ASN1Encodable optionalData) {
	this.identifier = contentType;
	this.requiredData = requiredData;
	this.optionalData = optionalData;
    }

    /**
     * Returns the object identifier..
     *
     * @return Object identifier
     */
    public String getIdentifier() {
	return identifier.toString();
    }

    /**
     * Returns the required data.
     *
     * @return Required data
     */
    public ASN1Encodable getRequiredData() {
	return requiredData;
    }

    /**
     * Returns the optional data.
     *
     * @return Optional data
     */
    public ASN1Encodable getOptionalData() {
	return optionalData;
    }

}
