/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing.common;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.openecard.client.gui.swing.StepBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingImage extends JLabel {

    private static final Logger logger = LoggerFactory.getLogger(StepBar.class);
    private ImageIcon image = new ImageIcon();

    public SwingImage(String resource) {

	URL url = SwingImage.class.getResource(resource);
	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
	    this.image.setImage(image);
	} else {
	    logger.info("Cannot load resource: " + resource);
	}

	setIcon(image);
    }

    private Image loadImage(String resource) {
	URL url = SwingImage.class.getResource(resource);
	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    return toolkit.getImage(url);
	} else {
	    logger.info("Cannot load resource: " + resource);
	}
	return null;
    }

    public SwingImage(String resource, int width, int height) {
	ImageIcon logo = new ImageIcon();
	URL url = SwingImage.class.getResource(resource);
	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
	    logo.setImage(image);
	}

	setIcon(logo);
    }

    public void setImageSize(Dimension preferredSize) {
	super.setPreferredSize(preferredSize);
    }
}
