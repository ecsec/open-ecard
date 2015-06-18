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

package org.openecard.gui.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JLabel;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a hyperlink for use in a {@link org.openecard.gui.swing.StepFrame}. <br>
 * The link also has a click event which launches a browser. When the mouse
 * is located over the link, it is emphasised by underlining it.<br>
 * If no text is supplied, the text of the url is displayed.
 *
 * @author Tobias Wich
 */
public class Hyperlink implements StepComponent {

    private final URL href;
    private final String text;
    private final String underlineText;
    private final JLabel label;

    public Hyperlink(org.openecard.gui.definition.Hyperlink link) {
	href = link.getHref();
	text = link.getText() != null ? link.getText() : href.toString();
	underlineText = "<html><u>" + text + "</u></html>";
	label = new JLabel(text);
	label.setDoubleBuffered(true);
	label.setForeground(Color.blue);
	label.setToolTipText(href.toString());
	label.addMouseListener(new BrowserLauncher());
    }

    @Override
    public Component getComponent() {
	return label;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }


    /**
     * Open browser on click and hover link when mouse is located over the link.
     */
    private class BrowserLauncher implements MouseListener {
	@Override
	public void mouseClicked(MouseEvent e) {
	    try {
		boolean browserOpened = false;
		URI uri = new URI(href.toString());
		// try opening the browser with the recommended java method (should work on windows in any case)
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
			Desktop.getDesktop().browse(uri);
			browserOpened = true;
		    } catch (IOException ex) {
			// opening browser failed
		    }
		}
		// there is a bug which prevents this from working under linux without gnome. big up oracle you guys rock
		// in a standard linux desktop (freedesktop.org or lsb conforming) there is the xdg-open untility which can open the default browser
		// if that method also screws up then I'm out of ideas
		if (! browserOpened) {
		    ProcessBuilder pb = new ProcessBuilder("xdg-open", uri.toString());
		    try {
			pb.start();
		    } catch (IOException ex) {
			// failed to execute command
		    }
		}
	    } catch (URISyntaxException ex) {
		// silently fail, its just no use against developer stupidity
	    }
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	    label.setText(underlineText);
	}
	@Override
	public void mouseExited(MouseEvent e) {
	    label.setText(text);
	}
    }

}
