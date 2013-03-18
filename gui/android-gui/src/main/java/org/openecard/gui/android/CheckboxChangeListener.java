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
import org.openecard.gui.definition.BoxItem;


/**
 * Alters the checked state of the Checkbox-BoxItem according to the new state of the Checkbox-view.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class CheckboxChangeListener implements OnCheckedChangeListener {

    private final BoxItem item;

    CheckboxChangeListener(BoxItem item) {
	this.item = item;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	item.setChecked(isChecked);
    }

}
