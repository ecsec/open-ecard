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
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger

/**
 * Implements the PINCompareDIDAuthenticateOutputType.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class PINCompareDIDAuthenticateOutputType {
    private val authMap: AuthDataMap
    /**
     * Returns the retry counter.
     *
     * @return Retry counter
     */
    /**
     * Sets the retry counter.
     *
     * @param retryCounter Retry counter
     */
    @JvmField
    var retryCounter: BigInteger? = null

    /**
     * Creates a new PINCompareDIDAuthenticateOutputType.
     *
     * @param data DIDAuthenticationDataType Generic type containing a PinCompareDIDAuthenticateOutputType.
     * @throws ParserConfigurationException
     */
    constructor(data: DIDAuthenticationDataType) {
        authMap = AuthDataMap(data)

        val retryCounterStr = authMap.getContentAsString(ISO_NS, "RetryCounter")
        if (retryCounterStr != null) {
            try {
                retryCounter = BigInteger(retryCounterStr)
            } catch (ex: NumberFormatException) {
                LOG.warn("Can not convert malformed RetryCounter value to an integer.", ex)
            }
        }
    }

    /**
     * Creates a new PINCompareDIDAuthenticateOutputType.
     *
     * @param authMap AuthDataMap
     */
    constructor(authMap: AuthDataMap) {
        this.authMap = authMap
    }

    val authDataType: PinCompareDIDAuthenticateOutputType?
        /**
         *
         * @return the PinCompareDIDAuthenticateOutputType
         */
        get() {
            val pinCompareOutput = PinCompareDIDAuthenticateOutputType()
            val authResponse =
                authMap.createResponse(pinCompareOutput)!!
            if (retryCounter != null) {
                authResponse.addElement(
                    ISO_NS,
                    "RetryCounter",
                    retryCounter.toString()
                )
            }

            return authResponse.response
        }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(PINCompareDIDAuthenticateOutputType::class.java)
        private const val ISO_NS = "urn:iso:std:iso-iec:24727:tech:schema"
    }
}
