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


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class UserConsentDescription {

    private String title;
    private String dialogType;
    private ArrayList<Step> steps;

    public UserConsentDescription(String title) {
	this(title, "");
    }

    public UserConsentDescription(String title, String dialogType) {
	this.title = title;
	this.dialogType = dialogType;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @return the dialogType
     */
    public String getDialogType() {
	return dialogType;
    }

    /**
     * @param dialogType the dialogType to set
     */
    public void setDialogType(String dialogType) {
	this.dialogType = dialogType;
    }

    public List<Step> getSteps() {
	if (steps == null) {
	    steps = new ArrayList<Step>();
	}
	return steps;
    }

}
