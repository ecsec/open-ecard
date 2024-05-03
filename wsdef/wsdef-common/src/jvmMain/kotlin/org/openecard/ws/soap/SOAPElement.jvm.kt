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
package org.openecard.ws.soap

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*
import javax.xml.namespace.QName

/**
 *
 * @author Tobias Wich
 */
open class SOAPElement protected constructor(protected val element: Element) {
    val childElements: List<Element>
        get() {
            val result = mutableListOf<Element>()
            val nodes = element.childNodes
            for (i in 0 until nodes.length) {
                val n = nodes.item(i)
                if (Node.ELEMENT_NODE == n.nodeType) {
                    result.add(n as Element)
                }
            }
            return result.toList()
        }

    @Throws(SOAPException::class)
    fun addChildElement(parent: Element, elementName: QName): Element {
        val doc = element.ownerDocument
        // check if the document is the same
        if (doc !== parent.ownerDocument) {
            throw SOAPException("Given nodes have different owner documents.")
        }

        val e = doc.createElementNS(elementName.namespaceURI, elementName.localPart)
        return parent.appendChild(e) as Element
    }

    @Throws(SOAPException::class)
    fun addChildElement(elementName: QName): Element {
        return addChildElement(element, elementName)
    }
}
