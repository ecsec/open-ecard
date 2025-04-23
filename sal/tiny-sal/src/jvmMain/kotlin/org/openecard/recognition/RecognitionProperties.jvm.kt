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
 ***************************************************************************/

package org.openecard.recognition

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.ws.common.OverridingProperties
import java.io.IOException
import java.util.*

private val LOG = KotlinLogging.logger {}

/**
 * Class loading properties with values for the Card Recognition module.
 * Take a look at the resource file `cardrecognition.properties` for a complete list of the available keys.
 *
 * @author Johannes Schmoelz
 * @author Tobias Wich
 */
object RecognitionProperties {
	private val properties = loadProperties()

	private fun loadProperties(): OverridingProperties {
		try {
			return OverridingProperties("cardrecognition.properties")
		} catch (ex: IOException) {
			// in that case a null pointer occurs when properties is accessed
			LOG.error(ex) { "Failed to load cardrecognition.properties file correctly." }
			throw IllegalStateException("Failed to load cardrecognition.properties file correctly.", ex)
		}
	}

	@JvmStatic
	fun getProperty(key: String): String? = properties.getProperty(key)

	@JvmStatic
	fun setProperty(
		key: String,
		value: String,
	): Any? = properties.setProperty(key, value)

	@JvmStatic
	fun properties(): Properties = properties.properties()

	@JvmStatic
	val action: String?
		get() = getProperty("org.openecard.recognition.action")

	@JvmStatic
	val serviceName: String?
		get() = getProperty("org.openecard.recognition.serviceName")

	@JvmStatic
	val serviceAddr: String?
		get() = getProperty("org.openecard.recognition.serviceAddr")
}
