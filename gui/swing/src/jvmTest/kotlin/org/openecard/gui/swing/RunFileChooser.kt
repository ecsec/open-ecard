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

import org.junit.jupiter.api.Disabled
import org.openecard.gui.UserConsent
import org.openecard.gui.file.CombiningOrFilter
import org.openecard.gui.file.FileDialogResult
import org.openecard.gui.file.FileEndingFilter
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test class for manual execution of the Swing based FileDialog.
 *
 * @author Tobias Wich
 */
class RunFileChooser {
	private lateinit var uc: UserConsent

	@BeforeTest
	fun initialize() {
		uc = SwingUserConsent(SwingDialogWrapper())
	}

	@Disabled
	@Test
	fun openFile() {
		val dialog = uc.obtainFileDialog()
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun saveFile() {
		val dialog = uc.obtainFileDialog()
		val result = dialog.showSave()
		checkResult(result)
	}

	@Disabled
	@Test
	fun setCurrentDir() {
		val dialog = uc.obtainFileDialog()
		val currentDir = File("/tmp")
		dialog.setCurrentDirectory(currentDir)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun setSelectedFile() {
		val dialog = uc.obtainFileDialog()
		val selectedFile = File("/etc/issue")
		dialog.setSelectedFiles(selectedFile)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun setSelectedFiles() {
		val dialog = uc.obtainFileDialog()
		// dialog.setMultiSelectionEnabled(true);
		val selectedFile1 = File("/etc/issue")
		val selectedFile2 = File("/etc/passwd")
		dialog.setSelectedFiles(selectedFile1, selectedFile2)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun clearSelectedFiles() {
		val dialog = uc.obtainFileDialog()
		val selectedFile1 = File("/etc/issue")
		val selectedFile2 = File("/etc/passwd")
		dialog.setSelectedFiles(selectedFile1, selectedFile2)
		dialog.clearSelectedFiles()
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun setFileFilter() {
		val dialog = uc.obtainFileDialog()
		dialog.addFileFilter(FileEndingFilter("xml"))
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun setCombiningFileFilter() {
		val dialog = uc.obtainFileDialog()
		val filter = CombiningOrFilter(FileEndingFilter("xml"), FileEndingFilter("png"))
		dialog.addFileFilter(filter)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun clearFileFilter() {
		val dialog = uc.obtainFileDialog()
		dialog.addFileFilter(FileEndingFilter("xml"))
		dialog.clearFileFilters()
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun showHiddenFiles() {
		val dialog = uc.obtainFileDialog()
		dialog.setShowHiddenFiles(true)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun folderSelectable() {
		val dialog = uc.obtainFileDialog()
		dialog.setFolderSelectable(true)
		val result = dialog.showOpen()
		checkResult(result)
	}

	@Disabled
	@Test
	fun selectMultipleFiles() {
		val dialog = uc.obtainFileDialog()
		dialog.setMultiSelectionEnabled(true)
		val result = dialog.showOpen()
		checkResult(result)
	}

	private fun checkResult(result: FileDialogResult) {
		if (result.isOK) {
			assertTrue(!result.selectedFiles.isEmpty())
		} else {
			assertTrue(result.selectedFiles.isEmpty())
		}
	}
}
