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
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.List;
import org.openecard.client.gui.definition.*;


/**
 * Adapter needed to fill View of StepActivity for boxitems.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class BoxItemAdapter extends BaseAdapter {

    private Context context;
    private String id;
    private List<BoxItem> boxItems;
    private boolean useCheckboxes;

    /**
     *
     * @param context application context
     * @param abstractBox abstractBox of type checkbox or radiobox
     * @param useCheckboxes true if checkboxes should be used, false for radioboxes
     */
    public BoxItemAdapter(Context context, AbstractBox abstractBox, boolean useCheckboxes) {
	this.useCheckboxes = useCheckboxes;
	this.context = context;
	this.id = abstractBox.getID();
	this.boxItems =  ((AbstractBox) abstractBox).getBoxItems();
    }

    @Override
    public int getCount() {
	return boxItems.size();
    }

    /* unused */
    @Override
    public Object getItem(int position) {
	return null;
    }

    /* unused */
    @Override
    public long getItemId(int position) {
	return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
	CompoundButton b = null;
	if(useCheckboxes){
	    b = new CheckBox(context);
	    b.setButtonDrawable(android.R.drawable.btn_radio);
	} else{
	    b = new RadioButton(context);
	    
	}
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
	AbstractBox result = useCheckboxes ? new Checkbox(id) :  new Radiobox(id);
	result.getBoxItems().addAll(boxItems);
	return result;
    }

}
