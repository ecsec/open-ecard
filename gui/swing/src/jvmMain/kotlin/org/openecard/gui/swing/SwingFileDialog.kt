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
package org.openecard.gui.swing

import org.openecard.gui.FileDialog
import org.openecard.gui.file.FileDialogResult
import org.openecard.gui.file.FileFilter
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.gui.swing.file.SwingFileFilterWrapper
import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.util.*
import javax.swing.JDialog
import javax.swing.JFileChooser

/**
 * Swing based FileDialog implementation.
 * This implementation wraps the [JFileChooser] class.
 *
 * @author Tobias Wich
 */
class SwingFileDialog : FileDialog {
    private val dialog: JFileChooser = object : JFileChooser() {
		override fun createDialog(parent: Component?): JDialog {
			val dialog = super.createDialog(parent)
			dialog.setIconImage(GUIDefaults.getImage("Frame.icon", 45, 45)!!.getImage())
			return dialog
		}
	}

	init {
		this.dialog.preferredSize = Dimension(800, 490)
    }


    override fun setTitle(title: String?) {
        dialog.setDialogTitle(title)
    }

    override fun setCurrentDirectory(currentDir: File?) {
        dialog.setCurrentDirectory(currentDir)
    }

    override fun setSelectedFiles(files: MutableList<File?>) {
        dialog.setSelectedFiles(files.toTypedArray())
    }

    override fun setSelectedFiles(vararg files: File?) {
        dialog.setSelectedFiles(files)
    }

    override fun clearSelectedFiles() {
        dialog.setSelectedFile(File(""))
    }

    override fun addFileFilter(filter: FileFilter) {
        dialog.setFileFilter(SwingFileFilterWrapper(filter))
    }

    override fun clearFileFilters() {
        dialog.resetChoosableFileFilters()
    }

    override fun setShowHiddenFiles(showHiddenFiles: Boolean) {
        dialog.setFileHidingEnabled(!showHiddenFiles)
    }

    override fun setMultiSelectionEnabled(multiSelectionEnabled: Boolean) {
        dialog.setMultiSelectionEnabled(multiSelectionEnabled)
    }

    override fun setFolderSelectable(folderSelectable: Boolean) {
        val mode: Int
        if (folderSelectable) {
            mode = JFileChooser.FILES_AND_DIRECTORIES
        } else {
            mode = JFileChooser.FILES_ONLY
        }
        dialog.setFileSelectionMode(mode)
    }

    override fun showOpen(): FileDialogResult {
        val resultCode = dialog.showOpenDialog(null)
        val result = buildResult(resultCode)
        return result
    }

    override fun showSave(): FileDialogResult {
        val resultCode = dialog.showSaveDialog(null)
        val result = buildResult(resultCode)
        return result
    }

    override fun show(approveButtonText: String?): FileDialogResult {
        val resultCode = dialog.showDialog(null, approveButtonText)
        val result = buildResult(resultCode)
        return result
    }


    private fun buildResult(resultCode: Int): FileDialogResult {
        val result: FileDialogResult
        if (resultCode == JFileChooser.APPROVE_OPTION) {
            var selectedFiles = dialog.selectedFiles
            if (selectedFiles.size == 0) {
                selectedFiles = arrayOfNulls<File>(1)
                selectedFiles[0] = dialog.selectedFile
            }
            result = FileDialogResult(selectedFiles.toList())
        } else {
            // cancel or error, it doesn't matter
            result = FileDialogResult()
        }
        return result
    }
}
