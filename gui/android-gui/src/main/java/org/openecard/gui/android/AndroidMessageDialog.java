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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;


/**
 * Android based MessageDialog implementation.
 * This implementation wraps the {@link AlertDialog} class.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMessageDialog implements MessageDialog {

    private final AlertDialog ad;

    /**
     * Creates a new AndroidMessageDialog using the given Context.
     *
     * @param context current Context of the App
     */
    public AndroidMessageDialog(Context context) {
	ad = new AlertDialog.Builder(context).create();
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title) {
	prepareAlertDialogForShowMessage(message);
	ad.show();
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType) {
	// TODO messageType is currently ignored
	prepareAlertDialogForShowMessage(message);
	ad.setTitle(title);
	ad.show();
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData) {
	// TODO messageType is currently ignored
	prepareAlertDialogForShowMessage(message);
	ad.setTitle(title);
	Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(iconData, 0, iconData.length));
	ad.setIcon(image);
	ad.show();
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

    private void prepareAlertDialogForShowMessage(String message) {
	ad.setCancelable(false); // This blocks the 'BACK' button
	ad.setMessage(message);
	ad.setButton("OK", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	    }
	});
    }

}
