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
 ***************************************************************************/

package org.openecard.addon.bind

import java.io.StringWriter

/**
 * Result of a Plug-In invocation.
 * This class contains everything the binding needs to create a binding specific response to the invoker.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
open class BindingResult(
	var resultCode: BindingResultCode = BindingResultCode.OK,
) {
	var body: ResponseBody? = null
	var resultMessage: String? = null
		private set
	private val parameters: MutableMap<String, String?> = mutableMapOf()
	private val auxData: MutableMap<String, String> = mutableMapOf()

	val attachments: List<Attachment> = mutableListOf()

	fun getParameters(): Map<String, String?> = this.parameters

	fun addParameter(
		key: String,
		value: String?,
	): BindingResult {
		parameters[key] = value
		return this
	}

	fun addParameters(parameters: Map<String, String?>?): BindingResult {
		this.parameters.putAll(parameters!!)
		return this
	}

	fun removeParameter(key: String): String? = parameters.remove(key)

	val auxResultData: MutableMap<String, String>
		get() = this.auxData

	fun addAuxResultData(
		key: String,
		value: String?,
	): BindingResult {
		value?.let {
			auxData[key] = it
		}
		return this
	}

	fun setResultMessage(resultMessage: String?): BindingResult {
		this.resultMessage = resultMessage
		return this
	}

	override fun toString(): String {
		val w = StringWriter()
		// Header
		w.write("BindingResult <")
		w.write(resultCode.name)
		w.write(", ")
		w.write(if (resultMessage == null) "" else "'")
		w.write(resultMessage!!)
		w.write(if (resultMessage == null) "" else "'")
		w.write(">\n")
		// Parameter
		val params = getParameters()
		printMap(w, "  ", "Parameters", params)
		// AuxData
		val aux = auxResultData
		printMap(w, "  ", "AuxResultData", aux)
		// Body
		val b: Body? = body
		if (b != null) {
			w.append("  Body type: ").append(body!!.mimeType).append("\n")
		}
		// Attachments
		val atts = attachments
		for (a in atts) {
			w.append("  Attachment with type: ").append(a.mIMEType).append("\n")
		}
		// done
		return w.toString()
	}

	private fun <V> printMap(
		w: StringWriter,
		prefix: String,
		identifier: String,
		map: Map<String, V>,
	) {
		if (map.isNotEmpty()) {
			w.append(prefix).append(identifier).append(" {\n")
			for ((key, v) in map) {
				w
					.append(prefix)
					.append(prefix)
					.append("'")
					.append(key)
					.append("': ")
				if (v is String) {
					w.append("'").append(v.toString()).append("',\n")
				} else {
					val s = v?.toString() ?: "null"
					w.append(s).append(",\n")
				}
			}
			w.append(prefix).append("}\n")
		}
	}
}
