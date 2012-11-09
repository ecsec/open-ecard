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

package org.openecard.client.gui.graphics;

import java.awt.Image;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 * Test of GraphicsUtil class.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class GraphicsUtilTest {

    /**
     * Test of createImage method, of class GraphicsUtil.
     */
    @Test
    public void testCreateImage_3args() {
	System.out.println("createImage");

	//
	// valid values for imageWidth and imageHeight
	//

	Image result = GraphicsUtil.createImage(OecLogoBgWhite.class, 200, 200);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgWhite.class, 150, 220);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgWhite.class, 370, 280);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgTransparent.class, 400, 400);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgTransparent.class, 1, 1);
	assertNotNull(result);

	//
	// invalid values for imageWidth and/or imageHeight
	//

	try {
	    GraphicsUtil.createImage(OecLogoBgWhite.class, 0, 0);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgWhite.class, -300, 0);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgWhite.class, 0, -150);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgTransparent.class, -100, 30);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgTransparent.class, 50, -70);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}
    }

    /**
     * Test of createImage method, of class GraphicsUtil.
     */
    @Test
    public void testCreateImage_7args() {
	System.out.println("createImage");

	//
	// the image resides within the canvas; all values are valid
	//

	Image result = GraphicsUtil.createImage(OecLogoBgWhite.class, 100, 100, 200, 200, 25, 25);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgTransparent.class, 150, 120, 260, 240, 0, 10);
	assertNotNull(result);

	//
	// the image doesn't reside completely within the canvas or it is beyond the canvas; all values are valid
	//

	result = GraphicsUtil.createImage(OecLogoBgTransparent.class, 200, 200, 100, 100, 0, 0);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgWhite.class, 200, 200, 400, 400, -300, -250);
	assertNotNull(result);

	result = GraphicsUtil.createImage(OecLogoBgTransparent.class, -10, -5, 100, 100, 20, 20);
	assertNotNull(result);

	//
	// invalid values for canvasWidth and/or canvasHeight
	//

	try {
	    GraphicsUtil.createImage(OecLogoBgTransparent.class, 50, 50, 0, 0, 0, 0);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgTransparent.class, -35, 17, -80, 0, 20, 20);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}

	try {
	    GraphicsUtil.createImage(OecLogoBgWhite.class, 100, 120, 0, -1, 0, 0);
	    fail("IllegalArgumentException expected.");
	} catch (IllegalArgumentException ex) {
	    // do nothing; exception expected
	}
    }

}
