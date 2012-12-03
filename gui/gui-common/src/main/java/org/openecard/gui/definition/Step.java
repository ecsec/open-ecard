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
import org.openecard.common.util.ValueGenerators;
import org.openecard.gui.executor.DummyAction;
import org.openecard.gui.executor.StepAction;


/**
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Step {

    private String id;
    private String title;
    private String description;
    private StepAction action;
    private boolean reversible = true;
    private boolean instantReturn = false;
    private boolean resetOnLoad = false;
    private List<InputInfoUnit> inputInfoUnits;

    public Step(String title) {
	this(ValueGenerators.generateSessionID(), title);
    }

    public Step(String id, String title) {
	this.id = id;
	this.title = title;
    }


    public String getID() {
	return id;
    }

    public void setID(String id) {
	this.id = id;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public boolean isReversible() {
	return reversible;
    }

    public void setReversible(boolean reversible) {
	this.reversible = reversible;
    }

    public boolean isInstantReturn() {
	return instantReturn;
    }

    public void setInstantReturn(boolean instantReturn) {
	this.instantReturn = instantReturn;
    }

    public boolean isResetOnLoad() {
	return resetOnLoad;
    }

    public void setResetOnLoad(boolean resetOnLoad) {
	this.resetOnLoad = resetOnLoad;
    }


    public List<InputInfoUnit> getInputInfoUnits() {
	if (inputInfoUnits == null) {
	    inputInfoUnits = new ArrayList<InputInfoUnit>();
	}
	return inputInfoUnits;
    }

    public boolean isMetaStep() {
	return getInputInfoUnits().isEmpty();
    }

    public StepAction getAction() {
	if (action == null) {
	    return new DummyAction(this);
	}
	return action;
    }

    public void setAction(StepAction action) {
	this.action = action;
    }

}
