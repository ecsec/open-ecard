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
package org.openecard.control.binding.http.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.toString
import java.io.UnsupportedEncodingException

private val logger = KotlinLogging.logger {}

/**
 * @author Moritz Horsch
 */
class HTTPTemplate(
	documentRoot: DocumentRoot,
	templatePath: String,
) {
	private val properties = mutableMapOf<String, String>()
	private var content: String? = null

	/**
	 * Creates a new HTTPTemplate.
	 *
	 * @param documentRoot Document root
	 * @param templatePath Template path
	 */
	init {
		try {
			val url = documentRoot.getFile(templatePath)
			content = toString(url!!.openStream())
		} catch (e: Exception) {
			logger.error(e) { e.message }
		}
	}

	/**
	 * Sets a property of the template.
	 *
	 * @param key Key
	 * @param value Value
	 */
	fun setProperty(
		key: String,
		value: String,
	) {
		properties[key] = value
	}

	/**
	 * Removes a property.
	 *
	 * @param key Key
	 */
	fun removeProperty(key: String) {
		properties.remove(key)
	}

	@get:Throws(UnsupportedEncodingException::class)
	val bytes: ByteArray
		/**
		 * Returns the template as a byte array.
		 *
		 * @return Template as a byte array
		 * @throws UnsupportedEncodingException
		 */
		get() = toString().toByteArray(charset("UTF-8"))

	override fun toString(): String {
		val out = StringBuilder(content)
		for (key in properties.keys) {
			val i = out.indexOf(key)
			val j = i + key.length
			if (i > 0 && j > 0) {
				out.replace(i, j, properties[key])
			}
		}
		return out.toString()
	}
}
