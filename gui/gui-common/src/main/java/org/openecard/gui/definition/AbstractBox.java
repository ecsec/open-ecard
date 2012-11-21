/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.gui.definition;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractBox extends IDTrait implements InputInfoUnit, OutputInfoUnit {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractBox.class);

    private String groupText;
    private List<BoxItem> boxItems;

    public AbstractBox(String id) {
	super(id);
    }


    /**
     * @return the groupText
     */
    public String getGroupText() {
	return groupText;
    }

    /**
     * @param groupText the groupText to set
     */
    public void setGroupText(String groupText) {
	this.groupText = groupText;
    }

    public List<BoxItem> getBoxItems() {
	if (boxItems == null) {
	    boxItems = new ArrayList<BoxItem>();
	}
	return boxItems;
    }


    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (!(this.getClass().equals(origin.getClass()))) {
	    _logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	AbstractBox other = (AbstractBox) origin;
	// do copy
	this.groupText = other.groupText;
	this.getBoxItems().clear();
	for (BoxItem next : other.getBoxItems()) {
	    BoxItem copy = new BoxItem();
	    copy.setChecked(next.isChecked());
	    copy.setDisabled(next.isDisabled());
	    copy.setName(next.getName());
	    copy.setText(next.getText());
	    this.getBoxItems().add(copy);
	}
    }

}
