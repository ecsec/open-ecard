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
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import org.openecard.client.gui.android.StepActivity;
import org.openecard.client.gui.definition.OutputInfoUnit;

/**
 * Implementation of a simple text view for use in a {@link StepActivity}.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Text implements StepView {

	private TextView t;

    public Text(org.openecard.client.gui.definition.Text text, Context ctx) {
    	t = new TextView(ctx);
		t.setTextColor(Color.BLACK);
		t.setText(text.getText());
    }

    @Override
    public View getView() {
	return t;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }

}
