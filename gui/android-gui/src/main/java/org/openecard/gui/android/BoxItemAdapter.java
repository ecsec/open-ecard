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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import java.util.List;
import org.openecard.gui.definition.AbstractBox;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Radiobox;


/**
 * Adapter needed to fill View of StepActivity for boxitems.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class BoxItemAdapter extends BaseAdapter {

    private final Context context;
    private final String id;
    private final List<BoxItem> boxItems;
    private final boolean useCheckboxes;
    private final CompoundButton[] itemViews;

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
	this.boxItems =  abstractBox.getBoxItems();
	itemViews = new CompoundButton[boxItems.size()];
    }

    @Override
    public int getCount() {
	return boxItems.size();
    }

    // unused
    @Override
    public Object getItem(int position) {
	return null;
    }

    // unused
    @Override
    public long getItemId(int position) {
	return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
	CompoundButton b;
	if (useCheckboxes) {
	    b = new CheckBox(context);
	    b.setOnCheckedChangeListener(new CheckboxChangeListener(boxItems.get(position)));
	} else {
	    b = new RadioButton(context);
	    b.setOnCheckedChangeListener(new RadioboxChangeListener(this, position));
	}
	String text = boxItems.get(position).getText();
	String name = boxItems.get(position).getName();
	b.setText(text != null ? text : name);
	b.setChecked(boxItems.get(position).isChecked());
	b.setEnabled(!boxItems.get(position).isDisabled());
	itemViews[position] = b;
	return b;
    }

    public OutputInfoUnit getValue() {
	AbstractBox result = useCheckboxes ? new Checkbox(id) :  new Radiobox(id);
	result.getBoxItems().addAll(boxItems);
	return result;
    }

    public List<BoxItem> getBoxItems() {
	return boxItems;
    }

    public CompoundButton[] getItems() {
	return itemViews;
    }

}
