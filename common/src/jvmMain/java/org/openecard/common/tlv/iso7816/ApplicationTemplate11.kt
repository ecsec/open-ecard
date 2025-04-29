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

import org.openecard.common.tlv.*
import org.openecard.common.util.*
import java.util.*

/**
 *
 * @author Tobias Wich
 */
class ApplicationTemplate(tlv: TLV) : TLVList(tlv, 0x61) {
    var applicationIdentifier: ByteArray?
        private set
    var applicationLabel: ByteArray?
        private set
    var fileReference: ByteArray?
        private set
    var commandAPDU: ByteArray?
        private set
    var discretionaryData: ByteArray?
        private set
    var cIODDO: CIODDO?
        private set
    var uRL: ByteArray?
        private set
    var applicationDataObjects: TLVList?
        private set

    init {
        val p = Parser(tlv.child)
        // application identifier
        if (p.match(0x4F)) {
            applicationIdentifier = p.next(0).value
        } else {
            throw TLVException("No ApplicationIdentifier defined in ApplicationTemplate.")
        }
        // application label
        applicationLabel = null
        if (p.match(0x50)) {
            applicationLabel = p.next(0).value
        }
        // file reference
        fileReference = null
        if (p.match(0x51)) {
            fileReference = p.next(0).value
        }
        // command apdu
        commandAPDU = null
        if (p.match(0x52)) {
            commandAPDU = p.next(0).value
        }
        // discretionary data
        discretionaryData = null
        if (p.match(0x53)) {
            discretionaryData = p.next(0).value
        }
        // cioddo
        cIODDO = null
        if (p.match(0x73)) {
            if (discretionaryData != null) {
                throw TLVException("DiscretionaryData already defined. CIODDO is forbidden then.")
            }
            cIODDO = CIODDO(p.next(0)!!)
        }
        // url
        uRL = null
        if (p.match(0x5F50)) {
            uRL = p.next(0).value
        }
        // set of application dataobjects
        applicationDataObjects = null
        if (p.match(0x61)) {
            applicationDataObjects = TLVList(p.next(0)!!)
        }
    }

    constructor(data: ByteArray?) : this(TLV.Companion.fromBER(data))


    fun hasApplicationLabel(): Boolean {
        return applicationLabel != null
    }

    fun hasFileReference(): Boolean {
        return fileReference != null
    }

    fun hasCommandAPDU(): Boolean {
        return commandAPDU != null
    }

    fun hasDiscretionaryData(): Boolean {
        return discretionaryData != null
    }

    fun hasCIODDO(): Boolean {
        return cIODDO != null
    }

    fun hasURL(): Boolean {
        return uRL != null
    }

    fun hasApplicationDataObject(): Boolean {
        return applicationDataObjects != null
    }


    val isCiaAid: Boolean
        // useful definitions
        get() {
            var aid = ByteUtils.toHexString(applicationIdentifier)
            aid = aid!!.uppercase(Locale.getDefault())
            if (aid.startsWith("E828BD080F")) {
                // TODO: more to check, but ok for now
                return true
            } else if (aid == "A000000063504B43532D3135") {
                // historical cia aid
                return true
            }

            return false
        }
}
