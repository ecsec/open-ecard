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
 */
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException

/**
 * The class models a FCI data type (see also ISO 7816-4).
 *
 * @author Hans-Martin Haase
 */
class FCI(
    /**
     * The TLV representing the FCI.
     */
    private val tlv: TLV
) {
    /**
     * Get the FCP data contained in the FCI if available.
     * If no FCP data is available null is returned.
     *
     * @return FCP if contained in the FCI or null.
     */
    /**
     * The FCP data contained in the FCI.
     */
    var fCPData: FCP? = null
        private set

    /**
     * Get the FMD data contained in the FCI if available.
     * If no FMD data is available null is returned.
     *
     * @return FMD if contained in the FCI or null.
     */
    /**
     * The FMD data contained in the FCI.
     */
    var fMDData: FMD? = null
        private set

    /**
     * The constructor extracts the FMD and FCP data from the FCI and stores them in the global variables.
     *
     * @param fciTLV A TLV representing the FCI.
     * @throws TLVException Thrown if the creation of the FCP or FMD object failed.
     * @throws UnsupportedEncodingException Thrown if the URL contained in the FMD can't be encoded.
     */
    init {
        // there are two possibilities for the structure of this object
        // 1. explicite FCI template which contains FCP and/or FMD templates.
        if (tlv.tagNumWithClass == 0x6FL) {
            var child = tlv.child
            if (child.tagNumWithClass == 0x62L) {
                fCPData = FCP(child!!)
                child = child.next
            } else {
                fCPData = null
            }

            if (child.tagNumWithClass == 0x64L) {
                fMDData = FMD(child!!)
            } else {
                fMDData = null
            }
        } else if (tlv.tagNumWithClass == 0x62L) {
            // 2. no FCI template just FCP and FMD templates
            fCPData = FCP(tlv)

            if (tlv.hasNext()) {
                val next = tlv.next
                if (next.tagNumWithClass == 0x64L) {
                    fMDData = FMD(next!!)
                }
            }
        } else if (tlv.tagNumWithClass == 0x64L) {
            fMDData = FMD(tlv)
            fCPData = null
        } else {
            val msg = "Unknown FCI tag discovered."
            throw TLVException(msg)
        }
    }
}
