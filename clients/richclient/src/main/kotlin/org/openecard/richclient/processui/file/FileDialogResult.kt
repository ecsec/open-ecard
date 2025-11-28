/*
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
package org.openecard.richclient.processui.file

import java.io.File
import java.io.Serializable

/**
 * Result class of the file dialog.
 * This class indicates the type of action, the user performed and if applicable also contains a list of selected files.
 *
 * @author Tobias Wich
 */
class FileDialogResult(
	selectedFiles: List<File>? = null,
) : Serializable {
	var selectedFiles: List<File>

	init {
		if (selectedFiles == null) {
			this.selectedFiles = emptyList()
		} else {
			this.selectedFiles = selectedFiles
		}
	}

	/**
	 * The result status of the file dialog.
	 *
	 * @return `true` if the dialog finished successfully and at least one file was selected, `false` otherwise.
	 */
	val isOK: Boolean
		get() = !isCancel

	/**
	 * The result status of the file dialog.
	 *
	 * @return `true` if the dialog was cancelled and no file was selected, `false` otherwise.
	 */
	val isCancel: Boolean
		get() = selectedFiles.isEmpty()
}
