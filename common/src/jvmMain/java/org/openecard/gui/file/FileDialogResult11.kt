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
 */
package org.openecard.gui.file

import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Result class of the file dialog.
 * This class indicates the type of action, the user performed and if applicable also contains a list of selected files.
 *
 * @author Tobias Wich
 */
class FileDialogResult @JvmOverloads constructor(selectedFiles: List<File>? = null) :
    Serializable {
    private var selectedFiles: List<File>? = null

    /**
     * Creates a result with status OK.
     * The instance returned by this constructor returns `true` in the [.isOK] method and returns
     * a non empty list in the [.getSelectedFiles] method.
     *
     * @param selectedFiles The list of files selected in the dialog, or the empty list if no files were selected.
     */
    /**
     * Creates a result with status CANCEL.
     * The instance returned by this constructor returns `true` in the [.isCancel] method and returns
     * an empty list in the [.getSelectedFiles] method.
     */
    init {
        if (selectedFiles == null) {
            this.selectedFiles = emptyList()
        } else {
            this.selectedFiles = selectedFiles
        }
    }

    val isOK: Boolean
        /**
         * Returns the result status of the file dialog.
         *
         * @return `true` if the dialog finished successfully and at least one file was selected,
         * `false` otherwise.
         */
        get() = !isCancel

    val isCancel: Boolean
        /**
         * Returns the result status of the file dialog.
         *
         * @return `true` if the dialog was cancelled and no file was selected, `false` otherwise.
         */
        get() = selectedFiles!!.isEmpty()

    /**
     * Returns the list of selected files.
     *
     * @return The list of selected files if the result of the dialog was OK, an empty list otherwise.
     */
    fun getSelectedFiles(): List<File> {
        return Collections.unmodifiableList(selectedFiles)
    }
}
