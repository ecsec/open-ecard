/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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
package org.openecard.common.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.namespace.QName

/**
 *
 * @author Tobias Wich
 * @param <T> Specialized type of the DIDAuthenticationData.
</T> */
class AuthDataResponse<T : DIDAuthenticationDataType?>(private val xmlDoc: Document, val response: T) {
    fun addElement(qname: QName, data: String?): Element {
        val e = if (qname.namespaceURI != null) {
            xmlDoc.createElementNS(qname.namespaceURI, qname.localPart)
        } else {
            xmlDoc.createElement(qname.localPart)
        }
        e.textContent = data
        // add to list and return
        response!!.any.add(e)
        return e
    }

    fun addElement(ns: String?, localName: String, data: String?): Element {
        return addElement(QName(ns, localName), data)
    }

    fun addElement(localName: String, data: String?): Element {
        return addElement(QName(ISO_NS, localName), data)
    }

    fun addAttribute(qname: QName?, data: String?) {
        response!!.otherAttributes[qname] = data
    }

    fun addAttribute(ns: String?, localName: String, data: String?) {
        addAttribute(QName(ns, localName), data)
    }

    fun addAttribute(localName: String, data: String?) {
        addAttribute(QName(null, localName), data)
    }

    companion object {
        const val ISO_NS: String = "urn:iso:std:iso-iec:24727:tech:schema"
        const val OEC_NS: String = "https://openecard.org/app"
    }
}
