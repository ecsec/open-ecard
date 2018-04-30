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
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.openecard.common.I18n;
import org.openecard.common.util.SysUtils;
import org.openecard.common.util.VersionUpdate;
import org.openecard.common.util.VersionUpdateChecker;
import org.openecard.richclient.gui.GuiUtils;


/**
 *
 * @author Sebastian Schuberth
 */
public class UpdateSysTray {

    private final VersionUpdateChecker updateChecker;
    private final I18n lang = I18n.getTranslation("update");
    private TrayIcon trayIcon;
    private SystemTray tray;

    public UpdateSysTray(VersionUpdateChecker updateChecker) {
	this.updateChecker = updateChecker;
    }

    public void init() {
	new JFXPanel(); //initialize JavaFX
	Image image = GuiUtils.getImage("update.jpg");

	//Check the SystemTray is supported
	if (! SystemTray.isSupported()) {
	    System.out.println("SystemTray is not supported");
	    return;
	}

	final PopupMenu popup = new PopupMenu();
	final MenuItem directDownload = new MenuItem(lang.translationForKey("direct_download"));
	directDownload.addActionListener((ActionEvent e) -> {
	    String url = getDownloadUrl();
	    SysUtils.openUrl(URI.create(url), false);
	    removeTrayIcon();
	});

	final MenuItem downloadPage = new MenuItem(lang.translationForKey("download_page"));
	downloadPage.addActionListener((ActionEvent e) -> {
	    String url = getDownloadPage();
	    SysUtils.openUrl(URI.create(url), false);
	    removeTrayIcon();
	});

	final MenuItem close = new MenuItem(lang.translationForKey("close"));
	close.addActionListener((ActionEvent e) -> {
	    removeTrayIcon();
	});


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

	popup.add(directDownload);
	popup.add(downloadPage);
	popup.add(close);
	trayIcon.setPopupMenu(popup);
	trayIcon.setToolTip(lang.translationForKey("tooltip_msg"));
	trayIcon.setImageAutoSize(true);

	try {
	    tray.add(trayIcon);
	} catch (AWTException e) {
	    System.out.println("TrayIcon could not be added.");
	}

    }

    private String getDownloadUrl(){
	VersionUpdate major = updateChecker.getMajorUpgrade();
	if(major != null){
	    return major.getDownloadLink().toString();
	}

	VersionUpdate minor = updateChecker.getMinorUpgrade();
	if(minor != null){
	    return minor.getDownloadLink().toString();
	}

	VersionUpdate sec = updateChecker.getSecurityUpgrade();
	if(sec != null){
	    return sec.getDownloadLink().toString();
	}

	return null;
    }

    private String getDownloadPage(){
	VersionUpdate major = updateChecker.getMajorUpgrade();
	if(major != null){
	    return major.getDownloadPage().toString();
	}

	VersionUpdate minor = updateChecker.getMinorUpgrade();
	if(minor != null){
	    return minor.getDownloadPage().toString();
	}

	VersionUpdate sec = updateChecker.getSecurityUpgrade();
	if(sec != null){
	    return sec.getDownloadPage().toString();
	}

	return null;
    }

    private void openUpdateWindow() {
	Platform.runLater(() -> {
	    Stage stage = new Stage();
	    removeTrayIcon();
	    UpdateWindow uw = new UpdateWindow(updateChecker, stage);
	});
    }

    public void removeTrayIcon() {
	if (tray != null && trayIcon != null) {
	    tray.remove(trayIcon);
	    tray = null;
	    trayIcon = null;
	}
    }

}
