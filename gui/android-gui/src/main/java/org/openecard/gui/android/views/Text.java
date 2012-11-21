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
import android.widget.TextView;
import org.openecard.gui.android.StepActivity;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple text view for use in a {@link StepActivity}.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Text implements StepView {

    private TextView t;

    public Text(org.openecard.gui.definition.Text text, Context ctx) {
	t = new TextView(ctx);
	t.setText(text.getText());
    }

    @Override
    public View getView() {
	return t;
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
