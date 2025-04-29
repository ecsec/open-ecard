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
import java.util.*

/**
 *
 * @author Tobias Wich
 */
open class TLVList {
    protected val tlv: TLV

    protected constructor(tlv: TLV, expectedTag: Tag) : this(tlv, expectedTag.tagNumWithClass)

    @JvmOverloads
    constructor(tlv: TLV, expectedTagNum: Long = 0x61) {
        if (tlv.tagNumWithClass != expectedTagNum) {
            throw TLVException("Not of type TLVList.")
        }
        this.tlv = tlv
    }

    constructor(children: List<TLV>) {
        tlv = TLV()
        tlv.setTagNumWithClass(Tag.Companion.SEQUENCE_TAG.getTagNumWithClass())
        // link in children
        if (!children.isEmpty()) {
            val first = children[0]
            tlv.child = first
            for (i in 1..<children.size) {
                first.addToEnd(children[i])
            }
        }
    }

    constructor(data: ByteArray?) : this(TLV.Companion.fromBER(data))


    val content: List<TLV?>
        get() {
            if (tlv.hasChild()) {
                return tlv.child!!.asList()
            }
            return LinkedList()
        }
}
