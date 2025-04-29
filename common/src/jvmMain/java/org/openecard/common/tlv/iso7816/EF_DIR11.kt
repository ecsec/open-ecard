/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.apdu.utils.CardUtils
import org.openecard.common.interfaces.*
import org.openecard.common.tlv.*
import java.util.*

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class EF_DIR(tlv: TLV?) {
    private val tlv = TLV()

    private var applicationIdentifiers: MutableList<ByteArray?>
    private var applicationTemplates: MutableList<ApplicationTemplate>

    init {
        this.tlv.setTagNumWithClass(Tag.Companion.SEQUENCE_TAG.getTagNumWithClass()) // pretend to be a sequence
        this.tlv.child = tlv

        val p = Parser(this.tlv.child)
        applicationIdentifiers = LinkedList()
        applicationTemplates = LinkedList()
        while (p.match(0x61) || p.match(0x4F)) {
            if (p.match(0x61)) {
                applicationTemplates.add(ApplicationTemplate(p.next(0)!!))
            } else if (p.match(0x4F)) {
                applicationIdentifiers.add(p.next(0).value)
            }
        }
        if (p.next(0) != null) {
            throw TLVException("Unrecognised element in EF.DIR.")
        }
    }

    constructor(data: ByteArray?) : this(TLV.Companion.fromBER(data))

    val applicationIds: List<ByteArray?>
        get() = applicationIdentifiers

    fun getApplicationTemplates(): List<ApplicationTemplate> {
        return applicationTemplates
    }

    companion object {
        private val EF_DIR_FID = byteArrayOf(0x2F, 0x00)

        @Throws(APDUException::class, TLVException::class)
        fun selectAndRead(dispatcher: Dispatcher, slotHandle: ByteArray?): EF_DIR {
            // Select and read EF.DIR
            val data = CardUtils.selectReadFile(dispatcher, slotHandle, EF_DIR_FID)

            return EF_DIR(data)
        }
    }
}
