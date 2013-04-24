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

package org.openecard.gui.android;

import android.content.Context;
import android.content.Intent;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;


/**
 * Android based MessageDialog implementation.
 * This implementation uses an Activity to show the MessageDialogs.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMessageDialog implements MessageDialog {

    public static final String ICON = "Icon";
    public static final String DIALOG_TYPE = "DialogType";
    public static final String TITLE = "Title";
    public static final String MESSAGE = "Message";
    private final Context context;
    private Intent intent;

    /**
     * Creates a new AndroidMessageDialog using the given Context.
     *
     * @param context current Context of the App
     */
    public AndroidMessageDialog(Context context) {
	this.context = context;
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title) {
	createBasicMessageDialogIntent(message, title);
	context.startActivity(intent);
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(DIALOG_TYPE, messageType);
	context.startActivity(intent);
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(DIALOG_TYPE, messageType);
	intent.putExtra(ICON, iconData);
	context.startActivity(intent);
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType,
	    DialogType messageType) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType,
	    DialogType messageType, byte[] iconData) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, String initialValue) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, String initialValue) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData,
	    int initialSelectionIndex, String... selectionValues) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialogResult showOptionDialog(String message, String title, OptionType optionType,
	    DialogType messageType, byte[] iconData, String... options) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private void createBasicMessageDialogIntent(String message, String title) {
	intent = new Intent(context, MessageDialogActivity.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	intent.putExtra(MESSAGE, message);
	intent.putExtra(TITLE, title);
    }

}
