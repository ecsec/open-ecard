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

/**
 *
 * @author Tobias Wich
 */
class CIODDO(tlv: TLV) {
    private val tlv: TLV

    var providerId: ByteArray?
        private set
    var odfPath: Path?
        private set
    var cIAInfoPath: Path? = null
        private set
    var applicationIdentifier: ByteArray?
        private set

    init {
        if (tlv.tagNumWithClass != 0x73L) {
            throw TLVException("Not of type CIODDO.")
        }
        this.tlv = tlv

        val p = Parser(tlv.child)
        // provider id
        providerId = null
        if (p.match(Tag(TagClass.UNIVERSAL, true, 6))) {
            providerId = p.next(0).value
        }
        // odf path
        odfPath = null
        if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
            odfPath = Path(p.next(0)!!)
        }
        // cia info
        if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            cIAInfoPath = Path(p.next(0)!!)
        }
        // app id
        if (p.match(Tag(TagClass.APPLICATION, true, 15))) {
            applicationIdentifier = p.next(0).value
        }
    }

    constructor(data: ByteArray?) : this(TLV.Companion.fromBER(data))


    fun hasProviderId(): Boolean {
        return providerId != null
    }

    fun hasOdfPath(): Boolean {
        return odfPath != null
    }

    fun hasCIAInfoPath(): Boolean {
        return cIAInfoPath != null
    }

    fun hasApplicationIdentifier(): Boolean {
        return applicationIdentifier != null
    }
}
