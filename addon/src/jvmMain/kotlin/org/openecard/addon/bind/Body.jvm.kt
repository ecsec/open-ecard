/****************************************************************************
 * Copyright (C) 2013-2024 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

/**
 * Base for different Body types.
 * A body instance contains a value and a MIME type. Request or response specific values are added by the respective
 * subclasses.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
abstract class Body
/**
	 * Creates a body with the given content.
	 *
	 * @param value Value to use as the bodies content, or `null` if the body should be empty.
	 * @param encoding Encoding of the value if applicable.
	 * @param mimeType MIME type of the value or, `null` if the value is `null`.
	 */
	protected constructor(
		/**
		 * Gets the value of this body instance.
		 *
		 * @return The value, or `null` if no value is set.
		 */
		@JvmField var value: ByteArray? = null,
		/**
		 * Gets the encoding of the
		 * @return
		 */
		@JvmField var encoding: Charset? = null,
		/**
		 * Gets the MIME type of this body's value.
		 *
		 * @return The MIME type, or `null` if no value and thus no MIME type is set.
		 */
		@JvmField var mimeType: String? = null,
	) {
		protected constructor(value: String, encoding: Charset?, mimeType: String?) : this() {
			setValue(value, encoding, mimeType)
		}

		/**
		 * Checks whether this instance contains a body value or not.
		 *
		 * @return `true` if the instance contains a value, `false` otherwise.
		 */
		fun hasValue(): Boolean = value != null

		fun hasStringValue(): Boolean = value != null && encoding != null

		val valueString: String?
			/**
			 * Gets the value of the body as a string.
			 * This method only returns a value if this body is representable by a string.
			 *
			 * @return The value of the string if it is set, `null` otherwise.
			 */
			get() {
				return value?.let { v ->
					encoding?.let { e ->
						String(v, e)
					}
				}
			}

		/**
		 * Sets the value of the body.
		 *
		 * @param value The value to be set in the body. `null` values are permitted.
		 * @param encoding The encoding of the value, or `null` if not used.
		 * @param mimeType The MIME type of the value.  `null` values are permitted.
		 */
		fun setValue(
			value: ByteArray?,
			encoding: Charset?,
			mimeType: String?,
		) {
			if (value == null) {
				this.encoding = null
				this.mimeType = null
			} else {
				this.encoding = encoding
				this.mimeType = mimeType
			}

			this.value = value
		}

		/**
		 * Sets the value of the body.
		 *
		 * @param value The value to be set in the body. `null` values are permitted.
		 * @param encoding The encoding of the value, or `null` if not used.
		 * @param mimeType The MIME type of the value.  `null` values are permitted.
		 */
		fun setValue(
			value: String?,
			encoding: Charset?,
			mimeType: String?,
		) {
			// use default for mime type if the value is omitted
			if (value == null) {
				setValue(null as ByteArray?, null, null)
			} else {
				val tmpMimeType =
					if (mimeType.isNullOrEmpty()) {
						logger.warn { "No MIME type specified, falling back to 'text/plain'." }
						"text/plain"
					} else {
						mimeType
					}
				val tmpEncoding =
					if (encoding == null) {
						logger.warn { "No encoding specified, using UTF-8." }
						Charsets.UTF_8
					} else {
						encoding
					}
				setValue(value.toByteArray(tmpEncoding), tmpEncoding, tmpMimeType)
			}
		}

		/**
		 * Sets the value of the body.
		 * A MIME type of `text/plain` is assumed.
		 *
		 * @param value The value to be set in the body. `null` values are permitted.
		 */
		fun setValue(value: String?) {
			setValue(value, Charsets.UTF_8, "text/plain")
		}

		/**
		 * Sets the value of the body.
		 *
		 * @param value The value to be set in the body. `null` values are permitted.
		 * @param mimeType The MIME type of the value.  `null` values are permitted.
		 */
		fun setValue(
			value: String?,
			mimeType: String?,
		) {
			setValue(value, Charsets.UTF_8, mimeType)
		}

		/**
		 * Sets the value of the body.
		 *
		 * @param value The value to be set in the body.
		 * @param mimeType The MIME type of the value.  `null` values are permitted.
		 */
		fun setValue(
			value: ByteArray?,
			mimeType: String?,
		) {
			setValue(value, null, mimeType)
		}
	}

/**
 * Request specific Body for use in Plug-Ins and Bindings.
 * Additionally to the base elements, a request contains the requested resource.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class RequestBody(
	/**
	 * Resource path of the request.
	 */
	val path: String,
) : Body() {
	/**
	 * Get the resource path of this request.
	 *
	 * @return A String containing the requests resource path.
	 */
	val formParamValue: Map<String, String?>
		get() {
			val utf8 = StandardCharsets.UTF_8
			val result: MutableMap<String, String?> = mutableMapOf()
			val value = valueString?.trim { it <= ' ' } ?: ""

			val entries = value.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			for (entry in entries) {
				val split = entry.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				try {
					if (split.size == 1) {
						val key = URLDecoder.decode(split[0], utf8.name())
						result[key] = null
					} else if (split.size == 2) {
						val key = URLDecoder.decode(split[0], utf8.name())
						val v = URLDecoder.decode(split[1], utf8.name())
						result[key] = v
					}
				} catch (ex: UnsupportedEncodingException) {
					// UTF8 is supported everywhere
					throw RuntimeException("UTF-8 is unsupported on this platform.", ex)
				}
			}

			return result
		}
}

/**
 * Response specific Body for use in Plug-Ins and Bindings.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class ResponseBody : Body {
	constructor() : super()

	constructor(value: String, encoding: Charset?, mimeType: String?) : super(value, encoding, mimeType)
}
