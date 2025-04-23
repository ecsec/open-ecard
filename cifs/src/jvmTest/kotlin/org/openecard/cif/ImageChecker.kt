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
 ***************************************************************************/
package org.openecard.cif

import io.github.oshai.kotlinlogging.KotlinLogging
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.IOException
import java.net.URL
import java.util.Properties
import javax.imageio.ImageIO
import javax.swing.ImageIcon

private val logger = KotlinLogging.logger { }

// TODO: remove integration test as soon as OpenJDK 8 in Debian can handle JPEGs correctly
// see also https://dev.openecard.org/issues/369

/**
 * Test to evaluate the availability of the images.
 *
 * @author Tobias Wich
 */
@Test(groups = ["it"])
class ImageChecker {
	private lateinit var imageUrls: MutableList<URL>

	@BeforeClass
	@Throws(IOException::class)
	fun loadImageUrls() {
		// load properties file
		val `in` = ImageChecker::class.java.getResourceAsStream("/card-images/card-images.properties")
		val p = Properties()
		p.load(`in`)

		// convert each entry into a URL
		imageUrls = ArrayList<URL>()
		for (next in p.entries) {
			val file = next.value as String?
			val url = ImageChecker::class.java.getResource("/card-images/$file")
			imageUrls.add(url!!)
		}
	}

	@Test
	@Throws(IOException::class)
	fun testPresence() {
		for (url in imageUrls) {
			url.openStream()
			Assert.assertNotNull(url)
		}
	}

	@Test(dependsOnMethods = ["testPresence"])
	@Throws(IOException::class)
	fun testLoadImages() {
		for (url in imageUrls) {
			logger.info { "Trying to load image '$url'." }
			val bi = ImageIO.read(url)
			val icon = ImageIcon(bi)
		}
	}
}
