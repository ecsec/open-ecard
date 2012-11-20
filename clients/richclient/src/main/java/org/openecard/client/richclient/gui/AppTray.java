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

package org.openecard.client.richclient.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.graphics.GraphicsUtil;
import org.openecard.client.gui.graphics.OecLogoBgWhite;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.RichClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class creates a tray icon on systems that do have a system tray.
 * Otherwise a normal window will be shown.
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class AppTray {

    private static final Logger logger = LoggerFactory.getLogger(AppTray.class);

    private static final String ICON_LOADER = "loader";
    private static final String ICON_LOGO = "logo";
    
    private final I18n lang = I18n.getTranslation("richclient");
    
    private SystemTray tray;
    private TrayIcon trayIcon;
    private Status status;
    private JFrame frame;
    private JLabel label;
    private RichClient client;
    private Boolean isLinux = null;
    private Boolean isKde = null;
    private boolean trayAvailable;

    /**
     * Constructor of AppTray class.
     * 
     * @param client RichClient
     */
    public AppTray(RichClient client) {
	this.client = client;
    }

    /**
     * Starts the setup process.
     * A loading icon is displayed.
     */
    public void beginSetup() {
	if (SystemTray.isSupported()) {
	    setupTrayIcon();
	} else {
	    setupFrame();
	}
    }

    /**
     * Finishes the setup process.
     * The loading icon is replaced with the eCard logo.
     */
    public void endSetup(CardRecognition rec) {
	if (trayAvailable) {
            trayIcon.setImage(getTrayIconImage(ICON_LOGO));
	    trayIcon.setToolTip(lang.translationForKey("tray.title"));
	} else {
	    label.setIcon(new ImageIcon(GraphicsUtil.createImage(OecLogoBgWhite.class, 256, 256)));
	}

	status = new Status(this, rec);
    }

    /**
     * Returns the current status.
     * 
     * @return current status
     */
    public Status status() {
        return status;
    }

    /**
     * Removes the tray icon from the tray and terminates the application.
     */
    public void shutdown() {
        if (trayAvailable) {
            trayIcon.displayMessage("Open eCard App", lang.translationForKey("tray.message.shutdown"), TrayIcon.MessageType.INFO);
            client.teardown();
            tray.remove(trayIcon);
        } else {
            client.teardown();
        }
        System.exit(0);
    }

    
    private void setupTrayIcon() {
        trayAvailable = true;
        
        tray = SystemTray.getSystemTray();
        
        trayIcon = new TrayIcon(getTrayIconImage(ICON_LOADER), lang.translationForKey("tray.message.loading"), null);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (status != null) {
                    status.showInfo(new Point(e.getX(), e.getY()));
                }
            }
        });
        
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            logger.error("TrayIcon could not be added to the system tray.", ex);

            // tray and trayIcon are not needed anymore
            tray = null;
            trayIcon = null;
            setupFrame();
        }
    }


    private Image getTrayIconImage(String name) {
        Dimension dim = tray.getTrayIconSize();
        
        if (isLinux()) {
            if (isKde()) {
                return getImageKde(name, dim);
            } else {
                return getImageLinux(name, dim);
            }
        } else {
            return getImageDefault(name, dim);
        }
    }
    
    private Image getImageKde(String name, Dimension dim) {
        if (name.equals(ICON_LOADER)) {
            switch(dim.width) {
                case 24:
                    return GuiUtils.getImage("loader_icon_linux_kde_24.gif");
                default:
                    return GuiUtils.getImage("loader_icon_linux_default_256.gif");
            }
        } else {
            // KDE uses tray icon images with the size of 24 x 24 pixels, but only 22 x 22 pixels are shown. The images
            // are not scaled, but simply get cropped on the right side and the bottom by 2 pixels. If a smaller image
            // is used (e.g. 22 x 22 pixels), it will be first scaled to 24 x 24 pixels and then it will be cropped.
            // So an image must be used which is 24 x 24 pixels in size and can be cropped by 2 pixels without loosing
            // any information. 
            // Attention: This may change in future version of KDE!
            return GraphicsUtil.createImage(OecLogoBgWhite.class, dim.width - 2, dim.height - 2, dim.width, dim.height, 0, 0);
        }
    }
    

    private Image getImageLinux(String name, Dimension dim) {
        if (name.equals(ICON_LOADER)) {
            switch (dim.width) {
                case 16:
                    return GuiUtils.getImage("loader_icon_linux_default_16.gif");
                case 24:
                    return GuiUtils.getImage("loader_icon_linux_default_24.gif");
                case 32:
                    return GuiUtils.getImage("loader_icon_linux_default_32.gif");
                case 48:
                    return GuiUtils.getImage("loader_icon_linux_default_48.gif");
                case 64:
                    return GuiUtils.getImage("loader_icon_linux_default_64.gif");
                case 72:
                    return GuiUtils.getImage("loader_icon_linux_default_72.gif");
                case 96:
                    return GuiUtils.getImage("loader_icon_linux_default_96.gif");
                case 128:
                    return GuiUtils.getImage("loader_icon_linux_default_128.gif");
                default:
                    return GuiUtils.getImage("loader_icon_linux_default_256.gif");
            }
        } else {
            return GraphicsUtil.createImage(OecLogoBgWhite.class, dim.width, dim.height);
        }
    }
    

    private Image getImageDefault(String name, Dimension dim) {
        if (name.equals(ICON_LOADER)) {
            switch (dim.width) {
                case 16:
                    return GuiUtils.getImage("loader_icon_default_16.gif");
                case 24:
                    return GuiUtils.getImage("loader_icon_default_24.gif");
                case 32:
                    return GuiUtils.getImage("loader_icon_default_32.gif");
                case 48:
                    return GuiUtils.getImage("loader_icon_default_48.gif");
                case 64:
                    return GuiUtils.getImage("loader_icon_default_64.gif");
                case 72:
                    return GuiUtils.getImage("loader_icon_default_72.gif");
                case 96:
                    return GuiUtils.getImage("loader_icon_default_96.gif");
                case 128:
                    return GuiUtils.getImage("loader_icon_default_128.gif");
                default:
                    return GuiUtils.getImage("loader_icon_default_256.gif");
            }
        } else {
            return GraphicsUtil.createImage(OecLogoBgWhite.class, dim.width, dim.height);
        }
    }


    private boolean isLinux() {
        if (isLinux == null) {
            String os = System.getProperty("os.name").toLowerCase();
            isLinux = os.indexOf("nux") >= 0;
        }
        return isLinux;
    }

    private boolean isKde() {
        if (isKde == null) {
	    // KDE_FULL_SESSION contains true when KDE is running
	    // The spec says (http://techbase.kde.org/KDE_System_Administration/Environment_Variables#KDE_FULL_SESSION)
	    // If you plan on using this variable to detect a running KDE session, check if the value is not empty
	    // instead of seeing if it equals true. The value might be changed in the future to include KDE version.
	    String kdeSession = System.getenv("KDE_FULL_SESSION");
	    if (kdeSession != null && ! kdeSession.isEmpty()) {
		isKde = true;
	    } else {
		isKde = false;
	    }
        }
        return isKde;
    }

    private void setupFrame() {
	trayAvailable = false;

	frame = new JFrame(lang.translationForKey("tray.title"));
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setIconImage(GraphicsUtil.createImage(OecLogoBgWhite.class, 256, 256));

	label = new JLabel(GuiUtils.getImageIcon("loader_icon_default_64.gif"));
	label.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent e) {
                status.showInfo(e.getLocationOnScreen());
	    }
	});

        ImageIcon logo = new ImageIcon(GraphicsUtil.createImage(OecLogoBgWhite.class, 256, 256));
	Container c = frame.getContentPane();
	c.setPreferredSize(new Dimension(logo.getIconWidth(), logo.getIconHeight()));
	c.setBackground(Color.white);
	c.add(label);

	frame.pack();
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
    }

}
