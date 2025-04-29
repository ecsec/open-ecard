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

import org.openecard.common.tlv.TLV

/**
 *
 * @author Tobias Wich
 */
class EF_OD(tlv: TLV) {
    private val content: MutableList<CIOChoice>

    init {
        val tlvList = tlv.asList()
        content = ArrayList(tlvList.size)
        for (next in tlvList) {
            content.add(CIOChoice(next!!))
        }
    }

    fun getContent(): List<CIOChoice> {
        return content
    }
}
