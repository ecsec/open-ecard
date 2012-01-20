/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.gui.android.views;

import android.content.Context;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.openecard.client.gui.definition.AbstractTextfield;
import org.openecard.client.gui.definition.InfoUnitElementType;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Textfield;

/**
 * <p>
 * Common base for {@link Textinput} and {@link Passwordinput}.
 * <p/>
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AbstractInput implements StepView {

	private final AbstractTextfield result;
	private TableLayout tbl;
	private TextView tv;

	public AbstractInput(AbstractTextfield input, Context ctx) {
		tv = new TextView(ctx);
		if (input.getText() != null)
			tv.setText(input.getText());
		else
			tv.setText(input.getName());
		EditText et = new EditText(ctx);
		et.setLines(1);
		if (input.type().equals(InfoUnitElementType.Passwordfield)) {
			et.setTransformationMethod(new PasswordTransformationMethod());
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
		if (input.type().equals(InfoUnitElementType.Passwordfield)) {
			result = new Passwordfield();
		} else {
			result = new Textfield();
		}
		result.setMinLength(input.getMinLength());
		result.setMaxLength(input.getMaxLength());
		result.setName(input.getName());
		result.setText(input.getText());
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
		String textValue = null;
		if (this.tv.getText() != null)
			textValue = this.tv.getText().toString();
		if (textValue == null) {
			textValue = "";
		}
		result.setValue(textValue);
		return result;
	}
}
