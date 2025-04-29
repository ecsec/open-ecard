/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.common.apdu.common

import org.openecard.common.apdu.common.APDUTemplateException
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.*
import javax.annotation.Nonnull

/**
 * APDU Template function capable of creating TLV BER data based on a tag and a value.
 *
 * @author Tobias Wich
 */
class TLVFunction : APDUTemplateFunction {
    @Nonnull
    @Throws(APDUTemplateException::class)
    override fun call(vararg params: Any?): String? {
        if (params.size != 2) {
            throw APDUTemplateException("Invalid number of parameters given. Two are needed.")
        }
        val tag = makeBytes(params[0])
        val value = makeBytes(params[1])

        try {
            val tlv = TLV()
            tlv.setTagNumWithClass(tag)
            tlv.value = value

            val result = tlv.toBER()
            val resultStr = ByteUtils.toHexString(result)
            return resultStr
        } catch (ex: TLVException) {
            throw APDUTemplateException("Failed to create TLV structure based on given parameters.", ex)
        }
    }

    private fun makeBytes(o: Any?): ByteArray? {
        return if (o is String) {
            StringUtils.toByteArray(o, true)
        } else {
            o as ByteArray?
        }
    }
}
