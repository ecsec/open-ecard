/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import java.util.logging.Level
import java.util.logging.Logger

/**
 *
 * @author Hans-Martin Haase
 */
class SecurityConditionChoice(tlv: TLV) : TLVType(tlv) {
    var isAlways: Boolean = false
    var authIdentifier: ByteArray?
        private set
    var authReference: AuthReference? = null
        private set
    var not: SecurityConditionChoice? = null
        private set
    var and: List<TLV?>? = null
        private set
    var or: List<TLV?>? = null
        private set

    init {
        val p = Parser(tlv.child)
        if (p.match(Tag(TagClass.UNIVERSAL, true, 5))) {
            isAlways = true
            p.next(0)
        }

        if (p.match(Tag(TagClass.UNIVERSAL, true, 4))) {
            authIdentifier = p.next(0).value
        }

        if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
            try {
                authReference = AuthReference(p.next(0)!!)
            } catch (ex: TLVException) {
                Logger.getLogger(SecurityConditionChoice::class.java.name).log(Level.SEVERE, null, ex)
                throw TLVException("Malformed authReference")
            }
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            not = SecurityConditionChoice(p.next(0)!!)
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            and = p.next(0)!!.asList()
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
            or = p.next(0)!!.asList()
        }
    }
}
