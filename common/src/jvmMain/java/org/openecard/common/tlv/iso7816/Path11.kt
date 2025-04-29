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
class Path(private val tlv: TLV) {
    private var efIdOrPath: ByteArray?

    // optional
    var index: Int?
        private set

    // optional
    var length: Int?
        private set

    init {
        val p = Parser(tlv.child)

        if (p.match(Tag(TagClass.UNIVERSAL, true, 4))) {
            efIdOrPath = p.next(0).value
        } else {
            throw TLVException("No efIdOrPath given.")
        }
        index = null
        length = null
        if (p.match(Tag(TagClass.UNIVERSAL, true, 2)) && p.matchLA(1, Tag(TagClass.CONTEXT, true, 0))) {
            index = toInteger(p.next(0).value)
            length = toInteger(p.next(0).value)
        }
    }

    constructor(data: ByteArray?) : this(TLV.Companion.fromBER(data))


    fun efIdOrPath(): ByteArray? {
        return efIdOrPath
    }
}
