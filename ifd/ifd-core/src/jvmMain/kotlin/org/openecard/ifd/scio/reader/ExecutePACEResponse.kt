/****************************************************************************
 * Copyright (C) 2012-2020 ecsec GmbH.
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
package org.openecard.ifd.scio.reader

import io.github.oshai.kotlinlogging.KotlinLogging
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.ifd.PacePinStatus
import org.openecard.common.ifd.PacePinStatus.Companion.fromCode
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.IntegerUtils

private val LOG = KotlinLogging.logger {  }

/**
 *
 * @author Tobias Wich
 */
class ExecutePACEResponse(response: ByteArray) {

    val resultCode: Int = ByteUtils.toInteger(response.copyOfRange(0, 4), false)
	val length: Short = ByteUtils.toShort(response.copyOfRange(4, 6), false)
	val data: ByteArray = response.copyOfRange(6, 6 + length)

	val isError: Boolean
        get() = this.resultCode != 0

    fun getResult(): Result {
        when (this.resultCode.toLong()) {
            0x00000000L -> return makeResultOK()
			// errors in input
			0xD0000001L -> return makeResultUnknownError("Inconsistent lengths in input.")
			0xD0000002L -> return makeResultUnknownError("Unexpected data in input.")
			0xD0000003L -> return makeResultUnknownError("Unexpected combination of data in input.")
			// errors in protocol
			0xE0000001L -> return makeResultUnknownError("Syntax error in TLV response.")
			0xE0000002L -> return makeResultUnknownError("Unexpected or missing object in TLV response.")
			0xE0000003L -> return makeResultUnknownError("Unknown PIN-ID.")
			0xE0000006L -> return makeResultUnknownError("Wrong Authentication Token.")
			// Others
			0xF0100001L -> return makeResultUnknownError("Communication abort.")
			0xF0100002L -> return makeResultError(ECardConstants.Minor.IFD.Terminal.NO_CARD, "No card.")
			0xF0200001L -> return makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Abort.")
			0xF0200002L -> return makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.")
            else -> {
                val sw = byteArrayOf(((this.resultCode shr 8) and 0xFF).toByte(), (this.resultCode and 0xFF).toByte())
                val msg = CardCommandStatus.getMessage(sw)
                val type = (this.resultCode shr 16) and 0xFFFF
                if ((this.resultCode.toLong() and 0xFFFC0000) == 0xF0000000) {
                    when (type) {
                        0xF000 -> return makeResultUnknownError("Select EF.CardAccess: $msg")
                        0xF001 -> return makeResultUnknownError("Read Binary EF.CardAccess: $msg")
                        0xF002 -> return makeResultUnknownError("MSE Set AT: $msg")
                        0xF003 -> return makeResultUnknownError("General Authenticate Step 1-4: $msg")
                    }
                } else if (ByteUtils.toInteger(sw) == 0x6300) {
                    return makeResultError(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED, msg)
                }

                when (fromCode(sw)) {
                    PacePinStatus.RC2 -> return makeResultError(ECardConstants.Minor.IFD.PASSWORD_ERROR, msg)
                    PacePinStatus.RC1 -> return makeResultError(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED, msg)
                    PacePinStatus.BLOCKED -> return makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, msg)
                    PacePinStatus.DEACTIVATED -> return makeResultError(
                        ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED,
                        msg
                    )

                    else -> {
                        // unknown error
                        val hexStringResult = ByteUtils.toHexString(
                            IntegerUtils.toByteArray(this.resultCode)
                        )
						LOG.warn { "Unknown error in ExecutePACEResponse: $hexStringResult" }
                        return makeResultUnknownError(null)
                    }
                }
            }
        }
    }

}
