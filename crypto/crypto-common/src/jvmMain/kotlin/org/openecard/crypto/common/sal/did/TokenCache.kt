/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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
package org.openecard.crypto.common.sal.did

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteComparator
import java.util.TreeMap

/**
 * Simple cache for the DIDInfos entry point to card data.
 *
 * @author Tobias Wich
 */
class TokenCache(
	private val dispatcher: Dispatcher,
) {
	private val cachedInfos: MutableMap<ByteArray, DidInfos> = TreeMap<ByteArray, DidInfos>(ByteComparator())

	fun getInfo(
		pin: CharArray?,
		handle: ConnectionHandleType,
	): DidInfos {
		var result = cachedInfos[handle.getSlotHandle()]

		if (result == null) {
			result = DidInfos(dispatcher, pin, handle)
		} else if (pin != null) {
			result.setPin(pin)
		}

		return result
	}

	fun clearPins() {
		val it = cachedInfos.entries.iterator()
		while (it.hasNext()) {
			val next = it.next()
			val slotHandle = next.key
			val dids = next.value
			dids.clearPin(slotHandle)
		}
	}
}
