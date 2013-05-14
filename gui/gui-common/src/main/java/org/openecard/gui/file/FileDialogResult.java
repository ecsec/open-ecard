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

package org.openecard.gui.file;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * Result class of the file dialog.
 * This class indicates the type of action, the user performed and if applicable also contains a list of selected files.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FileDialogResult implements Serializable {

    private final List<File> selectedFiles;

    /**
     * Creates a result with status CANCEL.
     * The instance returned by this constructor returns <code>true</code> in the {@link #isCancel()} method and returns
     * an empty list in the {@link #getSelectedFiles()} method.
     */
    public FileDialogResult() {
	this(null);
    }

    /**
     * Creates a result with status OK.
     * The instance returned by this constructor returns <code>true</code> in the {@link #isOK()} method and returns
     * a non empty list in the {@link #getSelectedFiles()} method.
     *
     * @param selectedFiles The list of files selected in the dialog, or the empty list if no files were selected.
     */
    public FileDialogResult(List<File> selectedFiles) {
	if (selectedFiles == null) {
	    this.selectedFiles = Collections.emptyList();
	} else {
	    this.selectedFiles = selectedFiles;
	}
    }

    /**
     * Returns the result status of the file dialog.
     *
     * @return <code>true</code> if the dialog finished successfully and at least one file was selected,
     *   <code>false</code> otherwise.
     */
    public boolean isOK() {
	return ! isCancel();
    }

    /**
     * Returns the result status of the file dialog.
     *
     * @return <code>true</code> if the dialog was cancelled and no file was selected, <code>false</code> otherwise.
     */
    public boolean isCancel() {
	return selectedFiles.isEmpty();
    }

    /**
     * Returns the list of selected files.
     *
     * @return The list of selected files if the result of the dialog was OK, an empty list otherwise.
     */
    public List<File> getSelectedFiles() {
	return Collections.unmodifiableList(selectedFiles);
    }

}
