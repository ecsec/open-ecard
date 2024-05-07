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

import java.io.File;
import org.openecard.gui.FileDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.file.CombiningOrFilter;
import org.openecard.gui.file.FileDialogResult;
import org.openecard.gui.file.FileEndingFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * Test class for manual execution of the Swing based FileDialog.
 *
 * @author Tobias Wich
 */
public class RunFileChooser {

    private UserConsent uc;

    @BeforeTest
    public void initialize() {
	uc = new SwingUserConsent(new SwingDialogWrapper());
    }

    @Test(enabled = ! true)
    public void openFile() {
	FileDialog dialog = uc.obtainFileDialog();
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void saveFile() {
	FileDialog dialog = uc.obtainFileDialog();
	FileDialogResult result = dialog.showSave();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void setCurrentDir() {
	FileDialog dialog = uc.obtainFileDialog();
	File currentDir = new File("/tmp");
	dialog.setCurrentDirectory(currentDir);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void setSelectedFile() {
	FileDialog dialog = uc.obtainFileDialog();
	File selectedFile = new File("/etc/issue");
	dialog.setSelectedFiles(selectedFile);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void setSelectedFiles() {
	FileDialog dialog = uc.obtainFileDialog();
	//dialog.setMultiSelectionEnabled(true);
	File selectedFile1 = new File("/etc/issue");
	File selectedFile2 = new File("/etc/passwd");
	dialog.setSelectedFiles(selectedFile1, selectedFile2);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void clearSelectedFiles() {
	FileDialog dialog = uc.obtainFileDialog();
	File selectedFile1 = new File("/etc/issue");
	File selectedFile2 = new File("/etc/passwd");
	dialog.setSelectedFiles(selectedFile1, selectedFile2);
	dialog.clearSelectedFiles();
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled =  ! true)
    public void setFileFilter() {
	FileDialog dialog = uc.obtainFileDialog();
	dialog.addFileFilter(new FileEndingFilter("xml"));
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void setCombiningFileFilter() {
	FileDialog dialog = uc.obtainFileDialog();
	CombiningOrFilter filter = new CombiningOrFilter(new FileEndingFilter("xml"), new FileEndingFilter("png"));
	dialog.addFileFilter(filter);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void clearFileFilter() {
	FileDialog dialog = uc.obtainFileDialog();
	dialog.addFileFilter(new FileEndingFilter("xml"));
	dialog.clearFileFilters();
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void showHiddenFiles() {
	FileDialog dialog = uc.obtainFileDialog();
	dialog.setShowHiddenFiles(true);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void folderSelectable() {
	FileDialog dialog = uc.obtainFileDialog();
	dialog.setFolderSelectable(true);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }

    @Test(enabled = ! true)
    public void selectMultipleFiles() {
	FileDialog dialog = uc.obtainFileDialog();
	dialog.setMultiSelectionEnabled(true);
	FileDialogResult result = dialog.showOpen();
	checkResult(result);
    }


    private void checkResult(FileDialogResult result) {
	if (result.isOK()) {
	    Assert.assertTrue(! result.getSelectedFiles().isEmpty());
	} else {
	    Assert.assertTrue(result.getSelectedFiles().isEmpty());
	}
    }

}
