/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.richclient.gui.components;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openecard.common.I18n;


/**
 *
 * @author Hans-Martin Haase
 */
public class FileChooserItem extends JFileChooser {

    private final I18n lang = I18n.getTranslation("addon");

    FileChooserItem(String fileTypes) {
	super(new File(System.getProperty("user.home")));
	// set open ecard logo in the title bar
	setDialogTitle(lang.translationForKey("addon.settings.file.select"));
	setFileFilter(new GenericFileTypeFilter(fileTypes));
	setDialogType(JFileChooser.OPEN_DIALOG);
	setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private class GenericFileTypeFilter extends FileFilter {

	private final List<String> fileTypes;

	public GenericFileTypeFilter(String fileTypes) {
	    String[] types = fileTypes.split(";");
	    this.fileTypes = new ArrayList<>(Arrays.asList(types));
	}

	@Override
	public boolean accept(File file) {
	    if (file.isDirectory()) {
		return true;
	    }

	    int startPosSuffix = file.getName().lastIndexOf(".");
	    if (startPosSuffix > -1) {
		for (String elem : fileTypes) {
		    String suffix = file.getName().substring(startPosSuffix);
		    suffix = suffix.replace(".", "");

		    if (suffix.equalsIgnoreCase(elem)) {
			return true;
		    }
		}
	    }
	    return false;
	}

	@Override
	public String getDescription() {
	    StringBuilder msg = new StringBuilder();
	    for (String type : fileTypes) {
		msg.append(".");
		msg.append(type);
		msg.append(", ");
	    }

	    return msg.toString();
	}
    }
}
