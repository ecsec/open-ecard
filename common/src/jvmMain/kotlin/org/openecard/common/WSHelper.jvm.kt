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

package org.openecard.common

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType
import oasis.names.tc.dss._1_0.core.schema.ResponseBaseType
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.util.HandlerUtils
import javax.annotation.Nonnull

/**
 *
 * @author Tobias Wich
 */
object WSHelper {
    @JvmStatic
	@Throws(WSException::class)
    fun <T : ResponseBaseType> checkResult(response: T): T {
        val r = response.result
        if (r.resultMajor == ECardConstants.Major.ERROR) {
            if (response is TransmitResponse) {
                val tr = response as TransmitResponse
                val rApdus = tr.outputAPDU

                if (rApdus.size < 1) {
                    throw WSException(r)
                } else {
                    val apdu = CardResponseAPDU.getTrailer(rApdus[rApdus.size - 1])
                    val msg = CardCommandStatus.getMessage(apdu)
                    throw WSException(msg)
                }
            } else {
                throw WSException(r)
            }
        }
        return response
    }

	@JvmStatic
    fun <T : ResponseBaseType> resultIsOk(response: T): Boolean {
        val r = response.result
        return r.resultMajor == ECardConstants.Major.OK
    }

	@JvmStatic
    fun <T : ResponseBaseType> resultIsError(response: T): Boolean {
        val r = response.result
        return r.resultMajor == ECardConstants.Major.ERROR
    }

    @JvmStatic
	fun <T : ResponseBaseType> resultIsWarn(response: T): Boolean {
        val r = response.result
        return r.resultMajor == ECardConstants.Major.WARN
    }

	@JvmStatic
    fun <T : ResponseBaseType> minorIsOneOf(response: T, vararg minors: String): Boolean {
        val r = response.result
        return minorIsOneOf(r, *minors)
    }

    @JvmStatic
	fun <T : ECardException> minorIsOneOf(ex: T, vararg minors: String): Boolean {
        val r = ex.result
        return minorIsOneOf(r, *minors)
    }

    @JvmStatic
	fun minorIsOneOf(r: Result, vararg minors: String): Boolean {
        val minor = r.resultMinor

        if (minor != null) {
            for (next in minors) {
                if (r.resultMinor == next) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Creates a WSException instance based on the given Result instance.
     * The result is not checked if it represents an error. The caller of this function should be aware of that fact.
     *
     * @param r The Result instance which is the foundation of the resulting exception.
     * @return The exception instance which is built from the given Result instance.
     */
	@JvmStatic
	fun createException(r: Result): WSException {
        return WSException(r)
    }

    ///
    /// functions to create OASIS Result messages
    ///
	@JvmStatic
	fun makeResultOK(): Result {
        val result = makeResult(ECardConstants.Major.OK, null, null, null)
        return result
    }

    @JvmStatic
	fun makeResultUnknownError(msg: String?): Result {
        val result = makeResult(ECardConstants.Major.ERROR, ECardConstants.Minor.App.UNKNOWN_ERROR, msg)
        return result
    }

    @JvmStatic
	fun makeResultUnknownIFDError(msg: String?): Result {
        val result = makeResult(ECardConstants.Major.ERROR, ECardConstants.Minor.IFD.UNKNOWN_ERROR, msg)
        return result
    }

    @JvmStatic
	fun makeResult(major: String, minor: String?, message: String?): Result {
        val result = makeResult(major, minor, message, "en")
        return result
    }

    @JvmStatic
	fun makeResultError(minor: String?, message: String?): Result {
        val result = makeResult(ECardConstants.Major.ERROR, minor, message, "en")
        return result
    }

    @JvmStatic
	fun makeResult(exc: Throwable): Result {
        if (exc is ECardException) {
            val result = exc.result
            return result
        } else {
            val result = makeResultUnknownError(exc.message)
            return result
        }
    }

    fun makeResult(major: String, minor: String?, message: String?, lang: String?): Result {
        val r = Result()
        r.resultMajor = major
        r.resultMinor = minor
        if (message != null) {
            val msg = InternationalStringType()
            msg.value = message
            msg.lang = lang
            r.resultMessage = msg
        }
        return r
    }

    @JvmStatic
	fun <C : Class<T>, T : ResponseBaseType> makeResponse(c: C, r: Result): T? {
        try {
            val t = c.getConstructor().newInstance()
            t.profile = ECardConstants.Profile.ECARD_1_1
            t.result = r
            return t
        } catch (ignore: Exception) {
            return null
        }
    }


//    @Deprecated("")
//    fun copyHandle(handle: ConnectionHandleType): ConnectionHandleType {
//        return HandlerUtils.copyHandle(handle)
//    }
//
//    @Deprecated("")
//    fun copyPath(handle: CardApplicationPathType): CardApplicationPathType {
//        return HandlerUtils.copyPath(handle)
//    }

    open class WSException : ECardException {
        constructor(r: Result) : super(makeOasisResultTraitImpl(r)) {
        }

        constructor(msg: String) : super(makeOasisResultTraitImpl(msg)) {
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }
}
