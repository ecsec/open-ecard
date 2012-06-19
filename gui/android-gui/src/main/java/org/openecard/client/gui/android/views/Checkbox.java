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

package org.openecard.client.gui.android.views;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.GridView;
import org.openecard.client.gui.android.CheckBoxItemAdapter;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 * Implementation of a checkbox group for use in a {@link StepFrame}.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Checkbox implements StepView {

    private GridView gridview;
    private CheckBoxItemAdapter boxItemAdapter;

    public Checkbox(org.openecard.client.gui.definition.Checkbox checkbox, Context ctx) {
	gridview = new GridView(ctx) {
	    //workaround to get a gridview displayed in a scrollview
	    @Override 
	    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	                 
	            // Calculate entire height by providing a very large height hint.
	            // But do not use the highest 2 bits of this integer; those are
	            // reserved for the MeasureSpec mode.
	            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
	                        MeasureSpec.AT_MOST);
	            super.onMeasure(widthMeasureSpec, expandSpec);

	            android.view.ViewGroup.LayoutParams params = getLayoutParams();
	            params.height = getMeasuredHeight();
	        
	    }
	};
	
	gridview.setColumnWidth(250);
	gridview.setNumColumns(-1); // auto fit
	gridview.setStretchMode(2); // stretch column
	boxItemAdapter = new CheckBoxItemAdapter(ctx, checkbox);
	gridview.setAdapter(boxItemAdapter);
	        
    }

    

    @Override
    public View getView() {
	return gridview;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	return boxItemAdapter.getValue();
    }

}
