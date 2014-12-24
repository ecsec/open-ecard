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

package org.openecard.cif;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test to evaluate the availability of the images.
 *
 * @author Tobias Wich
 */
public class ImageChecker {

    private static final Logger logger = LoggerFactory.getLogger(ImageChecker.class);
    private List<URL> imageUrls;

    @BeforeClass
    public void loadImageUrls() throws IOException {
	// load properties file
	InputStream in = ImageChecker.class.getResourceAsStream("/card-images/card-images.properties");
	Properties p = new Properties();
	p.load(in);

	// convert each entry into a URL
	imageUrls = new ArrayList<>();
	for (Map.Entry<Object, Object> next : p.entrySet()) {
	    String file = (String) next.getValue();
	    URL url = ImageChecker.class.getResource("/card-images/" + file);
	    imageUrls.add(url);
	}
    }

    @Test
    public void testPresence() throws IOException {
	for (URL url : imageUrls) {
	    url.openStream();
	    assertNotNull(url);
	}
    }

    // TODO: remove integration test as soon as OpenJDK 8 in Debian can handle JPEGs correctly
    // see also https://dev.openecard.org/issues/369
    @Test(groups={"it"})
    public void testLoadImages() throws IOException {
	for (URL url : imageUrls) {
	    logger.info("Trying to load image '{}'.", url);
	    BufferedImage bi = ImageIO.read(url);
	    ImageIcon icon = new ImageIcon(bi);
	}
    }

}
