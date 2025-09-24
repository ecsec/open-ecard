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
package org.openecard.gui

import org.openecard.gui.file.FileDialogResult
import org.openecard.gui.file.FileFilter
import java.io.File

/**
 * Interface for a generic file dialog.
 * This and the interfaces used in this definition are modeled after Swings [javax.swing.JFileChooser]. However it
 * should be abstract enough to fit under other implementations as well.
 *
 * @author Tobias Wich
 */
interface FileDialog {
	/**
	 * Sets the title of the dialog.
	 * This function may be ignored by an implementation incapable of displaying a title bar.<br></br>
	 * If the function is not called, a default value depending on the type of the dialog should be shown.
	 *
	 * @param title Text to set in the title of the dialog.
	 */
	fun setTitle(title: String)

	/**
	 * Sets the directory the dialog will display.
	 * The current directory may be overwritten by invocations to [.setSelectedFiles]. The
	 * behaviour when a reference to a file instead of a directory is given, is undefined. The implementation is free to
	 * choose whether to use the parent directory of the file or to not perform any changes at all.
	 *
	 * @param currentDir A file object pointing to the directory, which should be displayed in the file dialog.
	 */
	fun setCurrentDirectory(currentDir: File)

	/**
	 * Sets the preselected files of the dialog.
	 * This function is only guaranteed to work if all selected files are located in the same directory. Any
	 * implementation is however free to support more complex selection schemes. Note that this function may also alter
	 * the current directory as set with [.setCurrentDirectory].
	 *
	 * @param files List of preselected files. At least one must be present.
	 */
	fun setSelectedFiles(vararg files: File)

	/**
	 * Sets the preselected files of the dialog.
	 *
	 * @see .setSelectedFiles
	 * @param files List of preselected files. At least one must be present.
	 */
	fun setSelectedFiles(files: List<File>)

	/**
	 * Clears the currently set file preselection.
	 * The implementation is not required, but also not permitted, to reset the current directory.
	 *
	 * @see .setCurrentDirectory
	 */
	fun clearSelectedFiles()

	/**
	 * Adds a custom file filter to this dialog.
	 *
	 *
	 * File filters can be displayed in the dialog in a selection box. The user is then free to select the appropriate
	 * filter scheme. The filters description is shown to the user and should therefore indicate which files are
	 * filtered.
	 *
	 *
	 * The filter added last is preselected automatically. An implementation may keep the default all-file-filter in the
	 * list of selectable filters.
	 *
	 * @param filter The file filter that is to be added and then selected for the user.
	 */
	fun addFileFilter(filter: FileFilter)

	/**
	 * Remove all custom file filters.
	 * After the custom filters are removed, the default filter accepting all files, should be the only filter that is
	 * applied.
	 */
	fun clearFileFilters()

	/**
	 * Sets the visibility flag of hidden files in the dialog.
	 * An implementation is free to choose whether the user can alter this choice in the dialog.
	 *
	 * @param showHiddenFiles If the value is `true`, hidden files are shown. If the value is
	 * `false`, hidden files are not shown.
	 */
	fun setShowHiddenFiles(showHiddenFiles: Boolean)

	/**
	 * Sets the flag indicating that multiple files can be selected in the dialog.
	 *
	 * @param multiSelectionEnabled If the value is `true`, multiple files can be selected. If the value is
	 * `false`, only one file can be selected.
	 */
	fun setMultiSelectionEnabled(multiSelectionEnabled: Boolean)

	/**
	 * Sets the flag indicating that folders can be selected in the dialog.
	 *
	 * @param folderSelectable If the value is `true`, folders can be selected. If the value is
	 * `false`, only files can be selected.
	 */
	fun setFolderSelectable(folderSelectable: Boolean)

	/**
	 * Shows an 'Open File' dialog.
	 * The approval button must be set to a localized version of the text 'Open'. The title, if not set by an invocation
	 * of [.setTitle], must be set to an appropriate localized text indicating that this dialog
	 * is an open file dialog.
	 *
	 * @return Result containing the outcome of the dialog and a list of files if the dialog was not canceled.
	 */
	fun showOpen(): FileDialogResult

	/**
	 * Shows a 'Save File' dialog.
	 * The approval button must be set to a localized version of the text 'Save'. The title, if not set by an invocation
	 * of [.setTitle], must be set to an appropriate localized text indicating that this dialog
	 * is a save file dialog.
	 *
	 * @return Result containing the outcome of the dialog and a list of files if the dialog was not canceled.
	 */
	fun showSave(): FileDialogResult

	/**
	 * Shows a file dialog with a custom approval button text.
	 * The approval button's text is set to the given value. The text value should be localized. The value of the dialog
	 * title is undefined if not set explicitly by an invocation of [.setTitle].
	 *
	 * @param approveButtonText The text of the approval button. This text must be localized.
	 * @return Result containing the outcome of the dialog and a list of files if the dialog was not canceled.
	 */
	fun show(approveButtonText: String?): FileDialogResult
}
