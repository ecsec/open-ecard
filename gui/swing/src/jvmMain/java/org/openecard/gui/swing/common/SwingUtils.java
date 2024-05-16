/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.swing.common;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.openecard.common.util.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class SwingUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SwingUtils.class);

    /**
     * Opens a URL in the OS configured default program.
     * Currently only file and http/https and mailto URLs are tested.
     * The URL may contain system properties which get expanded when the expand flag is set.
     *
     * @param uri URL to open.
     * @param expandSysProps If {@code true} expand system properties in the URL, if {@code false} just use the URL as
     *   it is.
     */
    public static void openUrl(URI uri, boolean expandSysProps) {
	try {
	    if (expandSysProps) {
		String urlStr = uri.toString();
		urlStr = SysUtils.expandSysProps(urlStr);
		uri = new URI(urlStr);
	    }

	    boolean browserOpened = false;
	    if (Desktop.isDesktopSupported()) {
		if ("file".equals(uri.getScheme()) && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
		    try {
			Desktop.getDesktop().open(new File(uri));
			browserOpened = true;
		    } catch (IOException ex) {
			// failed to open browser
			LOG.debug(ex.getMessage(), ex);
		    }
		} else if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
			Desktop.getDesktop().browse(uri);
			browserOpened = true;
		    } catch (IOException ex) {
			// failed to open browser
			LOG.debug(ex.getMessage(), ex);
		    }
		}
	    }
	    if (! browserOpened) {
		String openTool;
		if (SysUtils.isUnix()) {
		    openTool = "xdg-open";
		} else if (SysUtils.isWin()) {
		    openTool = "start";
		} else {
		    openTool = "open";
		}
		ProcessBuilder pb = new ProcessBuilder(openTool, uri.toString());
		try {
		    pb.start();
		} catch (IOException ex) {
		    // failed to execute command
		    LOG.debug(ex.getMessage(), ex);
		}
	    }
	} catch (URISyntaxException ex) {
	    // wrong syntax
	    LOG.debug(ex.getMessage(), ex);
	}
    }

}
