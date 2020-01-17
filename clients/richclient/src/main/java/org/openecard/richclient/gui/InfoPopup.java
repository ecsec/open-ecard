/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import javax.swing.JDialog;


/**
 * This class creats a InfoPopup showing different information about connected terminals and available cards.
 * It also contains the different controls of the application, e.g. the exit button.
 *
 * @author Johannes Schmölz
 */
public class InfoPopup extends JDialog implements StatusContainer {

	private static final long serialVersionUID = 1L;

	private static final int DISTANCE_TO_TASKBAR = 2; // in px

	private Point point;

	/**
	 * Constructor of InfoPopup class.
	 *
	 * @param c Container which will be set as ContentPane
	 */
	public InfoPopup(Container c) {
		this(c, null);
	}

	/**
	 * Constructor of InfoPopup class.
	 *
	 * @param c Container which will be set as ContentPane
	 * @param p position
	 */
	public InfoPopup(Container c, Point p) {
		super();
		point = p;
		setupUI(c);
	}

	/**
	 * Updates the content of the InfoPopup by setting a new ContentPane.
	 *
	 * @param c Container which will be set as ContentPane
	 */
	@Override
	public void updateContent(Container c) {
		setContentPane(c);
		pack();
		repaint();
		setLocation(calculatePosition(c, point));
	}

	private void setupUI(Container c) {
		setAlwaysOnTop(true);
		setUndecorated(true);
		setContentPane(c);
		pack();

		if (point != null) {
			setLocation(calculatePosition(c, point));
		}

		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				dispose();
				}
			}
		);

		setVisible(true);
	}


	private Point calculatePosition(Container c, Point p) {

		// we will get a transform matrix from the graphicsConfig below
		// it represents a possible screen scaling
		AffineTransform tx = new AffineTransform();
		// the size of the popup
		Dimension winSize = c.getPreferredSize();
		//bounds based on the click which happend on screen
		Rectangle globalBounds = new Rectangle(p.x, p.y, 0, 0);
		// init screenbounds to a sane default value if determination below fails
		int offset = 100;
		Rectangle screenBounds = new Rectangle(0+offset,0+offset,winSize.width+offset,winSize.height+offset);
		// find out non which screen the click happend (via contains) and determine it`s screen bounds
		try {
			GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gDevices = gEnv.getScreenDevices();
			deviceLoop:
			for (GraphicsDevice gDevice : gDevices) {
				GraphicsConfiguration[] gConfigs = gDevice.getConfigurations();
				for (GraphicsConfiguration gConfig : gConfigs) {
					// get the AffineTransform matrix of the config
					// which represents the screen scale factor
					tx = gConfig.getDefaultTransform();
					// get the bounds with scale factor in mind
					Rectangle bounds = tx.createTransformedShape(gConfig.getBounds()).getBounds();
					//if click is in screen, remember the bounds and break the loops
					if (bounds.contains(p)) {
						screenBounds = bounds;
						break deviceLoop;
					}
				}
			} 
		}catch (HeadlessException e) {
			System.err.println("org.openecard.richclient.gui.InfoPopup.calculatePosition(): failed to determine screenBounds, using fallback. Cause:");
			System.err.println(e.getMessage());
		}
		// calculate winBounds needed for positioning
		// in most cases the popup can be positioned left of the click/icon
		Rectangle winBounds = fitWindowLeft(globalBounds, winSize, screenBounds);
		if (winBounds == null) {
			//we need to fit it right (taskbar on the left)
			winBounds = fitWindowRight(globalBounds, winSize, screenBounds);
		}
		if (winBounds == null) {
			//fit fallback
			winBounds = fitWindowToScreen(winSize, screenBounds);
		}
		//if click is in lower half of screen
		if (p.y > screenBounds.height/2) {
			// use click cord as y limit for the popup
			winBounds.y = p.y - winSize.height;
		}
		
		// use the AffineTransorm Matrix from the graphicsConfiguration
		// to take screen scale into the calculation
		double x = winBounds.x/tx.getScaleX();
		double y = winBounds.y/tx.getScaleY();

		return new Point((int)x, (int)y);
	}

	/**
	 * Taken from java.desktop/sun/awt/X11/XBaseMenuWindow.java
	 * 
	 * Checks if window fits to the right specified item
	 * returns rectangle that the window fits to or null.
	 * @param itemBounds rectangle of item in global coordinates
	 * @param windowSize size of submenu window to fit
	 * @param screenBounds size of screen
	 */
	Rectangle fitWindowRight(Rectangle itemBounds, Dimension windowSize, Rectangle screenBounds) {
		int width = windowSize.width;
		int height = windowSize.height;
		//Window should be moved if it's outside top-left screen bounds
		int x = (itemBounds.x + itemBounds.width > screenBounds.x) ? itemBounds.x + itemBounds.width : screenBounds.x;
		int y = (itemBounds.y > screenBounds.y) ? itemBounds.y : screenBounds.y;
		if (x + width <= screenBounds.x + screenBounds.width) {
			//move it to the top if needed
			if (height > screenBounds.height) {
				height = screenBounds.height;
			}
			if (y + height > screenBounds.y + screenBounds.height) {
				y = screenBounds.y + screenBounds.height - height;
			}
			return new Rectangle(x, y, width, height);
		} else {
			return null;
		}
	}

	/**
	 * Taken from java.desktop/sun/awt/X11/XBaseMenuWindow.java
	 * 
	 * Checks if window fits to the left specified item
	 * returns rectangle that the window fits to or null.
	 * @param itemBounds rectangle of item in global coordinates
	 * @param windowSize size of submenu window to fit
	 * @param screenBounds size of screen
	 */
	Rectangle fitWindowLeft(Rectangle itemBounds, Dimension windowSize, Rectangle screenBounds) {
		int width = windowSize.width;
		int height = windowSize.height;
		//Window should be moved if it's outside top-right screen bounds
		int x = (itemBounds.x < screenBounds.x + screenBounds.width) ? itemBounds.x - width : screenBounds.x + screenBounds.width - width;
		int y = (itemBounds.y > screenBounds.y) ? itemBounds.y : screenBounds.y;
		if (x >= screenBounds.x) {
			//move it to the top if needed
			if (height > screenBounds.height) {
				height = screenBounds.height;
			}
			if (y + height > screenBounds.y + screenBounds.height) {
				y = screenBounds.y + screenBounds.height - height;
			}
			return new Rectangle(x, y, width, height);
		} else {
			return null;
		}
	}

	/**
	 * Taken from java.desktop/sun/awt/X11/XBaseMenuWindow.java
	 * 
	 * The last thing we can do with the window
	 * to fit it on screen - move it to the
	 * top-left edge and cut by screen dimensions
	 * @param windowSize size of submenu window to fit
	 * @param screenBounds size of screen
	 */
	Rectangle fitWindowToScreen(Dimension windowSize, Rectangle screenBounds) {
		int width = (windowSize.width < screenBounds.width) ? windowSize.width : screenBounds.width;
		int height = (windowSize.height < screenBounds.height) ? windowSize.height : screenBounds.height;
		return new Rectangle(screenBounds.x, screenBounds.y, width, height);
	}
	
}
