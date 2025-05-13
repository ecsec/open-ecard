/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
package org.openecard.addon

import org.openecard.addon.manifest.AddonSpecification
import org.openecard.common.util.FileUtils.addonsConfDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Properties

/**
 * The class implements the AddonProperties which basically wrap a Properties object.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class AddonProperties(
	/**
	 * The specification of the addon.
	 */
	private val addonSpec: AddonSpecification,
) {
	/**
	 * The ID of the addon.
	 */
	private val id: String = addonSpec.getId()

	/**
	 * A File object identifying the configuration file of the addon.
	 */
	private var configFile: File? = null

	/**
	 * A Properties object which is used for managing the addon specific properties.
	 */
	private val props = Properties()

	/**
	 * Creates an AddonProperties object for the given AddonSpecification.
	 *
	 * @param addonSpec AddonSpecification object containing the required attributes to set.
	 */
	init {

		try {
			// create a pointer to the configuration file. The file is created on the first time store is called.
			configFile = File(addonsConfDir, "$id/$id$FILE_EXTENSION")
		} catch (ex: IOException) {
			logger.error("Couldn't create file object to the config directory.", ex)
		} catch (ex: SecurityException) {
			val msg = (
				"SecurityException seems like you do not have enough permissions to create a file object for " +
					"the config directory."
			)
			logger.error(msg, ex)
		}
	}

	/**
	 * Loads a existing configuration file.
	 *
	 *
	 * If there exists no file containing properties than nothing is loaded.
	 *
	 *
	 * @throws AddonPropertiesException Thrown if a conf file exists but can't be loaded.
	 */
	@Throws(AddonPropertiesException::class)
	fun loadProperties() {
		if (configFile!!.exists()) {
			try {
				props.load(FileInputStream(configFile))
			} catch (ex: IOException) {
				logger.error("Failed to load properties for the addon with the ID " + id + ".", ex)
				throw AddonPropertiesException("Failed to load properties for the addon with the ID " + id + ".", ex)
			}
		} else {
			logger.warn(
				(
					"Can't load the properties of the addon with the ID " + id + ". The file containing the " +
						"properties does not exist."
				),
			)
		}
	}

	/**
	 * Saves the currently set properties to a file.
	 *
	 *
	 * The file will be located in `$HOME/.openecard/addons`.
	 * The file name will be `<id>.conf`.
	 *
	 *
	 * @throws AddonPropertiesException Thrown if the conf file can't be found or an error occurred while saving
	 * the properties.
	 */
	@Throws(AddonPropertiesException::class)
	fun saveProperties() {
		try {
			// create add-on specific configuration directory
			val addonDir = File(addonsConfDir, id)
			if (!addonDir.exists()) {
				if (!addonDir.mkdirs()) {
					logger.error(id)
				}
			}
			val out: OutputStream = FileOutputStream(configFile, false)
			props.store(out, "Configuration file of the " + id + "addon.")
		} catch (ex: FileNotFoundException) {
			val msg = (
				"Failed to save the properties for the addon with the ID " + id + ". The file was not found." +
					"The settings aren't stored."
			)
			logger.error(msg, ex)
			throw AddonPropertiesException(msg, ex)
		} catch (ex: IOException) {
			val msg = (
				"Failed to save the properties for the addon with the ID " + id + ". The settings aren't " +
					"stored."
			)
			logger.error(msg, ex)
			throw AddonPropertiesException(msg, ex)
		}
	}

	/**
	 * Set a new property with the key **key** and the value **value**
	 *
	 * @param key A string representing the name of the property.
	 * @param value A string representing the value of the property.
	 */
	fun setProperty(
		key: String?,
		value: String?,
	) {
		props.setProperty(key, value)
	}

	/**
	 * Get a property by a key.
	 *
	 * @param key A string representing the name of the property.
	 * @return The value of property **key**. If the property does not exist **null** is returned.
	 */
	fun getProperty(key: String?): String? = props.getProperty(key)

	/**
	 * Delete the file containing the users configuration of the addon.
	 * This method should be called just in case the user uninstalls/removes the addon.
	 */
	fun removeConfiguration() {
		try {
			val confDir = File(addonsConfDir, id)
			removeDirectory(confDir)
		} catch (ex: IOException) {
			logger.error("Failed to get the Addon configuration directory.", ex)
		} catch (ex: SecurityException) {
			logger.error("Failed to access the Addon configuration directory.", ex)
		}
		configFile!!.delete()
	}

	/**
	 * Remove a directory recursively.
	 *
	 * @param directoryToRemove The directory to remove.
	 */
	private fun removeDirectory(directoryToRemove: File) {
		val files = directoryToRemove.list()
		for (item in files!!) {
			val fileItem = File(directoryToRemove, item)
			if (fileItem.isFile()) {
				fileItem.delete()
			} else {
				removeDirectory(fileItem)
				fileItem.delete()
			}
		}
	}

	companion object {
		/**
		 * Logger for logging errors and other necessary things.
		 */
		private val logger: Logger = LoggerFactory.getLogger(AddonProperties::class.java)

		/**
		 * File extension used for files if the saveProperties and loadProperties method is used.
		 */
		private const val FILE_EXTENSION = ".conf"
	}
}
