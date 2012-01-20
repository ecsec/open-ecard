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

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import java.util.List;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.InputInfoUnit;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Radiobox;

/**
 * Adapter needed to fill View of StepActivity for radioboxes
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class RadioBoxItemAdapter extends BaseAdapter {

    private Context context;
    private List<BoxItem> boxItems;

    public RadioBoxItemAdapter(Context c, InputInfoUnit i) {
	this.context = c;
	this.boxItems = ((Radiobox) i).getBoxItems();
    }

    public int getCount() {
	return boxItems.size();
    }

    /* unused */
    public Object getItem(int position) {
	return null;
    }

    /* unused */
    public long getItemId(int position) {
	return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
	CompoundButton b = new RadioButton(context);
	b.setTextColor(Color.BLACK);
	b.setText(boxItems.get(position).getText() != null ? boxItems.get(position).getText() : boxItems.get(position).getName());
	b.setChecked(boxItems.get(position).isChecked());
	b.setEnabled(!boxItems.get(position).isDisabled());
	b.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		boxItems.get(position).setChecked(isChecked);
	    }
	});
	return b;
    }

    public OutputInfoUnit getValue() {
	org.openecard.client.gui.definition.Radiobox result = new org.openecard.client.gui.definition.Radiobox();
	result.getBoxItems().addAll(boxItems);
	return result;
    }
}