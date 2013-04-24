/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import org.openecard.gui.message.DialogType;


/**
 * This Activity is used by {@link AndroidMessageDialog} to show a MessageDialog.
 * It uses the parameters given in the calling Intent to adapt the representation according to them.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class MessageDialogActivity extends Activity {

    private String message;
    private String title;
    private byte[] iconData;
    private DialogType dialogType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.message_dialog);

	Button okButton = (Button) findViewById(R.id.button_ok);
	okButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	}); 

	getParameters();
	fillLayout();
    }

    private void fillLayout() {
	setTitle(title);
	TextView description = (TextView) findViewById(R.id.message); 
	description.setText(message);
	ImageView iconView = (ImageView) findViewById(R.id.imageViewIcon);
	if (iconData == null) {
	    // fall back to a icon specified by the dialog type
	    if (dialogType != null) {
		switch (dialogType) {
		    case ERROR_MESSAGE:
			iconView.setImageResource(R.drawable.error);
			break;
		    case INFORMATION_MESSAGE:
			iconView.setImageResource(R.drawable.info);
		    	break;
		    case QUESTION_MESSAGE:
			iconView.setImageResource(R.drawable.question);
		    	break;
		    case WARNING_MESSAGE:
			iconView.setImageResource(R.drawable.warning);
		    	break;
		    default: // PLAIN_MESSAGE no icon
			break;
		}
	    }
	} else {
	    ByteArrayInputStream is = new ByteArrayInputStream(iconData);
	    Drawable drw = Drawable.createFromStream(is, "icon");
	    iconView.setImageDrawable(drw);
	}
    }

    private void getParameters() {
	Bundle extras = getIntent().getExtras();
	message = extras.getString(AndroidMessageDialog.MESSAGE);
	title = extras.getString(AndroidMessageDialog.TITLE);

	iconData = extras.getByteArray(AndroidMessageDialog.ICON);
	Serializable serializable = extras.getSerializable(AndroidMessageDialog.DIALOG_TYPE);
	if (serializable != null) {
	    dialogType = (DialogType) serializable;
	}
    }

}
