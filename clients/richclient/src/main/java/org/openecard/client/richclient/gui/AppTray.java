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
import java.net.URL;
import javax.swing.JOptionPane;
import org.openecard.client.common.I18n;
import org.openecard.client.common.logging.LoggingConstants;
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

    private final I18n lang = I18n.getTranslation("tray");

    private TrayIcon trayIcon;
    private RichClient client;

    public AppTray(RichClient client) {
       this.client = client;
       initialize();
    }

    private void initialize() {
        if (SystemTray.isSupported()) {
            final SystemTray tray = SystemTray.getSystemTray();
            
            MenuItem configItem = new MenuItem(lang.translationForKey("tray.config"));
            configItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                     JOptionPane.showMessageDialog(null, "Implement Me!");
                }
            });
            
            MenuItem helpItem = new MenuItem(lang.translationForKey("tray.help"));
            helpItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, "Implement Me!");
                }
            });
            
            MenuItem aboutItem = new MenuItem(lang.translationForKey("tray.about"));
            aboutItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, "Implement Me!");
                }
            });
            
            MenuItem exitItem = new MenuItem(lang.translationForKey("tray.exit"));
            exitItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("Open eCard App", lang.translationForKey("tray.message.shutdown"), TrayIcon.MessageType.INFO);
                    client.teardown();
                    tray.remove(trayIcon);
                    System.exit(0);
                }
            });
            
            final PopupMenu popup = new PopupMenu();
            popup.add(configItem);
            popup.add(helpItem);
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(exitItem);
                        
            trayIcon = new TrayIcon(getImage("loader.gif"), lang.translationForKey("tray.message.loading"), popup);
            trayIcon.setImageAutoSize(true);
            
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                logger.error(LoggingConstants.THROWING, "TrayIcon could not be added to the system tray.", ex);
            }
            
        } else {
            // TODO: handle cases where system tray is not supported
        }
    }

    public void done() {
        trayIcon.setImage(getImage("logo.png"));
        trayIcon.setToolTip(lang.translationForKey("tray.title"));
    }

    private Image getImage(String name) {
	URL imageUrl = AppTray.class.getResource("images/" + name);

	if (imageUrl == null) {
	    imageUrl = AppTray.class.getResource("/images/" + name);
	}
	Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);

	return image;
    }

}
