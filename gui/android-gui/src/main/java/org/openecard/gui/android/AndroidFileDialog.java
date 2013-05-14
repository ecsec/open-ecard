/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.gui.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import org.openecard.gui.FileDialog;
import org.openecard.gui.file.FileDialogResult;
import org.openecard.gui.file.FileFilter;


/**
 * Android based FileDialog implementation.
 * This implementation uses an Activity to show the FileDialogs.
 * It returns an emtpy dummy result immediately. The real result is available in the onActivityResult callback of the
 * calling Activity.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidFileDialog implements FileDialog {

    public static final String FILE_DIALOG_TYPE = "fileDialogType";
    public static final String APPROVE_BUTTON_TEXT = "approveButtonText";
    public static final String FILE_FILTERS = "fileFilters";
    public static final String SELECTED_FILES = "selectedFiles";
    public static final String FOLDER_SELECTABLE = "folderSelectable";
    public static final String MULTI_SELECTION_ENABLED = "multiSelectionEnabled";
    public static final String SHOW_HIDDEN_FILES = "showHiddenFiles";
    public static final int REQUEST_CODE = 0;
    public static final String TITLE = "Title";
    public static final String CURRENT_DIR = "currentDir";

    private final Activity activityContext;
    private Intent intent;
    private String title;
    private ArrayList<File> selectedFiles = new ArrayList<File>();
    private ArrayList<FileFilter> fileFilters = new ArrayList<FileFilter>();
    private File currentDir;
    private boolean showHiddenFiles;
    private boolean multiSelectionEnabled;
    private boolean folderSelectable;
    private String approveButtonText;

    /**
     * Creates a new AndroidMessageDialog using the given Context.
     *
     * @param activityContext current ActivityContext
     */
    public AndroidFileDialog(Activity activityContext) {
	this.activityContext = activityContext;
	intent = new Intent(activityContext, FileDialogActivity.class);
    }

    @Override
    public void setTitle(String title) {
	this.title = title;
    }

    @Override
    public void setCurrentDirectory(File currentDir) {
	this.currentDir = currentDir;
    }

    @Override
    public void setSelectedFiles(File... files) {
	selectedFiles.clear();
	Collections.addAll(selectedFiles, files);
    }

    @Override
    public void setSelectedFiles(List<File> files) {
	selectedFiles.clear();
	selectedFiles.addAll(files);
    }

    @Override
    public void clearSelectedFiles() {
	selectedFiles.clear();
    }

    @Override
    public void addFileFilter(FileFilter filter) {
	fileFilters.add(filter);
    }

    @Override
    public void clearFileFilters() {
	fileFilters.clear();
    }

    @Override
    public void setShowHiddenFiles(boolean showHiddenFiles) {
	this.showHiddenFiles = showHiddenFiles;
    }

    @Override
    public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
	this.multiSelectionEnabled = multiSelectionEnabled;
    }

    @Override
    public void setFolderSelectable(boolean folderSelectable) {
	this.folderSelectable = folderSelectable;
    }

    @Override
    public FileDialogResult showOpen() {
	intent.putExtra(FILE_DIALOG_TYPE, FileDialogType.OPEN);
	startFileDialogActivity();
	return new FileDialogResult();
    }

    @Override
    public FileDialogResult showSave() {
	intent.putExtra(FILE_DIALOG_TYPE, FileDialogType.SAVE);
	startFileDialogActivity();
	return new FileDialogResult();
    }

    @Override
    public FileDialogResult show(String approveButtonText) {
	this.approveButtonText = approveButtonText;
	if (title == null) {
	    // set title the same as approveButtonText (like JFileChooser does)
	    this.title = approveButtonText;
	}
	intent.putExtra(FILE_DIALOG_TYPE, FileDialogType.OTHER);
	startFileDialogActivity();
	return new FileDialogResult();
    }

    private void startFileDialogActivity() {
	intent.putExtra(TITLE, title);
	intent.putExtra(CURRENT_DIR, currentDir);
	intent.putExtra(FILE_FILTERS, fileFilters);
	intent.putExtra(SHOW_HIDDEN_FILES, showHiddenFiles);
	intent.putExtra(FOLDER_SELECTABLE, folderSelectable);

	intent.putExtra(MULTI_SELECTION_ENABLED, multiSelectionEnabled);
	intent.putExtra(SELECTED_FILES, selectedFiles);
	intent.putExtra(APPROVE_BUTTON_TEXT, approveButtonText);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
    }

}
