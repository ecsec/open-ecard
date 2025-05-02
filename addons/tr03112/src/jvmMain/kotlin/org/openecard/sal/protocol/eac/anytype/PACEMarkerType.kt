/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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
package org.openecard.sal.protocol.eac.anytype

import iso.std.iso_iec._24727.tech.schema.KeyRefType
import iso.std.iso_iec._24727.tech.schema.PACEMarkerType
import org.openecard.common.util.StringUtils

/**
 * Convenience class to convert [PACEMarkerType] with AnyTypes to a type
 * with attributes instead.
 *
 * @author Dirk Petrautzki
 */
class PACEMarkerType(paceMarker: PACEMarkerType) {
    private var passwordRef: KeyRefType? = null
    var passwordValue: String? = null
        private set
    private var minLength: Int? = null
    private var maxLength: Int? = null
    var protocol: String?

    /**
     * Create a new PACEMarkerType from a [PACEMarkerType] as base.
     *
     * @param paceMarker the PACEMarkerType to convert
     */
    init {
        protocol = paceMarker.getProtocol()
        for (elem in paceMarker.getAny()) {
            if (elem.getLocalName() == "PasswordRef") {
                passwordRef = KeyRefType()
                val algorithmInfoNodes = elem.getChildNodes()
                for (i in 0..<algorithmInfoNodes.getLength()) {
                    val node = algorithmInfoNodes.item(i)
                    if (node.getLocalName() == "KeyRef") {
                        passwordRef!!.setKeyRef(StringUtils.toByteArray(node.getTextContent()))
                    } else if (node.getLocalName() == "Protected") {
                        passwordRef!!.setProtected(node.getTextContent().toBoolean())
                    }
                }
            } else if (elem.getLocalName() == "PasswordValue") {
                passwordValue = elem.getTextContent()
            } else if (elem.getLocalName() == "minLength") {
                minLength = elem.getTextContent().toInt()
            } else if (elem.getLocalName() == "maxLength") {
                maxLength = elem.getTextContent().toInt()
            } else if (elem.getLocalName() == "StateInfo") {
                // TODO
            }
        }
    }

    /**
     * Returns the maximum length for this password type, or `Integer.MAX_VALUE` as default if none was set.
     *
     * @return the maximum length for this password type
     */
    fun getMaxLength(): Int {
        if (maxLength != null) {
            return maxLength!!
        } else {
            return Int.Companion.MAX_VALUE
        }
    }

    /**
     * Returns the minimum length for this password type, or 0 as default if none was set.
     *
     * @return the minimum length for this password type
     */
    fun getMinLength(): Int {
        if (minLength != null) {
            return minLength!!
        } else {
            return 0
        }
    }

    fun getPasswordRef(): KeyRefType {
        return passwordRef!!
    }
}
