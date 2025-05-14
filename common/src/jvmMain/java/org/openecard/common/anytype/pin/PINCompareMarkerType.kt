/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
package org.openecard.common.anytype.pin

import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType
import iso.std.iso_iec._24727.tech.schema.KeyRefType
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import iso.std.iso_iec._24727.tech.schema.StateInfoType
import org.openecard.common.util.StringUtils
import org.w3c.dom.Node
import java.math.BigInteger

/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class PINCompareMarkerType(
	didAbstractMarkerType: DIDAbstractMarkerType,
) {
	var pINRef: KeyRefType? = null
		private set
	var pINValue: String? = null
		private set
	var passwordAttributes: PasswordAttributesType? = null
		private set
	var protocol: String
		private set

	init {
		protocol = didAbstractMarkerType.protocol

		require(protocol == "urn:oid:1.3.162.15480.3.0.9")

		for (e in didAbstractMarkerType.any) {
			if (e.localName == "PinRef") {
				pINRef = KeyRefType()
				val nodeList = e.childNodes

				for (i in 0..<nodeList.length) {
					val n = nodeList.item(i)

					if (n.nodeType != Node.ELEMENT_NODE) {
						continue
					} else if (n.localName == "KeyRef") {
						pINRef!!.keyRef = StringUtils.toByteArray(n.textContent)
					} else if (n.localName == "Protected") {
						pINRef!!.isProtected = n.textContent.toBoolean()
					}
				}
			} else if (e.localName == "PinValue") {
				pINValue = e.textContent
			} else if (e.localName == "PasswordAttributes") {
				passwordAttributes = PasswordAttributesType()
				val nodeList = e.childNodes

				for (i in 0..<nodeList.length) {
					val n = nodeList.item(i)

					if (n.nodeType != Node.ELEMENT_NODE) {
						continue
					} else if (n.localName == "pwdFlags") {
						passwordAttributes!!.pwdFlags.addAll(
							n.textContent
								.split(" ".toRegex())
								.dropLastWhile { it.isEmpty() },
						)
					} else if (n.localName == "pwdType") {
						passwordAttributes!!.pwdType = PasswordTypeType.fromValue(n.textContent)
					} else if (n.localName == "minLength") {
						passwordAttributes!!.minLength = BigInteger(n.textContent)
					} else if (n.localName == "maxLength") {
						passwordAttributes!!.maxLength = BigInteger(n.textContent)
					} else if (n.localName == "storedLength") {
						passwordAttributes!!.storedLength = BigInteger(n.textContent)
					} else if (n.localName == "padChar") {
						passwordAttributes!!.padChar = StringUtils.toByteArray(n.textContent)
					}
				}
			} else if (e.localName == "StateInfo") {
				// TODO
			}
		}
	}

	val stateInfo: StateInfoType
		get() {
			throw UnsupportedOperationException("Not yet implemented")
		}
}
