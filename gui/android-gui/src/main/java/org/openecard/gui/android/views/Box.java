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
import android.widget.GridView;
import org.openecard.gui.android.BoxItemAdapter;
import org.openecard.gui.definition.AbstractBox;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a radio- and checkbox group for use in a {@link StepActivity}.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Box implements StepView {

    private GridView gridview;
    private BoxItemAdapter boxItemAdapter;

    public Box(AbstractBox box, Context ctx, boolean useCheckBoxes) {
	gridview = new GridView(ctx) {
	    //workaround to get a gridview displayed in a scrollview
	    @Override
	    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Calculate entire height by providing a very large height hint.
		// But do not use the highest 2 bits of this integer; those are
		// reserved for the MeasureSpec mode.
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

		// enlarge by 50 because on some phones the measured height was to small
		expandSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() + 50, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, expandSpec);
		android.view.ViewGroup.LayoutParams params = getLayoutParams();
		params.height = getMeasuredHeight();
	    }
	};
	gridview.setColumnWidth(250);
	gridview.setNumColumns(GridView.AUTO_FIT);
	gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
	boxItemAdapter = new BoxItemAdapter(ctx, box, useCheckBoxes);
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
