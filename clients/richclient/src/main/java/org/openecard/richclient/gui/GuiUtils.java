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

package org.openecard.richclient.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz
 */
public class GuiUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GuiUtils.class);

    private static final int IMG_HEIGHT = 81;
    private static final int IMG_WIDTH = 128;

    public static ImageIcon getScaledCardImageIcon(InputStream imageStream) {
	ImageIcon icon = new ImageIcon();
	try {
	    icon = new ImageIcon(ImageIO.read(imageStream));
	    icon.setImage(icon.getImage().getScaledInstance(IMG_WIDTH, IMG_HEIGHT, Image.SCALE_SMOOTH));
	} catch (IOException ex) {
	    LOG.error("Failed to read image stream.", ex);
	}
	return icon;
    }

    public static Image getImage(String name) {
	byte[] imgData = getImageData(name);
	Image img = Toolkit.getDefaultToolkit().createImage(imgData);
	return img;
    }

    private static byte[] getImageData(String name) {
	URL imageUrl = GuiUtils.class.getResource("images/" + name);
	if (imageUrl == null) {
	    imageUrl = GuiUtils.class.getResource("/images/" + name);
	}
	if (imageUrl == null) {
	    LOG.error("Failed to find image {}.", name);
	    return new byte[0];
	}

	try (InputStream in = imageUrl.openStream()) {
	    return getImageData(in);
	} catch (IOException ex) {
	    LOG.error(String.format("Failed to read image %s.", name), ex);
	    return new byte[0];
	}
    }

    private static byte[] getImageData(@Nonnull InputStream in) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream(40 * 1024);
	byte[] buf = new byte[4096];
	int numRead;

	while ((numRead = in.read(buf)) != -1) {
	    out.write(buf, 0, numRead);
	}

	return out.toByteArray();
    }

}
