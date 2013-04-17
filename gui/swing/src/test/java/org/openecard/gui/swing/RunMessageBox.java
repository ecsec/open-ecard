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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.openecard.gui.MessageBox;
import org.openecard.gui.UserConsent;
import org.openecard.gui.messagebox.MessageBoxResult;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * Test class for manual execution of the Swing based MessageBox.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class RunMessageBox {

    private static final String MSG = "Message to show";
    private static final String TITLE = "Title";
    private UserConsent uc;
    private byte[] iconData;

    @BeforeTest
    public void initialize() throws IOException {
	uc = new SwingUserConsent(new SwingDialogWrapper());
	iconData = getLogoBytes();
    }

    private byte[] getLogoBytes() throws IOException {
	InputStream is = this.getClass().getResourceAsStream("/openecard_logo.png");
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
	JOptionPane.showMessageDialog(null, MSG);
    }

    @Test(enabled = ! true)
    public void showMessage2() {
	MessageBox messageBox = uc.obtainMessageBox();
	messageBox.showMessage(MSG, TITLE, MessageBox.ERROR_MESSAGE);
	JOptionPane.showMessageDialog(null, MSG, TITLE, MessageBox.ERROR_MESSAGE);
    }

    @Test(enabled = ! true)
    public void showMessage3() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	byte[] iconData = getLogoBytes();
	messageBox.showMessage(MSG, TITLE, MessageBox.ERROR_MESSAGE, iconData);
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane.showMessageDialog(null, MSG, TITLE, MessageBox.ERROR_MESSAGE, icon);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog(MSG);
	int returnValue = JOptionPane.showConfirmDialog(null, MSG);
	checkResult(result, returnValue);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog2() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog(MSG, TITLE, MessageBox.OK_CANCEL_OPTION);
	int returnValue = JOptionPane.showConfirmDialog(null, MSG, TITLE, MessageBox.OK_CANCEL_OPTION);
	checkResult(result, returnValue);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog3() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog(MSG, TITLE, MessageBox.OK_CANCEL_OPTION, MessageBox.ERROR_MESSAGE);
	int returnValue = JOptionPane.showConfirmDialog(null, MSG, TITLE, MessageBox.OK_CANCEL_OPTION, MessageBox.ERROR_MESSAGE);
	checkResult(result, returnValue);
    }

    @Test(enabled = ! true)
    public void showConfirmDialog4() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showConfirmDialog(MSG, TITLE, MessageBox.OK_CANCEL_OPTION, MessageBox.ERROR_MESSAGE, iconData);
	ImageIcon icon = new ImageIcon(iconData);
	int returnValue = JOptionPane.showConfirmDialog(null, MSG, TITLE, MessageBox.OK_CANCEL_OPTION, MessageBox.ERROR_MESSAGE, icon);
	checkResult(result, returnValue);
    }

    @Test(enabled = ! true)
    public void showInputDialog() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog(MSG);
	String input = JOptionPane.showInputDialog(MSG);
	checkInput(result);
	checkResult(result, input);
    }

    @Test(enabled = ! true)
    public void showInputDialog2() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog(MSG, "initialValue");
	String input = JOptionPane.showInputDialog(MSG, "initialValue");
	checkInput(result);
	checkResult(result, input);
    }

    @Test(enabled = ! true)
    public void showInputDialog3() {
	MessageBox messageBox = uc.obtainMessageBox();
	MessageBoxResult result = messageBox.showInputDialog(MSG, TITLE, MessageBox.ERROR_MESSAGE);
	String input = JOptionPane.showInputDialog(null, MSG, TITLE, MessageBox.ERROR_MESSAGE);
	checkInput(result);
	checkResult(result, input);
    }

    @Test(enabled =  ! true)
    public void showInputDialog4() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	String[] strings = new String[] { "one", "two", "three" };
	MessageBoxResult result;
	result = messageBox.showInputDialog(MSG, TITLE, MessageBox.ERROR_MESSAGE, iconData, strings, "three");
	ImageIcon icon = new ImageIcon(iconData);
	String input = (String) JOptionPane.showInputDialog(null, MSG, TITLE, MessageBox.ERROR_MESSAGE, icon, strings, "three");

	checkInput(result);
	checkResult(result, input);
    }

    @Test(enabled = ! true)
    public void showOptionDialog() throws IOException {
	MessageBox messageBox = uc.obtainMessageBox();
	String[] strings = new String[] { "one", "two", "three" };
	MessageBoxResult result = messageBox.showOptionDialog(
		MSG, TITLE, MessageBox.OK_CANCEL_OPTION, MessageBox.ERROR_MESSAGE, iconData, strings, "three");
	ImageIcon icon = new ImageIcon(iconData);
	int returnValue = JOptionPane.showOptionDialog(null, MSG, TITLE, MessageBox.OK_CANCEL_OPTION,  MessageBox.ERROR_MESSAGE, icon, strings, "three");
	checkResult(result, returnValue);
    }

    /**
     * Check if result is the same as the JOptionPane result. Same choices need to be made of course.
     */
    private void checkResult(MessageBoxResult result, int joptionpaneresult) {
	Assert.assertEquals(result.getReturnValue(), joptionpaneresult);
    }

    /**
     * Check if result is the same as the JOptionPane result. Same choices need to be made of course.
     */
    private void checkResult(MessageBoxResult result, String joptionpaneresult) {
	Assert.assertEquals(result.getUserInput(), joptionpaneresult);
    }

    /**
     * Check if input is really set when result is OK
     */
    private void checkInput(MessageBoxResult result) {
	if (result.getReturnValue() == MessageBox.OK) {
	    Assert.assertNotNull(result.getUserInput());
	} else {
	    Assert.assertNull(result.getUserInput());
	}
    }

}
