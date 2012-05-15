/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Logo extends JLabel {

    public Logo() {
	ImageIcon logo = new ImageIcon();
//	URL url = Logo.class.getResource("/loader.gif");
	URL url = Logo.class.getResource("/openecardwhite.gif");
	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
	    logo.setImage(image);
	}

	setIcon(logo);
//	logo.setImageObserver(this);
    }
}
