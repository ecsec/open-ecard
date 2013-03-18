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

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.List;
import org.openecard.gui.definition.BoxItem;


/**
 * Alters the checked state of the Radiobox-BoxItem according to the new state of the Radiobox-view.
 * All other Radiobox-BoxItems and their Views will be unchecked.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class RadioboxChangeListener implements OnCheckedChangeListener {

    private final int position;
    private final List<BoxItem> boxItems;
    private final CompoundButton[] itemViews;

    RadioboxChangeListener(BoxItemAdapter boxItemAdapter, int position) {
	this.position = position;
	boxItems = boxItemAdapter.getBoxItems();
	itemViews = boxItemAdapter.getItems();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	if (isChecked) {
	    for (int i = 0; i < boxItems.size(); i++) {
		if (i == position) {
		    boxItems.get(i).setChecked(true);
		} else {
		    boxItems.get(i).setChecked(false);
		    if (itemViews[i] != null) {
			itemViews[i].setChecked(false);
		    }
		}
	    }
	}
    }

}
