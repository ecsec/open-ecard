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

package org.openecard.gui.android.views;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.openecard.gui.definition.AbstractTextField;
import org.openecard.gui.definition.InfoUnitElementType;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.TextField;


/**
 * Common base for {@link Textinput} and {@link Passwordinput}.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AbstractInput implements StepView {

    private final AbstractTextField result;
    private TableLayout tbl;
    private TextView tv;
    private EditText et;

    public AbstractInput(AbstractTextField input, Context ctx) {
	tv = new TextView(ctx);
	WindowManager mgr = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
	Display display = mgr.getDefaultDisplay();

	if (input.getDescription() != null) {
	    tv.setText(input.getDescription());
	} else {
	    tv.setText(input.getID());
	}
	tv.setWidth(display.getWidth() / 2);
	tv.setGravity(Gravity.CENTER | Gravity.BOTTOM);

	et = new EditText(ctx);
	et.setLines(1);
	et.setWidth(display.getWidth() / 2);
	et.setMinimumWidth(input.getMinLength() * 20);
	et.requestFocus();
	if (input.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
	    et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	}
	if (input.getMaxLength() > 0) {
	    InputFilter[] FilterArray = new InputFilter[1];
	    FilterArray[0] = new InputFilter.LengthFilter(input.getMaxLength());
	    et.setFilters(FilterArray);
	}
	if (input.getValue() != null) {
	    et.setText(input.getValue());
	}
	tbl = new TableLayout(ctx);
	TableRow tbr = new TableRow(ctx);
	tbr.addView(tv);
	tbr.addView(et);
	tbl.addView(tbr);
	if (input.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
	    result = new PasswordField(input.getID());
	} else {
	    result = new TextField(input.getID());
	}
	result.setMinLength(input.getMinLength());
	result.setMaxLength(input.getMaxLength());
	result.setValue(input.getValue());
	result.setID(input.getID());
    }

    @Override
    public View getView() {
	return this.tbl;
    }

    @Override
    public boolean validate() {
	/*
	 * String textValue = this.textField.getText(); if (textValue == null) {
	 * textValue = ""; } int textSize = textValue.length(); // min <= text
	 * && text <= max if (minLength <= textSize && textSize <= maxLength) {
	 * return true; } else { return false; }
	 */
	// TODO
	return true;
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	String textValue = "";
	if (et.getText() != null) {
	    textValue = this.et.getText().toString();
	}

	result.setValue(textValue);
	return result;
    }

}
