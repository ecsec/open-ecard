/****************************************************************************
 * Copyright (C) 2019-2025 ecsec GmbH.
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

package org.openecard.addons.status

import dev.icerock.moko.resources.format
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addon.bind.ResponseBody
import org.openecard.i18n.I18N
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.openecard.ws.schema.Status
import org.openecard.ws.schema.StatusChange
import javax.xml.transform.TransformerException

/**
 * Specialized ResponseBody capable of marshalling wait for change messages.
 *
 * @author Tobias Wich
 */
class StatusResponseBodyFactory {
	private val m: WSMarshaller = createInstance()

	fun createStatusResponse(status: Status): BindingResult = createBindingResult(status)

	fun createWaitForChangeResponse(status: StatusChange?): BindingResult =
		status?.let {
			createBindingResult(it)
		} ?: BindingResult(
			BindingResultCode.RESOURCE_UNAVAILABLE,
			"The requested session does not exist.",
		)

	private fun createBindingResult(toBeMarshalled: Any): BindingResult =
		try {
			BindingResult.OK.apply {
				body =
					ResponseBody().apply {
						setValue(
							m.doc2str(m.marshal(toBeMarshalled)),
							"text/xml",
						)
					}
			}
		} catch (ex: Exception) {
			when (ex) {
				is WSMarshallerException, is TransformerException -> {
					BindingResult(
						BindingResultCode.INTERNAL_ERROR,
						I18N.strings.addon_error_internal
							.format("Failed to marshal Status message.\n ${ex.message}")
							.localized(),
					)
				}
				else -> {
					throw ex
				}
			}
		}
}
