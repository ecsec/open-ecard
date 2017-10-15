/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.lib.gui.stub;

import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class AndroidMessageDialog implements MessageDialog {

	private static final Logger LOG = LoggerFactory.getLogger(AndroidMessageDialog.class);

	private MessageDialogResult createMessageDialog(String message, String title) {
		LOG.info("Title: " + title + " - Message: " + message);
		return new MessageDialogResult(message);
	}

	@Override
	public MessageDialogResult showMessageDialog(String message, String title) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showConfirmDialog(String message, String title) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType, byte[] iconData) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showInputDialog(String message, String title) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showInputDialog(String message, String title, String initialValue) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, String initialValue) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData, int initialSelectionIndex, String... selectionValues) {
		return createMessageDialog(message, title);
	}

	@Override
	public MessageDialogResult showOptionDialog(String message, String title, OptionType optionType, DialogType messageType, byte[] iconData, String... options) {
		return createMessageDialog(message, title);
	}
}
