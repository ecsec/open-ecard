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

import org.openecard.bouncycastle.asn1.ASN1Object;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class AlgorithmIdentifier {

    private String algorithm;
    private ASN1Object parameters;

    /**
     * Instantiates a new algorithm identifier.
     *
     * @param seq ASN1 encoded sequence
     */
    public AlgorithmIdentifier(ASN1Sequence seq) {
	if (seq.size() == 1) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	} else if (seq.size() == 2) {
	    algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    parameters = (ASN1Object) seq.getObjectAt(1);
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for AlgorithmIdentifier");
	}
    }

    /**
     * Gets the single instance of AlgorithmIdentifier.
     *
     * @param obj
     * @return Single instance of AlgorithmIdentifier
     */
    public static AlgorithmIdentifier getInstance(Object obj) {
	if (obj == null || obj instanceof AlgorithmIdentifier) {
	    return (AlgorithmIdentifier) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new AlgorithmIdentifier((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the object identifier.
     *
     * @return Object identifier
     */
    public String getObjectIdentifier() {
	return algorithm;
    }

    /**
     * Gets the parameters.
     *
     * @return Parameters
     */
    public ASN1Object getParameters() {
	return parameters;
    }

}
