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

import android.app.Activity;
import android.content.Intent;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;


/**
 * Android based MessageDialog implementation.
 * This implementation uses an Activity to show the MessageDialogs. 
 * It returns a dummy OK result immediately. The real result is available in the onActivityResult callback of the
 * calling Activity.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMessageDialog implements MessageDialog {

    public static final int REQUEST_CODE = 0;
    public static final String OPTIONS = "Options";
    public static final String OPTION_TYPE = "OptionType";
    public static final String SELECTION_VALUES = "selectionValues";
    public static final String SELECTION_INDEX = "selectionIndex";
    public static final String INITIAL_VALUE = "initialValue";
    public static final String ICON = "Icon";
    public static final String DIALOG_TYPE = "DialogType";
    public static final String TITLE = "Title";
    public static final String MESSAGE = "Message";
    public static final String RETURN_VALUE = "returnValue";
    public static final String USER_INPUT = "userInput";

    private final Activity activityContext;
    private Intent intent;

    /**
     * Creates a new AndroidMessageDialog using the given Context.
     *
     * @param activityContext current ActivityContext
     */
    public AndroidMessageDialog(Activity activityContext) {
	this.activityContext = activityContext;
    }

    private MessageDialogResult getOKResult() {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title) {
	return showMessageDialog(message, title, null, null);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType) {
	return showMessageDialog(message, title, messageType, null);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(DIALOG_TYPE, messageType);
	intent.putExtra(ICON, iconData);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
	return getOKResult();
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title) {
	return showConfirmDialog(message, title, OptionType.YES_NO_CANCEL_OPTION, null, null);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType) {
	return showConfirmDialog(message, title, optionType, null, null);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType,
	    DialogType messageType) {
	return showConfirmDialog(message, title, optionType, messageType, null);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType,
	    DialogType messageType, byte[] iconData) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(OPTION_TYPE, optionType);
	intent.putExtra(DIALOG_TYPE, messageType);
	intent.putExtra(ICON, iconData);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
	return getOKResult();
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title) {
	return showInputDialog(message, title, null, "");
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, String initialValue) {
	return showInputDialog(message, title, null, initialValue);
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, String initialValue) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(INITIAL_VALUE, initialValue);
	intent.putExtra(DIALOG_TYPE, messageType);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
	return getOKResult();
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData,
	    int initialSelectionIndex, String... selectionValues) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(ICON, iconData);
	intent.putExtra(SELECTION_INDEX, initialSelectionIndex);
	intent.putExtra(SELECTION_VALUES, selectionValues);
	intent.putExtra(DIALOG_TYPE, messageType);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
	return getOKResult();
    }

    @Override
    public MessageDialogResult showOptionDialog(String message, String title, OptionType optionType,
	    DialogType messageType, byte[] iconData, String... options) {
	createBasicMessageDialogIntent(message, title);
	intent.putExtra(ICON, iconData);
	intent.putExtra(OPTIONS, options);
	intent.putExtra(OPTION_TYPE, optionType);
	intent.putExtra(DIALOG_TYPE, messageType);
	activityContext.startActivityForResult(intent, REQUEST_CODE);
	return getOKResult();
    }

    private void createBasicMessageDialogIntent(String message, String title) {
	intent = new Intent(activityContext, MessageDialogActivity.class);
	intent.putExtra(MESSAGE, message);
	intent.putExtra(TITLE, title);
    }

}
