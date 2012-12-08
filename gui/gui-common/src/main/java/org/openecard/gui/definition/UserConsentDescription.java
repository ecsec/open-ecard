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
 * Definition of a user consent.
 * This class is the parent element for steps.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class UserConsentDescription {

    private String title;
    private String dialogType;
    private ArrayList<Step> steps;

    /**
     * Creates a user consent description with the given title.
     * The dialog type is set to the empty string.
     *
     * @param title The title of the user consent.
     */
    public UserConsentDescription(String title) {
	this(title, "");
    }

    /**
     * Creates a user consent description with the given title and dialog type.
     *
     * @param title The title of the user consent.
     * @param dialogType The dialog type of the user consent. This must not be null.
     */
    public UserConsentDescription(String title, String dialogType) {
	this.title = title;
	this.dialogType = dialogType;
    }

    /**
     * Gets the title of this instance.
     *
     * @return The title of this instance.
     */
    public String getTitle() {
	return title;
    }
    /**
     * Sets the title of this instance.
     *
     * @param title The title of this instance.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * Gets the dialog type of this instance.
     * The dialog type is used to request specific behaviour of the user consent. For example an EAC user consent may
     * need special layouting, so the a dialog type URI http://openecard.org/uc/eac may be defined and implemented by
     * the user consent implementation.
     *
     * @return The dialog type, or the empty string if none is defined explicitly.
     */
    public String getDialogType() {
	return dialogType;
    }

    /**
     * Sets the dialog type of this instance.
     * The dialog type is used to request specific behaviour of the user consent. For example an EAC user consent may
     * need special layouting, so the a dialog type URI http://openecard.org/uc/eac may be defined and implemented by
     * the user consent implementation.
     *
     * @param dialogType The dialog type of this instance. The empty string may be used to reset the user consent to the
     *   default behaviour.
     */
    public void setDialogType(String dialogType) {
	this.dialogType = dialogType;
    }

    /**
     * Gets the list of steps for this user consent.
     * The returned list is modifiable and can be used to add and remove steps from the user consent.
     *
     * @return Modifiable list of the steps of this user consent.
     */
    public List<Step> getSteps() {
	if (steps == null) {
	    steps = new ArrayList<Step>();
	}
	return steps;
    }

}
