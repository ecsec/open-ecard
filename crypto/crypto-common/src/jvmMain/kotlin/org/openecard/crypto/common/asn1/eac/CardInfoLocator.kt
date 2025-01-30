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

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.bouncycastle.asn1.DERIA5String

/**
 *
 * @author Moritz Horsch
 */
class CardInfoLocator(seq: ASN1Sequence) {
    val protocol: String

    /**
     * Gets the URL.
     *
     * @return the URL
     */
    val uRL: String

    /**
     * Gets the EFCardInfo fileID.
     *
     * @return the EFCardInfo fileID
     */
    val eFCardInfo: FileID?

    /**
     * Instantiates a new card info locator.
     *
     * @param seq the ASN1 encoded sequence
     */
    init {
        if (seq.size() == 2) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
            this.uRL = DERIA5String.getInstance(seq.getObjectAt(1)).string
			this.eFCardInfo = null
        } else if (seq.size() == 3) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
            this.uRL = DERIA5String.getInstance(seq.getObjectAt(1)).string
            this.eFCardInfo = FileID.Companion.getInstance(seq.getObjectAt(2))
        } else {
            throw IllegalArgumentException("Sequence wrong size for CardInfoLocator")
        }
    }

    companion object {
        /**
         * Gets the single instance of CardInfoLocator.
         *
         * @param obj
         * @return single instance of CardInfoLocator
         */
        fun getInstance(obj: Any): CardInfoLocator {
            if (obj is CardInfoLocator) {
                return obj
            } else if (obj is ASN1Sequence) {
                return CardInfoLocator(obj)
            }

            throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass.getName())
        }
    }
}
