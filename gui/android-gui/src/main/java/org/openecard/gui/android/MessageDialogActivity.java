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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import org.openecard.common.I18n;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;


/**
 * This Activity is used by {@link AndroidMessageDialog} to show a MessageDialog.
 * It uses the parameters given in the calling Intent to adapt the representation according to them.
 *
 * @author Dirk Petrautzki
 */
public class MessageDialogActivity extends Activity {

    private final I18n lang = I18n.getTranslation("gui");

    private static final int RESULT_CODE = 2;
    private String message;
    private String title;
    private byte[] iconData;
    private DialogType dialogType;
    private String initialValue;
    private String[] selectionValues;
    private Integer selectionIndex;
    private OptionType optionType;
    private Button okButton;
    private Button noButton;
    private String[] options;
    private Button cancelButton;
    private EditText editText;
    private Spinner s;
    private Intent resultData = new Intent();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.message_dialog);

	setupButtons();
	getParameters();
	fillLayout();
    }

    private void setupButtons() {
	okButton = (Button) findViewById(R.id.button_ok);
	okButton.setText(lang.translationForKey("button.ok"));
	okButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		if (initialValue != null) {
		    resultData.putExtra(AndroidMessageDialog.USER_INPUT, editText.getText().toString());
		} else if (selectionValues != null) {
		    resultData.putExtra(AndroidMessageDialog.USER_INPUT, selectionValues[s.getSelectedItemPosition()]);
		} else {
		    resultData.putExtra(AndroidMessageDialog.RETURN_VALUE, ReturnType.OK);
		}
		setResult(RESULT_CODE, resultData);
		finish();
	    }
	});
	noButton = (Button) findViewById(R.id.button_no);
	noButton.setText(lang.translationForKey("button.no"));
	noButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		resultData.putExtra(AndroidMessageDialog.RETURN_VALUE, ReturnType.NO);
		setResult(RESULT_CODE, resultData);
		finish();
	    }
	});
	cancelButton = (Button) findViewById(R.id.button_cancel);
	cancelButton.setText(lang.translationForKey("button.cancel"));
	cancelButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		resultData.putExtra(AndroidMessageDialog.RETURN_VALUE, ReturnType.CANCEL);
		setResult(RESULT_CODE, resultData);
		finish();
	    }
	});
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
	    } else { // iconData and dialogType are not set
		if (initialValue != null) {
		    iconView.setImageResource(R.drawable.question);
		} else {
		    iconView.setImageResource(R.drawable.info);
		}
	    }
	} else {
	    ByteArrayInputStream is = new ByteArrayInputStream(iconData);
	    Drawable drw = Drawable.createFromStream(is, "icon");
	    iconView.setImageDrawable(drw);
	}
	editText = (EditText) findViewById(R.id.userInput);
	if (initialValue != null) {
	    editText.setText(initialValue);
	} else {
	    editText.setVisibility(View.GONE);
	}
	s = (Spinner) findViewById(R.id.spinner);
	if (selectionValues != null) {
	    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		    android.R.layout.simple_spinner_item, selectionValues);
	    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    s.setAdapter(dataAdapter);
	    s.setSelection(selectionIndex);
	} else {
	    s.setVisibility(View.GONE);
	}
	if (optionType != null) {
	    switch (optionType) {
		case OK_CANCEL_OPTION:
		    noButton.setVisibility(View.GONE);
		    break;

		case YES_NO_OPTION:
		    cancelButton.setVisibility(View.GONE);
		    // fall through

		default:  // YES_NO_CANCEL_OPTION
		    okButton.setText(lang.translationForKey("button.yes"));
		    break;
	    }
	} else {
	    // only OK button is visible
	    noButton.setVisibility(View.GONE);
	    cancelButton.setVisibility(View.GONE);
	}
	if (options != null) {
	    LinearLayout linearLayoutButtons = (LinearLayout) findViewById(R.id.linearLayoutButtons);
	    linearLayoutButtons.removeAllViews();

	    for (final String option : options) {
		Button b = new Button(this);
		b.setText(option);
		b.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			resultData.putExtra(AndroidMessageDialog.USER_INPUT, option);
			setResult(RESULT_CODE, resultData);
			finish();
		    }
		});
		linearLayoutButtons.addView(b);
	    }
	}
    }

    /**
     * Extract the parameters from the calling intent.
     */
    private void getParameters() {
	Bundle extras = getIntent().getExtras();
	message = extras.getString(AndroidMessageDialog.MESSAGE);
	title = extras.getString(AndroidMessageDialog.TITLE);

	iconData = extras.getByteArray(AndroidMessageDialog.ICON);
	Serializable serializable = extras.getSerializable(AndroidMessageDialog.DIALOG_TYPE);
	if (serializable != null) {
	    dialogType = (DialogType) serializable;
	}
	initialValue = extras.getString(AndroidMessageDialog.INITIAL_VALUE);
	selectionValues = extras.getStringArray(AndroidMessageDialog.SELECTION_VALUES);
	selectionIndex = extras.getInt(AndroidMessageDialog.SELECTION_INDEX);
	serializable = extras.getSerializable(AndroidMessageDialog.OPTION_TYPE);
	if (serializable != null) {
	    optionType = (OptionType)  serializable;
	}
	options = extras.getStringArray(AndroidMessageDialog.OPTIONS);
    }

}
