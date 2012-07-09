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

package org.openecard.client.gui.android;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.List;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.InputInfoUnit;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 * Adapter needed to fill View of StepActivity for checkboxes
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class CheckBoxItemAdapter extends BaseAdapter {

    private Context context;
    private Checkbox input;
    private List<BoxItem> boxItems;

    public CheckBoxItemAdapter(Context c, InputInfoUnit i) {
	this.context = c;
	this.input = (Checkbox) i;
	this.boxItems = input.getBoxItems();
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
	CompoundButton b = new CheckBox(context);
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
	Checkbox result = new Checkbox(input.getID());
	result.getBoxItems().addAll(boxItems);
	return result;
    }

}
