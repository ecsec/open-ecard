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
package org.openecard.richclient.processui.swing

import org.openecard.richclient.processui.UserConsent
import org.openecard.richclient.processui.graphics.oecImage
import org.openecard.richclient.processui.message.DialogType
import org.openecard.richclient.processui.message.MessageDialogResult
import org.openecard.richclient.processui.message.OptionType
import org.openecard.richclient.processui.message.ReturnType
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

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

			val baos = ByteArrayOutputStream()
			ImageIO.write(oecImage(60, 60), "png", baos)
			val imageInByte = baos.toByteArray()
			return imageInByte
		}

	@Ignore
	@Test
	fun showMessage() {
		val messageBox = uc.obtainMessageDialog()
		messageBox.showMessageDialog(MSG, TITLE)
	}

	@Ignore
	@Test
	fun showMessage2() {
		val messageBox = uc.obtainMessageDialog()
		messageBox.showMessageDialog(
			MSG,
			TITLE,
			DialogType.ERROR_MESSAGE,
		)
	}

	@Ignore
	@Test
	@Throws(IOException::class)
	fun showMessage3() {
		val messageBox = uc.obtainMessageDialog()
		messageBox.showMessageDialog(
			MSG,
			TITLE,
			DialogType.ERROR_MESSAGE,
			iconData,
		)
	}

	@Ignore
	@Test
	fun showConfirmDialog() {
		val messageBox = uc.obtainMessageDialog()
		var result = messageBox.showConfirmDialog("Press yes!", TITLE)
		assertEquals(result.returnValue, ReturnType.OK)
		result = messageBox.showConfirmDialog("Press no!", TITLE)
		assertEquals(result.returnValue, ReturnType.NO)
		result = messageBox.showConfirmDialog("Press cancel!", TITLE)
		assertEquals(result.returnValue, ReturnType.CANCEL)
	}

	@Ignore
	@Test
	fun showConfirmDialog2() {
		val messageBox = uc.obtainMessageDialog()
		val result =
			messageBox.showConfirmDialog("Press ok!", TITLE, OptionType.OK_CANCEL_OPTION)
		assertEquals(result.returnValue, ReturnType.OK)
	}

	@Ignore
	@Test
	fun showConfirmDialog3() {
		val messageBox = uc.obtainMessageDialog()
		val result =
			messageBox.showConfirmDialog(
				"Press ok!",
				TITLE,
				OptionType.OK_CANCEL_OPTION,
				DialogType.ERROR_MESSAGE,
			)
		assertEquals(result.returnValue, ReturnType.OK)
	}

	@Ignore
	@Test
	@Throws(IOException::class)
	fun showConfirmDialog4() {
		val messageBox = uc.obtainMessageDialog()
		val result =
			messageBox.showConfirmDialog(
				"Press ok!",
				TITLE,
				OptionType.OK_CANCEL_OPTION,
				DialogType.ERROR_MESSAGE,
				iconData,
			)
		assertEquals(result.returnValue, ReturnType.OK)
	}

	@Ignore
	@Test
	fun showInputDialog() {
		val messageBox = uc.obtainMessageDialog()
		val result = messageBox.showInputDialog("Enter the text \"test\"!", TITLE)
		assertEquals(result.returnValue, ReturnType.OK)
		assertEquals(result.userInput, "test")
	}

	@Ignore
	@Test
	fun showInputDialog2() {
		val messageBox = uc.obtainMessageDialog()
		val result = messageBox.showInputDialog("Press ok!", "initialValue")
		assertEquals(result.returnValue, ReturnType.OK)
		assertEquals(result.userInput, "initialValue")
	}

	@Ignore
	@Test
	fun showInputDialog3() {
		val messageBox = uc.obtainMessageDialog()
		val result =
			messageBox.showInputDialog("Press ok!", TITLE, DialogType.ERROR_MESSAGE, "")
		assertEquals(result.returnValue, ReturnType.OK)
	}

	@Ignore
	@Test
	@Throws(IOException::class)
	fun showInputDialog4() {
		val messageBox = uc.obtainMessageDialog()
		val result: MessageDialogResult =
			messageBox.showInputDialog(
				"Press ok!",
				TITLE,
				DialogType.ERROR_MESSAGE,
				iconData,
				2,
				*options,
			)
		assertEquals(result.returnValue, ReturnType.OK)
		assertEquals(result.userInput, "three")
	}

	@Ignore
	@Test
	@Throws(IOException::class)
	fun showOptionDialog() {
		val messageBox = uc.obtainMessageDialog()
		val result =
			messageBox.showOptionDialog(
				"Press the one button!",
				TITLE,
				OptionType.OK_CANCEL_OPTION,
				DialogType.ERROR_MESSAGE,
				iconData,
				*options,
			)
		assertEquals(result.returnValue, ReturnType.OK)
		assertEquals(result.userInput, "one")
	}
}

private const val MSG = "Message to show"
private const val TITLE = "Title"
