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
import android.widget.GridView;
import org.openecard.client.gui.android.RadioBoxItemAdapter;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 * Implementation of a radiobox group for use in a {@link StepActivity}.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Radiobutton implements StepView {

    private GridView gridview;
    private RadioBoxItemAdapter boxItemAdapter;

    public Radiobutton(org.openecard.client.gui.definition.Radiobox radiobox, Context ctx) {
	gridview = new GridView(ctx);
	gridview.setColumnWidth(250);
	gridview.setNumColumns(-1); // auto fit
	gridview.setStretchMode(2); // stretch column
	boxItemAdapter = new RadioBoxItemAdapter(ctx, radiobox);
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
