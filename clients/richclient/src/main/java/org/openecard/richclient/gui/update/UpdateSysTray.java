/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.richclient.gui.update;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.openecard.common.I18n;
import org.openecard.common.util.VersionUpdateChecker;
import org.openecard.richclient.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sebastian Schuberth
 */
public class UpdateSysTray {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateSysTray.class);

    private final VersionUpdateChecker updateChecker;
    private final I18n lang = I18n.getTranslation("update");
    private TrayIcon trayIcon;
    private SystemTray tray;
    private UpdateWindow uw;

    public UpdateSysTray(VersionUpdateChecker updateChecker) {
	this.updateChecker = updateChecker;
    }

    public void init() {
	Platform.setImplicitExit(false);
	new JFXPanel(); //initialize JavaFX
	Image image = GuiUtils.getImage("update.jpg");

	//Check the SystemTray is supported
	if (! SystemTray.isSupported()) {
	    LOG.warn("SystemTray is not supported. Opening update dialog directly.");
	    openUpdateWindow();
	}

	trayIcon = new TrayIcon(image);
	tray = SystemTray.getSystemTray();

	trayIcon.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    openUpdateWindow();
		}
	    }
	});

	trayIcon.setToolTip(lang.translationForKey("tooltip_msg"));
	trayIcon.setImageAutoSize(true);

	try {
	    tray.add(trayIcon);
	} catch (AWTException e) {
	    System.out.println("TrayIcon could not be added.");
	}
    }

    private synchronized void openUpdateWindow() {
	if (uw == null) {
	    // no window displayed, start it up
	    Platform.runLater(() -> {
		Stage stage = new Stage();
		stage.setOnHidden(event -> {
		    synchronized (this) {
			uw = null;
		    }
		});
		uw = new UpdateWindow(updateChecker, stage);
		uw.init();
	    });
	} else {
	    // window is already displayed, just bring it to the front
	    Platform.runLater(uw::toFront);
	}
    }

    public void removeTrayIcon() {
	if (tray != null && trayIcon != null) {
	    tray.remove(trayIcon);
	    tray = null;
	    trayIcon = null;
	}
    }

}
