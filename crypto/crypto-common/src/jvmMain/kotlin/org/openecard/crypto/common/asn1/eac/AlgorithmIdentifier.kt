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
package org.openecard.crypto.common.asn1.eac

import org.openecard.bouncycastle.asn1.ASN1Object
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1Sequence

/**
 *
 * @author Moritz Horsch
 */
class AlgorithmIdentifier private constructor(seq: ASN1Sequence) {
    /**
     * Gets the object identifier.
     *
     * @return Object identifier
     */
    val objectIdentifier: String

    /**
     * Gets the parameters.
     *
     * @return Parameters
     */
    val parameters: ASN1Object?


    /**
     * Instantiates a new algorithm identifier.
     *
     * @param seq ASN1 encoded sequence
     */
    init {
        if (seq.size() == 1) {
            this.objectIdentifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
			parameters = null
        } else if (seq.size() == 2) {
            this.objectIdentifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
            parameters = seq.getObjectAt(1) as ASN1Object?
        } else {
            throw IllegalArgumentException("Sequence wrong size for AlgorithmIdentifier")
        }
    }

    companion object {
        /**
         * Gets the single instance of AlgorithmIdentifier.
         *
         * @param obj
         * @return Single instance of AlgorithmIdentifier
         */
        fun getInstance(obj: Any): AlgorithmIdentifier {
            if (obj is AlgorithmIdentifier) {
                return obj
            } else if (obj is ASN1Sequence) {
                return AlgorithmIdentifier(obj)
            }

            throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass.getName())
        }
    }
}
