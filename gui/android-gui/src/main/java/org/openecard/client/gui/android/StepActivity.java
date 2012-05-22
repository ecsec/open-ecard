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

package org.openecard.client.gui.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.gui.android.views.StepView;
import org.openecard.client.gui.definition.*;


/**
 * This activity displays a specific step when showStep is called
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class StepActivity extends Activity {

    private TextView mTitle = null;
    LinearLayout ll;
    ArrayList<StepView> views = new ArrayList<StepView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	setContentView(R.layout.gui_interface);
	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
	mTitle = (TextView) findViewById(R.id.title_left_text);
	mTitle.setText(R.string.app_name);
	mTitle = (TextView) findViewById(R.id.title_right_text);
	ll = (LinearLayout) findViewById(R.id.linearLayoutGUIInterface);
	AndroidNavigator.getInstance().setActivity(this);
    }

    /**
     * Get result for all components on the frame that support result values.
     * 
     * @return List containg all result values. As a matter of fact this list
     *         can be empty.
     */
    public List<OutputInfoUnit> getResultContent() {
	ArrayList<OutputInfoUnit> result = new ArrayList<OutputInfoUnit>();
	for (StepView next : views) {
	    if (next.isValueType()) {
		result.add(next.getValue());
	    }
	}
	return result;
    }

    void showStep(final Step step2) {
	ll.post(new Runnable() {
	    @Override
	    public void run() {
		ll.removeAllViews();
		org.openecard.client.gui.definition.Step a = step2;
		if (a.getID() != null)
		    mTitle.setText(a.getID());
		org.openecard.client.gui.android.views.StepView t = null;
		for (InputInfoUnit infoUnitType : a.getInputInfoUnits()) {
		    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
			t = new org.openecard.client.gui.android.views.Text((Text) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
			t = new org.openecard.client.gui.android.views.Checkbox((Checkbox) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
			t = new org.openecard.client.gui.android.views.Radiobutton((Radiobox) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((PasswordField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.TEXT_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((TextField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.HYPERLINK)) {
			t = new org.openecard.client.gui.android.views.Hyperlink((Hyperlink) infoUnitType, StepActivity.this);
		    }
		    views.add(t);
		    ll.addView(t.getView());
		}

		Button cancel = (Button) findViewById(R.id.button_cancel);
		cancel.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    AndroidNavigator.getInstance().setStepResult(false, true, null);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});

		Button b = (Button) findViewById(R.id.button_back);
		b.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {

			try {
			    AndroidNavigator.getInstance().setStepResult(true, false, null);
			} catch (InterruptedException e) {

			    e.printStackTrace();
			}

		    }
		});

		Button next = (Button) findViewById(R.id.button_next);
		next.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    AndroidNavigator.getInstance().setStepResult(false, false, getResultContent());
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});
	    }
	});
    }

}
