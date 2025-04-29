/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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
package org.openecard.common.anytype.pin

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType
import org.openecard.common.anytype.AuthDataMap
import java.util.*

/**
 * Implements the PINCompareDIDAuthenticateInputType.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class PINCompareDIDAuthenticateInputType(data: DIDAuthenticationDataType) {
    private val authMap = AuthDataMap(data)
    private var pin: CharArray?

    /**
     * Creates a new PINCompareDIDAuthenticateInputType.
     *
     * @param data DIDAuthenticationDataType
     * @throws ParserConfigurationException
     */
    init {
        // Optional contents
        val tmpPin = authMap.getContentAsString("Pin")
        pin = tmpPin?.toCharArray() ?: CharArray(0)
    }

    var pIN: CharArray?
        /**
         * Returns the PIN.
         *
         * @return PIN
         */
        get() = pin!!.clone()
        set(pin) {
            if (this.pin != null) {
                Arrays.fill(this.pin, ' ')
            }
            if (pin != null) {
                this.pin = pin.clone()
            } else {
                this.pin = null
            }
        }

    val outputType: PINCompareDIDAuthenticateOutputType
        /**
         * Returns a new PINCompareDIDAuthenticateOutputType.
         *
         * @return PINCompareDIDAuthenticateOutputType
         */
        get() = PINCompareDIDAuthenticateOutputType(authMap)

    val authDataType: PinCompareDIDAuthenticateInputType?
        get() {
            val pinCompareOutput = PinCompareDIDAuthenticateInputType()
            val authResponse =
                authMap.createResponse(pinCompareOutput)!!
            if (pin != null) {
                // NOTE: no way to use char[] in XML DOM, so PIN can not be deleted from memory afterwards
                authResponse.addElement(
                    ISO_NS,
                    "Pin",
                    String(pin!!)
                )
            }

            return authResponse.response
        }

    companion object {
        private const val ISO_NS = "urn:iso:std:iso-iec:24727:tech:schema"
    }
}
