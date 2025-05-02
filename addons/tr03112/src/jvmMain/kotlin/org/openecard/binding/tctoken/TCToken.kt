/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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
package org.openecard.binding.tctoken

import generated.TCTokenType
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException
import org.openecard.common.util.UrlBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException
import javax.annotation.Nonnull

/**
 * Helper class adding further functionality to the underlying TCTokenType.
 *
 * @author Tobias Wich
 */
class TCToken : TCTokenType() {
    /**
     * Indicates whether the original data of the TCToken contains a invalid PSK.
     * <br></br>
     * Note: This method is just for the case there was PSK data in the original data received. If the original TCToken
     * data does not contain the PathSecurity-Parameters or the PSK element than this method will return false.
     *
     * @return `TRUE` if the PSK contain in the TCToken is invalid else `FALSE`.
     */
    /**
     * Sets the indicator of an invalid PSK.
     * <br></br>
     * Note: This method is just for the case there is a PSK in the original TCToken but it is invalid for some reason.
     * If there is no PSK in the TCToken data this message should not be used.
     *
     * @param this.isInvalidPSK `TRUE` if the PSK should be marked as invalid else `FALSE`. The indicator is set to
     * `FALSE` per default.
     */
    var isInvalidPSK: Boolean = false

    /**
     * Gets the CommunicationErrorAddress for use in error conditions.
     * If the CommunicationErrorAddress is available this one is used.
     *
     * @param minor The ResultMinor string.
     * @return The error URL.
     * @throws InvalidRedirectUrlException In case the address is not present or a valid URL.
     */
    @Throws(InvalidRedirectUrlException::class)
    fun getComErrorAddressWithParams(@Nonnull minor: String): String? {
        try {
            val errorUrl = getCommunicationErrorAddress()
            val url: URI = checkUrl(errorUrl)
            val result: String? = UrlBuilder.fromUrl(url).queryParam("ResultMajor", "error")
                .queryParamUrl("ResultMinor", TCTokenHacks.fixResultMinor(minor))
                .build().toString()
            return result
        } catch (ex: URISyntaxException) {
            // should not happen, but here it is anyways
            LOG.error("Construction of redirect URL failed.", ex)
            throw InvalidRedirectUrlException(ErrorTranslations.NO_URL)
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(TCToken::class.java)

        @Throws(InvalidRedirectUrlException::class)
        private fun checkUrl(urlStr: String?): URI {
            if (urlStr != null && !urlStr.isEmpty()) {
                try {
                    val url = URI(urlStr)
                    return url
                } catch (ex: URISyntaxException) {
                    LOG.error("No valid CommunicationErrorAddress provided.")
                    throw InvalidRedirectUrlException(ErrorTranslations.NO_URL)
                }
            } else {
                LOG.error("No CommunicationErrorAddress to perform a redirect provided.")
                throw InvalidRedirectUrlException(ErrorTranslations.NO_REDIRECT_AVAILABLE)
            }
        }
    }
}
