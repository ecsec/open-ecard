/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.homeConfigDir
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.ws.common.OverridingProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

private val LOG = KotlinLogging.logger { }

/**
 * Openecard properties class.
 * These properties are loaded from the following places. The first occurrence of a key value pair overrides the next.
 *
 *  1. System Properties
 *  1. $HOME/.openecard/openecard.properties
 *  1. $classpath/openecard_config/openecard.properties
 *
 *
 * @author Tobias Wich
 */
object OpenecardProperties {
	private var properties: OverridingProperties? = null

	init {
		load()
	}

	/**
	 * Load properties from application bundle and disc.
	 */
	@Synchronized
	fun load() {
		val homeProps = loadHomeProps()
		var bundledProps: InputStream? = null

		try {
			val fileName = "openecard_config/openecard.properties"
			bundledProps = resolveResourceAsStream(OpenecardProperties::class.java, fileName)
		} catch (ex: IOException) {
			LOG.info(ex) { "Failed to load properties from config dir." }
		}
		try {
			properties = OverridingProperties(bundledProps!!, homeProps)
		} catch (ex: IOException) {
			// in that case a null pointer occurs when properties is accessed
			LOG.error(ex) { "${ex.message}" }
		}
	}

	private fun loadHomeProps(): InputStream? {
		try {
			val fileName = "openecard_test.properties"
			val testProps =
				resolveResourceAsStream(
					OpenecardProperties::class.java,
					fileName,
				)
			if (testProps != null) {
				return testProps
			}
		} catch (ex: IOException) {
			LOG.info(ex) { "Failed to load test properties from resources." }
		}

		try {
			val homePath = homeConfigDir
			val cfgFile = File(homePath, "openecard.properties")
			val homeProps: InputStream = FileInputStream(cfgFile)
			return homeProps
		} catch (ex: FileNotFoundException) {
			LOG.debug { "No properties file to load as it does not exist." }
			return null
		} catch (ex: IOException) {
			LOG.warn(ex) { "Failed to load bundled properties." }
			return null
		}
	}

	private fun saveHomeProps(homeProps: Properties) {
		val homePath = homeConfigDir
		val cfgFile = File(homePath, "openecard.properties")
		val out: OutputStream = FileOutputStream(cfgFile)
		homeProps.store(out, null)
		out.close()
	}

	/**
	 * @see OverridingProperties.getProperty
	 */
	@JvmStatic
	fun getProperty(key: String): String? = properties!!.getProperty(key)

	/**
	 * @see OverridingProperties.properties
	 */
	@JvmStatic
	fun properties(): Properties = properties!!.properties()

	/**
	 * Writes the changes, not the defaults in the given Properties instance to the user's config file.
	 * This function preserves the property values already present in the config file.
	 *
	 * @param changes Properties to be written.
	 * @throws IOException Thrown in case there was a problem reading or writing the config file.
	 */
	@JvmStatic
	@Synchronized
	fun writeChanges(changes: Properties) {
		// load currently written properties
		val homeProps = Properties()
		val homeStream = loadHomeProps()
		if (homeStream != null) {
			homeProps.load(homeStream)
			homeStream.close()
		}

		for ((key, value) in changes) {
			homeProps[key] = value
		}

		saveHomeProps(homeProps)
	}
}
