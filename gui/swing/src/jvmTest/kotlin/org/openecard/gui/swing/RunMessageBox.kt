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
package org.openecard.gui.swing

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.gui.UserConsent
import org.openecard.gui.message.DialogType
import org.openecard.gui.message.MessageDialogResult
import org.openecard.gui.message.OptionType
import org.openecard.gui.message.ReturnType
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Test class for manual execution of the Swing based MessageDialog.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class RunMessageBox {
    private lateinit var uc: UserConsent
    private lateinit var iconData: ByteArray
    private lateinit var options: Array<String>

    @BeforeTest
    @Throws(IOException::class)
    fun initialize() {
        uc = SwingUserConsent(SwingDialogWrapper())
        iconData = this.logoBytes
        options = arrayOf<String>("one", "two", "three")
    }

    @get:Throws(IOException::class)
    private val logoBytes: ByteArray
        get() {
            val `is` = resolveResourceAsStream(
                RunMessageBox::class.java,
                "openecard_logo.png"
            )
            val originalImage = ImageIO.read(`is`)
            val baos = ByteArrayOutputStream()
            ImageIO.write(originalImage, "png", baos)
            val imageInByte = baos.toByteArray()
            return imageInByte
        }

    @Test(enabled = !true)
    fun showMessage() {
        val messageBox = uc.obtainMessageDialog()
        messageBox.showMessageDialog(MSG, TITLE)
    }

    @Test(enabled = !true)
    fun showMessage2() {
        val messageBox = uc.obtainMessageDialog()
        messageBox.showMessageDialog(
            MSG,
            TITLE,
            DialogType.ERROR_MESSAGE
        )
    }

    @Test(enabled = !true)
    @Throws(IOException::class)
    fun showMessage3() {
        val messageBox = uc.obtainMessageDialog()
        messageBox.showMessageDialog(
            MSG,
            TITLE,
            DialogType.ERROR_MESSAGE,
            iconData
        )
    }

    @Test(enabled = !true)
    fun showConfirmDialog() {
        val messageBox = uc.obtainMessageDialog()
        var result = messageBox.showConfirmDialog("Press yes!", TITLE)
        Assert.assertEquals(result.returnValue, ReturnType.OK)
        result = messageBox.showConfirmDialog("Press no!", TITLE)
        Assert.assertEquals(result.returnValue, ReturnType.NO)
        result = messageBox.showConfirmDialog("Press cancel!", TITLE)
        Assert.assertEquals(result.returnValue, ReturnType.CANCEL)
    }

    @Test(enabled = !true)
    fun showConfirmDialog2() {
        val messageBox = uc.obtainMessageDialog()
        val result =
            messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION)
        Assert.assertEquals(result.returnValue, ReturnType.OK)
    }

    @Test(enabled = !true)
    fun showConfirmDialog3() {
        val messageBox = uc.obtainMessageDialog()
        val result = messageBox.showConfirmDialog(
            "Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
            DialogType.ERROR_MESSAGE
        )
        Assert.assertEquals(result.returnValue, ReturnType.OK)
    }

    @Test(enabled = !true)
    @Throws(IOException::class)
    fun showConfirmDialog4() {
        val messageBox = uc.obtainMessageDialog()
        val result = messageBox.showConfirmDialog(
            "Press ok!", TITLE, OptionType.OK_CANCEL_OPTION,
            DialogType.ERROR_MESSAGE, iconData
        )
        Assert.assertEquals(result.returnValue, ReturnType.OK)
    }

    @Test(enabled = !true)
    fun showInputDialog() {
        val messageBox = uc.obtainMessageDialog()
        val result = messageBox.showInputDialog("Enter the text \"test\"!", TITLE)
        Assert.assertEquals(result.returnValue, ReturnType.OK)
        Assert.assertEquals(result.userInput, "test")
    }

    @Test(enabled = !true)
    fun showInputDialog2() {
        val messageBox = uc.obtainMessageDialog()
        val result = messageBox.showInputDialog("Press ok!", "initialValue")
        Assert.assertEquals(result.returnValue, ReturnType.OK)
        Assert.assertEquals(result.userInput, "initialValue")
    }

    @Test(enabled = !true)
    fun showInputDialog3() {
        val messageBox = uc.obtainMessageDialog()
        val result =
            messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE, "")
        Assert.assertEquals(result.returnValue, ReturnType.OK)
    }

    @Test(enabled = !true)
    @Throws(IOException::class)
    fun showInputDialog4() {
        val messageBox = uc.obtainMessageDialog()
		val result: MessageDialogResult = messageBox.showInputDialog(
			"Press ok!",
			TITLE,
			DialogType.ERROR_MESSAGE,
			iconData,
			2,
			*options
		)
        Assert.assertEquals(result.returnValue, ReturnType.OK)
        Assert.assertEquals(result.userInput, "three")
    }

    @Test(enabled = !true)
    @Throws(IOException::class)
    fun showOptionDialog() {
        val messageBox = uc.obtainMessageDialog()
        val result = messageBox.showOptionDialog(
            "Press the one button!", TITLE,
            OptionType.OK_CANCEL_OPTION, DialogType.ERROR_MESSAGE, iconData, *options
        )
        Assert.assertEquals(result.returnValue, ReturnType.OK)
        Assert.assertEquals(result.userInput, "one")
    }

}

private const val MSG = "Message to show"
private const val TITLE = "Title"
