/****************************************************************************
 * Copyright (C) 2013-2018 HS Coburg.
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

import org.openecard.gui.MessageDialog
import org.openecard.gui.message.DialogType
import org.openecard.gui.message.MessageDialogResult
import org.openecard.gui.message.OptionType
import org.openecard.gui.message.ReturnType
import org.openecard.gui.swing.common.GUIDefaults
import javax.swing.ImageIcon
import javax.swing.JOptionPane

/**
 * Swing based MessageDialog implementation.
 * This implementation wraps the [JOptionPane] class from Swing.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class SwingMessageDialog : MessageDialog {
	override fun showMessageDialog(
		msg: String,
		title: String?,
	): MessageDialogResult = showMessageDialog(msg, title, DialogType.INFORMATION_MESSAGE)

	override fun showMessageDialog(
		msg: String,
		title: String?,
		msgType: DialogType,
	): MessageDialogResult = showMessageDialog(msg, title, msgType, null)

	override fun showMessageDialog(
		msg: String,
		title: String?,
		msgType: DialogType,
		iconData: ByteArray?,
	): MessageDialogResult {
		var msg = formatMessage(msg)
		val icon = if (iconData != null) ImageIcon(iconData) else null
		val jop = JOptionPane(msg, convertDialogType(msgType), JOptionPane.DEFAULT_OPTION, icon)
		val dialog = jop.createDialog(title)
		dialog.setIconImage(FRAME_ICON)
		if (SwingDialogWrapper.Companion.needsFullscreen()) {
			dialog.setAlwaysOnTop(true)
		}
		dialog.isVisible = true
		dialog.toFront()

		return MessageDialogResult(ReturnType.OK)
	}

	override fun showConfirmDialog(
		msg: String,
		title: String?,
	): MessageDialogResult = showConfirmDialog(msg, title, OptionType.YES_NO_CANCEL_OPTION)

	override fun showConfirmDialog(
		msg: String,
		title: String?,
		optionType: OptionType,
	): MessageDialogResult = showConfirmDialog(msg, title, optionType, DialogType.QUESTION_MESSAGE)

	override fun showConfirmDialog(
		msg: String,
		title: String?,
		optionType: OptionType,
		msgType: DialogType,
	): MessageDialogResult = showConfirmDialog(msg, title, optionType, msgType, null)

	override fun showConfirmDialog(
		msg: String,
		title: String?,
		optionType: OptionType,
		msgType: DialogType,
		iconData: ByteArray?,
	): MessageDialogResult {
		var msg = msg
		msg = formatMessage(msg)
		val icon = if (iconData != null) ImageIcon(iconData) else null
		val jop =
			JOptionPane(
				msg,
				convertDialogType(msgType),
				convertOptionType(optionType),
				icon,
			)
		val dialog = jop.createDialog(title)
		dialog.setIconImage(FRAME_ICON)
		if (SwingDialogWrapper.Companion.needsFullscreen()) {
			dialog.setAlwaysOnTop(true)
		}
		dialog.isVisible = true
		dialog.toFront()

		val returnValue = jop.getValue()
		return if (returnValue == null) {
			MessageDialogResult(ReturnType.CANCEL)
		} else {
			MessageDialogResult(convertReturnType(returnValue as Int))
		}
	}

	override fun showInputDialog(
		msg: String,
		title: String,
	): MessageDialogResult = showInputDialog(msg, title, "")

	override fun showInputDialog(
		msg: String,
		title: String,
		initialValue: String?,
	): MessageDialogResult = showInputDialog(msg, title, DialogType.QUESTION_MESSAGE, initialValue)

	override fun showInputDialog(
		msg: String,
		title: String?,
		msgType: DialogType,
		initialValue: String?,
	): MessageDialogResult {
		var msg = formatMessage(msg)
		val jop = JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION)
		val dialog = jop.createDialog(title)
		dialog.setIconImage(FRAME_ICON)
		jop.setInitialSelectionValue(initialValue)
		jop.setWantsInput(true)
		if (SwingDialogWrapper.Companion.needsFullscreen()) {
			dialog.setAlwaysOnTop(true)
		}
		dialog.isVisible = true
		dialog.toFront()

		val returnValue = jop.getInputValue()
		return if (returnValue == null) {
			MessageDialogResult(null as String?)
		} else {
			MessageDialogResult(returnValue as String)
		}
	}

	override fun showInputDialog(
		msg: String,
		title: String?,
		msgType: DialogType,
		iconData: ByteArray?,
		initialSelectedIndex: Int,
		vararg options: String?,
	): MessageDialogResult {
		var msg = msg
		var initialSelectedIndex = initialSelectedIndex
		msg = formatMessage(msg)
		val optionsList = options.toList()
		require(!optionsList.isEmpty()) { "List of options must be given." }
		if (initialSelectedIndex > optionsList.size) {
			initialSelectedIndex = 0
		}
		val initialValue = optionsList[initialSelectedIndex]
		val icon = ImageIcon(iconData)
		val jop =
			JOptionPane(
				msg,
				convertDialogType(msgType),
				JOptionPane.OK_CANCEL_OPTION,
				icon,
			)
		val dialog = jop.createDialog(title)
		dialog.setIconImage(FRAME_ICON)
		jop.setSelectionValues(options)
		jop.setInitialSelectionValue(initialValue)
		jop.setWantsInput(true)
		if (SwingDialogWrapper.Companion.needsFullscreen()) {
			dialog.setAlwaysOnTop(true)
		}
		dialog.isVisible = true
		dialog.toFront()

		val returnValue = jop.getInputValue()
		return if ("uninitializedValue" == returnValue && !optionsList.contains("uninitializedValue")) {
			MessageDialogResult(ReturnType.CANCEL)
		} else {
			MessageDialogResult(returnValue as String?)
		}
	}

	override fun showOptionDialog(
		msg: String,
		title: String?,
		optionType: OptionType,
		msgType: DialogType,
		iconData: ByteArray?,
		vararg options: String?,
	): MessageDialogResult {
		var msg = msg
		msg = formatMessage(msg)
		require(options.isNotEmpty()) { "List of options must be given." }
		var icon: ImageIcon? = null
		if (iconData != null) {
			icon = ImageIcon(iconData)
		}
		val jop =
			JOptionPane(
				msg,
				convertDialogType(msgType),
				convertOptionType(optionType),
				icon,
				options,
			)
		val dialog = jop.createDialog(title)
		dialog.setIconImage(FRAME_ICON)
		if (SwingDialogWrapper.Companion.needsFullscreen()) {
			dialog.setAlwaysOnTop(true)
		}
		dialog.isVisible = true
		dialog.toFront()

		val returnValue = jop.getValue()
		return if (returnValue == null) {
			MessageDialogResult(ReturnType.CANCEL)
		} else {
			MessageDialogResult(returnValue as String)
		}
	}

	private fun formatMessage(message: String): String {
		val builder = StringBuilder()
		builder.append("<html><body style='width: 450px;'><p>")
		builder.append(message.replace("\n", "<br>"))
		builder.append("</p></body></html>")
		return builder.toString()
	}

	companion object {
		private val FRAME_ICON = GUIDefaults.getImage("Frame.icon", 45, 45)!!.getImage()

		private fun convertOptionType(optionType: OptionType): Int =
			when (optionType) {
				OptionType.YES_NO_OPTION -> JOptionPane.YES_NO_OPTION
				OptionType.YES_NO_CANCEL_OPTION -> JOptionPane.YES_NO_CANCEL_OPTION
				OptionType.OK_CANCEL_OPTION -> JOptionPane.OK_CANCEL_OPTION
			}

		private fun convertDialogType(dialogType: DialogType): Int =
			when (dialogType) {
				DialogType.ERROR_MESSAGE -> JOptionPane.ERROR_MESSAGE
				DialogType.INFORMATION_MESSAGE -> JOptionPane.INFORMATION_MESSAGE
				DialogType.WARNING_MESSAGE -> JOptionPane.WARNING_MESSAGE
				DialogType.QUESTION_MESSAGE -> JOptionPane.QUESTION_MESSAGE
				DialogType.PLAIN_MESSAGE -> JOptionPane.PLAIN_MESSAGE
			}

		private fun convertReturnType(returnValue: Int): ReturnType =
			when (returnValue) {
				JOptionPane.OK_OPTION -> ReturnType.OK
				JOptionPane.NO_OPTION -> ReturnType.NO
				JOptionPane.CANCEL_OPTION -> ReturnType.CANCEL
				else -> throw IllegalArgumentException()
			}
	}
}
