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
import android.widget.SimpleExpandableListAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.gui.definition.ToggleText;


/**
 * Adapter needed to fill View of StepActivity for ToggleTexts.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ToggleTextExpandableListAdapter extends SimpleExpandableListAdapter {

    /**
     *
     * @param context application context
     * @param toggleText the toggleText to display
     */
    public ToggleTextExpandableListAdapter(Context context, final ToggleText toggleText) {
	super(context, createGroupList(toggleText), R.layout.group_row, new String[] { "Title" }, new int[] { R.id.row_name },
		createChildList(toggleText), R.layout.child_row, new String[] { "Text" }, new int[] { R.id.grp_child });
    }

    /**
     *
     * @param toggleText the toggleText to display
     * @return list of childs (here we have only the child representing the text of the toggleText)
     */
    private static List<? extends List<? extends Map<String, ?>>> createChildList(final ToggleText toggleText) {
	ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();
	ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> child = new HashMap<String, String>();
	child.put("Text", toggleText.getText());
	secList.add(child);
	result.add(secList);
	return result;
    }

    /**
     *
     * @param toggleText the toggleText to display
     * @return list of groups (here we have only the group representing the title of the toggleText)
     */
    private static List<? extends Map<String, ?>> createGroupList(final ToggleText toggleText) {
	ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m = new HashMap<String, String>();
	m.put("Title", toggleText.getTitle());
	result.add(m);
	return result;
    }

}
