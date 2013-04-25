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

package org.openecard.gui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import org.openecard.gui.FileDialog;
import org.openecard.gui.file.FileDialogResult;
import org.openecard.gui.file.FileFilter;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.gui.swing.file.SwingFileFilterWrapper;


/**
 * Swing based FileDialog implementation.
 * This implementation wraps the {@link JFileChooser} class.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingFileDialog implements FileDialog {

    private final JFileChooser dialog;

    public SwingFileDialog() {
	this.dialog = new JFileChooser() {
	    @Override
	    protected JDialog createDialog(Component parent) {
		JDialog dialog = super.createDialog(parent);
		dialog.setIconImage(GUIDefaults.getImage("Frame.icon", 45, 45).getImage());
		return dialog;
	    }
	};
	this.dialog.setPreferredSize(new Dimension(800, 490));
    }


    @Override
    public void setTitle(String title) {
	dialog.setDialogTitle(title);
    }

    @Override
    public void setCurrentDirectory(File currentDir) {
	dialog.setCurrentDirectory(currentDir);
    }

    @Override
    public void setSelectedFiles(List<File> files) {
	dialog.setSelectedFiles((File[]) files.toArray());
    }

    @Override
    public void setSelectedFiles(File... files) {
	dialog.setSelectedFiles(files);
    }

    @Override
    public void clearSelectedFiles() {
	dialog.setSelectedFile(new File(""));
    }

    @Override
    public void addFileFilter(FileFilter filter) {
	dialog.setFileFilter(new SwingFileFilterWrapper(filter));
    }

    @Override
    public void clearFileFilters() {
	dialog.resetChoosableFileFilters();
    }

    @Override
    public void setShowHiddenFiles(boolean showHiddenFiles) {
	dialog.setFileHidingEnabled(! showHiddenFiles);
    }

    @Override
    public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
	dialog.setMultiSelectionEnabled(multiSelectionEnabled);
    }

    @Override
    public void setFolderSelectable(boolean folderSelectable) {
	int mode;
	if (folderSelectable) {
	    mode = JFileChooser.FILES_AND_DIRECTORIES;
	} else {
	    mode = JFileChooser.FILES_ONLY;
	}
	dialog.setFileSelectionMode(mode);
    }

    @Override
    public FileDialogResult showOpen() {
	int resultCode = dialog.showOpenDialog(null);
	FileDialogResult result = buildResult(resultCode);
	return result;
    }

    @Override
    public FileDialogResult showSave() {
	int resultCode = dialog.showSaveDialog(null);
	FileDialogResult result = buildResult(resultCode);
	return result;
    }

    @Override
    public FileDialogResult show(String approveButtonText) {
	int resultCode = dialog.showDialog(null, approveButtonText);
	FileDialogResult result = buildResult(resultCode);
	return result;
    }


    private FileDialogResult buildResult(int resultCode) {
	FileDialogResult result;
	if (resultCode == JFileChooser.APPROVE_OPTION) {
	    File[] selectedFiles = dialog.getSelectedFiles();
	    if (selectedFiles.length == 0) {
		selectedFiles = new File[1];
		selectedFiles[0] = dialog.getSelectedFile();
	    }
	    result = new FileDialogResult(Arrays.asList(selectedFiles));
	} else {
	    // cancel or error, it doesn't matter
	    result = new FileDialogResult();
	}
	return result;
    }

}
