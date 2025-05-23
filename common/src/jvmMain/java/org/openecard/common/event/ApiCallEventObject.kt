/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.common.event

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.RequestType
import iso.std.iso_iec._24727.tech.schema.ResponseType
import org.openecard.common.util.JAXBUtils.deepCopy
import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.marshal.WSMarshallerException

/**
 *
 * @author Tobias Wich
 * @param <Request>
 * @param <Response>
</Response></Request> */
class ApiCallEventObject<Request : RequestType?, Response : ResponseType?>(
	handle: ConnectionHandleType?,
	req: Request,
) : EventObject(handle) {
	val request: Request
	protected var res: Response? = null

	init {
		this.request = copyMessage(req)
	}

	private fun <T> copyMessage(msg: T): T {
		try {
			return deepCopy(msg!!)
		} catch (ex: MarshallingTypeException) {
			throw RuntimeException("The requested type is not supported by the marshaller.", ex)
		} catch (ex: WSMarshallerException) {
			throw RuntimeException("Error initializing the marshaller.", ex)
		}
	}

	var response: Response?
		get() = res
		set(res) {
			this.res = copyMessage<Response>(res!!)
		}

	fun hasResponse(): Boolean = response != null
}
