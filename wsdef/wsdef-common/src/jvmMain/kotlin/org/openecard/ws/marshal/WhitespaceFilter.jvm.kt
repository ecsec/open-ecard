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
 ***************************************************************************/
package org.openecard.ws.marshal

import org.w3c.dom.Node
import java.util.*

/**
 *
 * @author Tobias Wich
 */
object WhitespaceFilter {
	fun filter(root: Node) {
		val childNodes = root.childNodes

		// remove afterwards else the nodelist indices may not be correct
		val toRemove = LinkedList<Node>()

		for (i in 0 until childNodes.length) {
			val next = childNodes.item(i)

			// only one textnode
			if ((next.nodeType == Node.TEXT_NODE) && childNodes.length == 1) {
				next.nodeValue = next.nodeValue.trim { it <= ' ' }
			} else if (next.nodeType == Node.TEXT_NODE) {
				val strippedData = next.nodeValue.trim { it <= ' ' }
				if (strippedData.isEmpty()) {
					toRemove.addFirst(next)
				}
			} else {
				filter(next)
			}
		}

		// remove all this bullshit
		for (n in toRemove) {
			root.removeChild(n)
		}
	}
}
