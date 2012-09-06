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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.about.AboutDialog;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.RichClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
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
    private PopupMenu popup;
    private Status status;
    private JFrame frame;
    private JLabel label;
    private RichClient client;
    private Boolean isLinux = null;
    private Boolean isKde = null;
    private boolean trayAvailable;

    public AppTray(RichClient client) {
	this.client = client;
    }

    /**
     * Starts the setup process.
     * A loading icon is displayed.
     */
    public void beginSetup() {
	createPopupMenu();

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
	    label.setIcon(GuiUtils.getImageIcon("logo_icon_default_256.png"));
	}

	status = new Status(rec);
    }

    public Status status() {
        return status;
    }

    
    private void createPopupMenu() {
        // TODO: implement config menu
//	MenuItem configItem = new MenuItem(lang.translationForKey("tray.config"));
//	configItem.addActionListener(new ActionListener() {
//
//	    @Override
//	    public void actionPerformed(ActionEvent e) {
//		JOptionPane.showMessageDialog(null, "Implement Me!");
//	    }
//	});

        // TODO: implement help menu
//	MenuItem helpItem = new MenuItem(lang.translationForKey("tray.help"));
//	helpItem.addActionListener(new ActionListener() {
//
//	    @Override
//	    public void actionPerformed(ActionEvent e) {
//		JOptionPane.showMessageDialog(null, "Implement Me!");
//	    }
//	});

        MenuItem aboutItem = new MenuItem(lang.translationForKey("tray.about"));
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.showDialog();
            }
        });

        MenuItem exitItem = new MenuItem(lang.translationForKey("tray.exit"));
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (trayAvailable) {
                    trayIcon.displayMessage("Open eCard App", lang.translationForKey("tray.message.shutdown"), TrayIcon.MessageType.INFO);
                    client.teardown();
                    tray.remove(trayIcon);
                } else {
                    client.teardown();
                }
                System.exit(0);
            }
        });

        popup = new PopupMenu();
//	popup.add(configItem);
//	popup.add(helpItem);
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);
    }
    
    
    private void setupTrayIcon() {
        trayAvailable = true;
        
        tray = SystemTray.getSystemTray();
        
        trayIcon = new TrayIcon(getTrayIconImage(ICON_LOADER), lang.translationForKey("tray.message.loading"), popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
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
            switch(dim.width) {
                case 24:
                    return GuiUtils.getImage("logo_icon_linux_kde_24.png");
                default:
                    return GuiUtils.getImage("logo_icon_linux_default_256.png");
            }
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
            switch (dim.width) {
                case 16:
                    return GuiUtils.getImage("logo_icon_linux_default_16.png");
                case 24:
                    return GuiUtils.getImage("logo_icon_linux_default_24.png");
                case 32:
                    return GuiUtils.getImage("logo_icon_linux_default_32.png");
                case 48:
                    return GuiUtils.getImage("logo_icon_linux_default_48.png");
                case 64:
                    return GuiUtils.getImage("logo_icon_linux_default_64.png");
                case 72:
                    return GuiUtils.getImage("logo_icon_linux_default_72.png");
                case 96:
                    return GuiUtils.getImage("logo_icon_linux_default_96.png");
                case 128:
                    return GuiUtils.getImage("logo_icon_linux_default_128.png");
                default:
                    return GuiUtils.getImage("logo_icon_linux_default_256.png");
            }
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
            switch (dim.width) {
                case 16:
                    return GuiUtils.getImage("logo_icon_default_16.png");
                case 24:
                    return GuiUtils.getImage("logo_icon_default_24.png");
                case 32:
                    return GuiUtils.getImage("logo_icon_default_32.png");
                case 48:
                    return GuiUtils.getImage("logo_icon_default_48.png");
                case 64:
                    return GuiUtils.getImage("logo_icon_default_64.png");
                case 72:
                    return GuiUtils.getImage("logo_icon_default_72.png");
                case 96:
                    return GuiUtils.getImage("logo_icon_default_96.png");
                case 128:
                    return GuiUtils.getImage("logo_icon_default_128.png");
                default:
                    return GuiUtils.getImage("logo_icon_default_256.png");
            }
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
            try {
                String[] command = {"pgrep", "-l", "kwin"};
                ProcessBuilder pb = new ProcessBuilder(command);
                Process child = pb.start();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = child.getInputStream();
                int c;
                while ((c = is.read()) != -1) {
                    baos.write(c);
                }
                is.close();

                isKde = baos.toString().indexOf("kwin") >= 0;

            } catch (IOException ex) {
                isKde = false;
            }
        }
        return isKde;
    }
    
    private void setupFrame() {
	trayAvailable = false;
        
	frame = new JFrame(lang.translationForKey("tray.title"));
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setIconImage(GuiUtils.getImage("logo_icon_default_256.png"));

	label = new JLabel(GuiUtils.getImageIcon("loader_icon_default_64.gif"));
	label.add(popup);
	label.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		    status.showInfo(e.getLocationOnScreen());
                    return;
		}

                if (e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON3) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
	    }
	});

        ImageIcon logo = GuiUtils.getImageIcon("logo_icon_default_256.png");
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
