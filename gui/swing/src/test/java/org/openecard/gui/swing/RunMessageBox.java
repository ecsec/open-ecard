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
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.MessageBox;
import org.openecard.gui.UserConsent;
import org.openecard.gui.messagebox.DialogType;
import org.openecard.gui.messagebox.MessageBoxResult;
import org.openecard.gui.messagebox.OptionType;
import org.openecard.gui.messagebox.ReturnType;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * Test class for manual execution of the Swing based MessageBox.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RunMessageBox {

    private static final String MSG = "Message to show";
    private static final String TITLE = "Title";
    private UserConsent uc;
    private byte[] iconData;
    private ArrayList<String> options;

    @BeforeTest
    public void initialize() throws IOException {
	uc = new SwingUserConsent(new SwingDialogWrapper());
	iconData = getLogoBytes();
	options = new ArrayList<String>();
	options.add("one");
	options.add("two");
	options.add("three");
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
	MessageBox messageBox = uc.obtainMessageBox();
	messageBox.showMessage(MSG);
    }

    @Test(enabled = ! true)
    public void showMessage2() {
	MessageBox messageBox = uc.obtainMessageBox();
	messageBox.showMessage(MSG, TITLE, DialogType.ERROR_MESSAGE);
    }

    @Test(enabled = ! true)
    public void showMessage3() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	messageBox.showMessage(MSG, TITLE, DialogType.ERROR_MESSAGE, iconData);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog("Press yes!");
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	result = messageBox.showConfirmDialog("Press no!");
	Assert.assertEquals(result.getReturnValue(), ReturnType.NO);
	result = messageBox.showConfirmDialog("Press cancel!");
	Assert.assertEquals(result.getReturnValue(), ReturnType.CANCEL);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog2() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog3() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
		DialogType.ERROR_MESSAGE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog4() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
		DialogType.ERROR_MESSAGE, iconData);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showInputDialog() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog("Enter the text \"test\"!");
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "test");
    }

    @Test(enabled = ! true)
    public void showInputDialog2() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog("Press ok!", "initialValue");
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "initialValue");
    }

    @Test(enabled = ! true)
    public void showInputDialog3() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
    }

    @Test(enabled = ! true)
    public void showInputDialog4() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result;
	result = messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE, iconData, options, 2);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "three");
    }

    @Test(enabled = ! true)
    public void showOptionDialog() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showOptionDialog("Press the one button!", TITLE,
		OptionType.OK_CANCEL_OPTION, DialogType.ERROR_MESSAGE, iconData, options, 1);
	Assert.assertEquals(result.getReturnValue(), ReturnType.OK);
	Assert.assertEquals(result.getUserInput(), "one");
    }

}
