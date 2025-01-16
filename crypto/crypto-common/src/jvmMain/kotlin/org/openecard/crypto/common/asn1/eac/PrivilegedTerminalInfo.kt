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
import org.openecard.bouncycastle.asn1.ASN1Set

/**
 *
 * @author Moritz Horsch
 */
class PrivilegedTerminalInfo(seq: ASN1Sequence) {
    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    val protocol: String

    /**
     * Gets the privileged terminal info.
     *
     * @return the privileged terminal info
     */
    val privilegedTerminalInfo: SecurityInfos

    /**
     * Instantiates a new privileged terminal info.
     *
     * @param seq ASN1 encoded sequence
     */
    init {
        if (seq.size() == 2) {
            protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString()
            privilegedTerminalInfo = SecurityInfos.Companion.getInstance(seq.getObjectAt(1) as ASN1Set)
        } else {
            throw IllegalArgumentException("Sequence wrong size for PrivilegedTerminalInfo")
        }
    }

    companion object {
        /**
         * Gets the single instance of PrivilegedTerminalInfo.
         *
         * @param obj
         * @return single instance of PrivilegedTerminalInfo
         */
        fun getInstance(obj: Any): PrivilegedTerminalInfo {
            if (obj is PrivilegedTerminalInfo) {
                return obj
            } else if (obj is ASN1Sequence) {
                return PrivilegedTerminalInfo(obj)
            }

            throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass.getName())
        }
    }
}
