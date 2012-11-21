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
import android.view.View;
import android.widget.ExpandableListView;
import org.openecard.gui.android.StepActivity;
import org.openecard.gui.android.ToggleTextExpandableListAdapter;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a view for ToggleText for use in a {@link StepActivity}.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ToggleText implements StepView {

    private ExpandableListView elv;

    public ToggleText(org.openecard.gui.definition.ToggleText toggleText, Context ctx) {

	elv = new ExpandableListView(ctx){
	    //workaround to get a ExpandableListView displayed in a ScrollView
	    @Override
	    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Calculate entire height by providing a very large height hint.
		// But do not use the highest 2 bits of this integer; those are
		// reserved for the MeasureSpec mode.
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

		android.view.ViewGroup.LayoutParams params = getLayoutParams();
		params.height = getMeasuredHeight();
	    }
	};
	elv.setAdapter(new ToggleTextExpandableListAdapter(ctx, toggleText));
	if(!toggleText.isCollapsed()) {
	    elv.expandGroup(0);
	}
    }

    @Override
    public View getView() {
	return elv;
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
