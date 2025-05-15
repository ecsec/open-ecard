/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
import org.testng.Assert
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

/**
 *
 * @author Hans-Martin Haase
 */
class AddonPropertiesTest {
	private var addonsExists = false
	private var path: File? = null
	private val addonSpec = AddonSpecification()

	/**
	 * Initialize a Context object just for testing purpose.
	 * @throws IOException
	 */
	@BeforeTest
	@Throws(IOException::class)
	fun init() {
		addonSpec.setId("test-addon")
		path = File(addonsConfDir, addonSpec.getId())
		if (!path!!.exists()) {
			path!!.mkdirs()
			addonsExists = false
		} else {
			addonsExists = true
		}
	}

	/**
	 * Test the set and get method.
	 */
	@Test
	fun testSetAndGet() {
		val props = AddonProperties(addonSpec)
		props.setProperty(NAME_KEY, NAME)
		props.setProperty(VERSION_KEY, VERSION)
		props.setProperty(URL_KEY, URL)

		Assert.assertEquals(props.getProperty(NAME_KEY), NAME)
		Assert.assertEquals(props.getProperty(VERSION_KEY), VERSION)
		Assert.assertEquals(props.getProperty(URL_KEY), URL)
	}

	/**
	 * Test the save and load method.
	 */
	@Test
	fun testLoadAndSave() {
		val props = AddonProperties(addonSpec)
		props.setProperty(NAME_KEY, NAME)
		props.setProperty(VERSION_KEY, VERSION)
		props.setProperty(URL_KEY, URL)

		try {
			props.saveProperties()
		} catch (ex: AddonPropertiesException) {
			logger.error("Properties not stored.", ex)
		}

		val props2 = AddonProperties(addonSpec)
		try {
			props2.loadProperties()
		} catch (ex: AddonPropertiesException) {
			logger.error("Properties not loaded", ex)
		}

		Assert.assertEquals(props2.getProperty(NAME_KEY), NAME)
		Assert.assertEquals(props2.getProperty(VERSION_KEY), VERSION)
		Assert.assertEquals(props2.getProperty(URL_KEY), URL)
	}

	/**
	 * Remove created folders and files
	 * @throws IOException
	 */
	@AfterSuite
	@Throws(IOException::class)
	fun clear() {
		if (!addonsExists) {
			File(addonsConfDir.path + "/test-addon.conf").delete()
			path!!.delete()
		} else {
			File(addonsConfDir.path + "/test-addon.conf").delete()
		}
	}

	companion object {
		private val logger: Logger = LoggerFactory.getLogger(AddonPropertiesTest::class.java)

		private const val NAME_KEY = "name"
		private const val NAME = "test-addon"
		private const val VERSION_KEY = "version"
		private const val VERSION = "1.0.0"
		private const val URL_KEY = "url"
		private const val URL = "http://www.test-addon.com"
	}
}
