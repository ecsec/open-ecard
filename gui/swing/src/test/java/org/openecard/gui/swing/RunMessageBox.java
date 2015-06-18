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

package org.openecard.gui.swing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * Test class for manual execution of the Swing based MessageDialog.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class RunMessageBox {

    private static final String MSG = "Message to show";
    private static final String TITLE = "Title";
    private UserConsent uc;
    private byte[] iconData;
    private String[] options;

    @BeforeTest
    public void initialize() throws IOException {
	uc = new SwingUserConsent(new SwingDialogWrapper());
	iconData = getLogoBytes();
	options = new String[] {"one", "two", "three"};
    }

    private byte[] getLogoBytes() throws IOException {
	InputStream is = FileUtils.resolveResourceAsStream(RunMessageBox.class, "openecard_logo.png");
	BufferedImage originalImage = ImageIO.read(is);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ImageIO.write(originalImage, "png", baos);
	byte[] imageInByte = baos.toByteArray();
	return imageInByte;
    }

    @Test(enabled = ! true)
    public void showMessage() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	messageBox.showMessageDialog(MSG, TITLE);
    }

    @Test(enabled = ! true)
    public void showMessage2() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	messageBox.showMessageDialog(MSG, TITLE, DialogType.ERROR_MESSAGE);
    }

    @Test(enabled = ! true)
    public void showMessage3() throws IOException {
	MessageDialog messageBox = uc.obtainMessageDialog();
	messageBox.showMessageDialog(MSG, TITLE, DialogType.ERROR_MESSAGE, iconData);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showConfirmDialog("Press yes!", TITLE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	result = messageBox.showConfirmDialog("Press no!", TITLE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.NO);
	result = messageBox.showConfirmDialog("Press cancel!", TITLE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.CANCEL);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog2() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog3() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
		DialogType.ERROR_MESSAGE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog4() throws IOException {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
		DialogType.ERROR_MESSAGE, iconData);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showInputDialog() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showInputDialog("Enter the text \"test\"!", TITLE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "test");
    }

    @Test(enabled = ! true)
    public void showInputDialog2() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showInputDialog("Press ok!", "initialValue");
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "initialValue");
    }

    @Test(enabled = ! true)
    public void showInputDialog3() {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE, "");
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showInputDialog4() throws IOException {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result;
	result = messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE, iconData, 2, options);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "three");
    }

    @Test(enabled = ! true)
    public void showOptionDialog() throws IOException {
	MessageDialog messageBox = uc.obtainMessageDialog();
	MessageDialogResult result = messageBox.showOptionDialog("Press the one button!", TITLE,
		OptionType.OK_CANCEL_OPTION, DialogType.ERROR_MESSAGE, iconData, options);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "one");
    }

}
