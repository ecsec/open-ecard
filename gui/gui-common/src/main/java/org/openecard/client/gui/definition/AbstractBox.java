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

package org.openecard.client.gui.definition;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractBox implements InputInfoUnit, OutputInfoUnit {

    private String groupText;
    private List<BoxItem> boxItems;

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

}
