/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.util.UrlBuilder
import java.net.URISyntaxException
import java.util.concurrent.Future

/**
 * Implements a TCTokenResponse.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class TCTokenResponse : BindingResult() {
    private var result: Result? = null
    private var token: TCToken? = null
    var bindingTask: Future<*>? = null

    /**
     * Returns the result of the client request.
     *
     * @return Result
     */
    fun getResult(): Result {
        if (result == null) {
            result = makeResultOK()
        }
        return result!!
    }

    /**
     * Sets the result of the client request.
     *
     * @param result
     */
    fun setResult(result: Result?) {
        this.result = result
    }

    fun setAdditionalResultMinor(minor: String?) {
        this.addAuxResultData(AuxDataKeys.MINOR_PROCESS_RESULT, minor)
    }

    var tCToken: TCToken
        /**
         * Gets the TCToken of the request.
         *
         * @return The TCToken.
         */
        get() = token!!
        /**
         * Sets the TCToken as received in the request.
         *
         * @param token The TCToken.
         */
        set(token) {
            this.token = token
        }

    var refreshAddress: String?
        /**
         * Returns the refresh address.
         *
         * @return Refresh address
         */
        get() = token!!.getRefreshAddress()
        /**
         * Sets the refresh address in the underlying TCToken.
         *
         * @param addr The new refresh address.
         */
        set(addr) {
            token!!.setRefreshAddress(addr)
        }

    /**
     * Completes the response, so that it can be used in the binding.
     * The values extended include result code, result message and the redirect address.
     *
     * @throws InvalidRedirectUrlException Thrown in case the error redirect URL could not be determined.
     */
    @Throws(InvalidRedirectUrlException::class)
    fun finishResponse() {
        try {
            val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
            val ub = UrlBuilder.fromUrl(this.refreshAddress)
            if (ECardConstants.Major.OK == result!!.getResultMajor()) {
                resultCode = BindingResultCode.REDIRECT
                val refreshURL: String? = ub.queryParam("ResultMajor", "ok").build().toString()
                auxResultData.put(AuxDataKeys.REDIRECT_LOCATION, refreshURL)
            } else {
                val isRefreshAddressValid = dynCtx.get(TR03112Keys.IS_REFRESH_URL_VALID) as Boolean
                val minor = result!!.getResultMinor()
                resultCode = BindingResultCode.REDIRECT
                val refreshURL: String?

                if (isRefreshAddressValid) {
                    val fixedMinor = TCTokenHacks.fixResultMinor(minor)
                    refreshURL = ub.queryParam("ResultMajor", "error")
                        .queryParamUrl("ResultMinor", fixedMinor)
                        .build().toString()
                } else {
                    refreshURL = token!!.getComErrorAddressWithParams(minor)
                }

                auxResultData.put(AuxDataKeys.REDIRECT_LOCATION, refreshURL)

                if (result!!.getResultMessage().getValue() != null) {
                    setResultMessage(result!!.getResultMessage().getValue())
                }
            }
        } catch (ex: URISyntaxException) {
            // this is a code failure as the URLs are verified upfront
            // TODO: translate when exception changes
            throw IllegalArgumentException(LANG.getOriginalMessage(ErrorTranslations.INVALID_URL), ex)
        }
    }

    companion object {
        private val LANG: I18n = I18n.getTranslation("tr03112")
    }
}
