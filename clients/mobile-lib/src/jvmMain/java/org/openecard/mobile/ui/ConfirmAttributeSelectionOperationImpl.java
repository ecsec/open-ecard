/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.mobile.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.SelectableItem;


/**
 *
 * @author Tobias Wich
 */
public class ConfirmAttributeSelectionOperationImpl implements ConfirmAttributeSelectionOperation {

    private final Promise<List<OutputInfoUnit>> waitForAttributes;
    private final Checkbox readBox;
    private final Checkbox writeBox;

    ConfirmAttributeSelectionOperationImpl(Promise<List<OutputInfoUnit>> waitForAttributes, Checkbox readBox, Checkbox writeBox) {
	this.waitForAttributes = waitForAttributes;
	this.readBox = readBox;
	this.writeBox = writeBox;
    }

    @Override
    public void enterAttributeSelection(List<SelectableItem> readAttr, List<SelectableItem> writeAttr) {
	List<OutputInfoUnit> outInfo = convertSelection(readAttr, writeAttr);
	waitForAttributes.deliver(outInfo);
    }

    private List<OutputInfoUnit> convertSelection(List<SelectableItem> itemsRead, List<SelectableItem> itemsWrite) {
	List<OutputInfoUnit> outInfos = new ArrayList<>();

	copyBox(outInfos, readBox, itemsRead);
	copyBox(outInfos, writeBox, itemsWrite);

	return outInfos;
    }

    private void copyBox(List<OutputInfoUnit> outInfos, Checkbox oldBox, List<SelectableItem> items) {
	if (oldBox != null) {
	    Checkbox newBox = convert(oldBox, items);

	    outInfos.add(newBox);
	}
    }

    private Checkbox convert(Checkbox oldBox, List<SelectableItem> items) {
	// create copy of the checkbox
	Checkbox newBox = new Checkbox(oldBox.getID());
	newBox.copyContentFrom(oldBox);
	Map<String, SelectableItem> itemsByName = new HashMap<>();
	if (items != null) {
	    for (SelectableItem currentItem : items) {
		itemsByName.put(currentItem.getName(), currentItem);
	    }
	}
	// copy changed values
	for (BoxItem next : newBox.getBoxItems()) {
	    String name = next.name;
	    SelectableItem receivedItem = itemsByName.get(name);
	    if (receivedItem != null) {
		next.setChecked(receivedItem.isChecked());
	    }
	}
	return newBox;
    }

}
