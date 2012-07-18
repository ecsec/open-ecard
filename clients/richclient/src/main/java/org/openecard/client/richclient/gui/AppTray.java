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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.about.AboutDialog;
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

    private final I18n lang = I18n.getTranslation("richclient");

    private SystemTray tray;
    private TrayIcon trayIcon;
    private PopupMenu popup;
    private JFrame frame;
    private JLabel label;
    private ImageIcon logo;
    private ImageIcon loader;
    private RichClient client;
    private boolean trayAvailable;

    public AppTray(RichClient client) {
       this.client = client;
       setupUI();
    }

    private void setupUI() {
	logo = GuiUtils.getImageIcon("logo.png");
	loader = GuiUtils.getImageIcon("loader.gif");

	createPopupMenu();

	if (SystemTray.isSupported()) {
	    trayAvailable = true;

	    tray = SystemTray.getSystemTray();

	    trayIcon = new TrayIcon(loader.getImage(), lang.translationForKey("tray.message.loading"), popup);
	    trayIcon.setImageAutoSize(true);

	    try {
		tray.add(trayIcon);
	    } catch (AWTException ex) {
		logger.error("TrayIcon could not be added to the system tray.", ex);

		// tray and trayIcon are not needed anymore
		tray = null;
		trayIcon = null;
		setupFrame();
	    }

	} else {
	    setupFrame();
	}
    }

    public void done() {
	if (trayAvailable) {
	    trayIcon.setImage(logo.getImage());
	    trayIcon.setToolTip(lang.translationForKey("tray.title"));
	} else {
	    label.setIcon(logo);
	}
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
		    System.exit(0);
		} else {
		    client.teardown();
		    System.exit(0);
		}
	    }
	});

	popup = new PopupMenu();
//	popup.add(configItem);
//	popup.add(helpItem);
	popup.add(aboutItem);
	popup.addSeparator();
	popup.add(exitItem);
    }


    private void setupFrame() {
	trayAvailable = false;

	frame = new JFrame(lang.translationForKey("tray.title"));
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setIconImage(logo.getImage());

	label = new JLabel(loader);
	label.add(popup);
	label.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON1) {
		    popup.show(e.getComponent(), e.getX(), e.getY());
		}
	    }
	});

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
