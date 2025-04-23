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
 ***************************************************************************/
package org.openecard.gui.graphics

import org.testng.Assert
import org.testng.annotations.Test

/**
 * Test of GraphicsUtil class.
 *
 * @author Johannes Schmölz
 */
class GraphicsUtilTest {
	/**
	 * Test of createImage method, of class GraphicsUtil.
	 */
	@Test
	fun testCreateImage_3args() {
		println("createImage")

		//
		// valid values for imageWidth and imageHeight
		//
		var result = GraphicsUtil.createImage(OecLogo::class.java, 200, 200)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 150, 220)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 370, 280)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 400, 400)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 1, 1)
		Assert.assertNotNull(result)

		//
		// invalid values for imageWidth and/or imageHeight
		//
		try {
			GraphicsUtil.createImage(OecLogo::class.java, 0, 0)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, -300, 0)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, 0, -150)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, -100, 30)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, 50, -70)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}
	}

	/**
	 * Test of createImage method, of class GraphicsUtil.
	 */
	@Test
	fun testCreateImage_7args() {
		println("createImage")

		//
		// the image resides within the canvas; all values are valid
		//
		var result = GraphicsUtil.createImage(OecLogo::class.java, 100, 100, 200, 200, 25, 25)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 150, 120, 260, 240, 0, 10)
		Assert.assertNotNull(result)

		//
		// the image doesn't reside completely within the canvas or it is beyond the canvas; all values are valid
		//
		result = GraphicsUtil.createImage(OecLogo::class.java, 200, 200, 100, 100, 0, 0)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, 200, 200, 400, 400, -300, -250)
		Assert.assertNotNull(result)

		result = GraphicsUtil.createImage(OecLogo::class.java, -10, -5, 100, 100, 20, 20)
		Assert.assertNotNull(result)

		//
		// invalid values for canvasWidth and/or canvasHeight
		//
		try {
			GraphicsUtil.createImage(OecLogo::class.java, 50, 50, 0, 0, 0, 0)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, -35, 17, -80, 0, 20, 20)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}

		try {
			GraphicsUtil.createImage(OecLogo::class.java, 100, 120, 0, -1, 0, 0)
			Assert.fail("IllegalArgumentException expected.")
		} catch (ex: IllegalArgumentException) {
			// do nothing; exception expected
		}
	}
}
