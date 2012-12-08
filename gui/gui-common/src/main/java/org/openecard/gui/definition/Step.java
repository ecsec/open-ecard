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
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;


/**
 * Description class for user consent steps.
 * Each step represents one dialog.
 *
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

    /**
     * Creates a step with the given title and a generated ID.
     *
     * @see #setTitle(java.lang.String)
     * @param title Title string of the step.
     */
    public Step(String title) {
	this(ValueGenerators.generateSessionID(), title);
    }

    /**
     * Creates a step with the given title and the given ID.
     *
     * @see #setTitle(java.lang.String)
     * @param id The ID to initialize the step with.
     * @param title Title string of the step.
     */
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

    /**
     * Gets the description of this step.
     * The description may be used as a subtitle.
     *
     * @return The description of this step.
     */
    public String getDescription() {
	return description;
    }
    /**
     * Sets the description of this step.
     * The description may be used as a subtitle.
     *
     * @param description The description of this step.
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * Gets the title of this step.
     * The title may be used in a progress indicator and in a title element.
     *
     * @return The title of this step.
     */
    public String getTitle() {
	return title;
    }
    /**
     * Sets the title of this step.
     * The title may be used in a progress indicator and in a title element.
     *
     * @param title The title of this step.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * Gets whether the step allows to go back to the previous step.
     *
     * @return {@code true} if this step allows to go back, {@code false} otherwise.
     */
    public boolean isReversible() {
	return reversible;
    }
    /**
     * Sets whether the step allows to go back to the previous step.
     *
     * @param reversible {@code true} if this step allows to go back, {@code false} otherwise.
     */
    public void setReversible(boolean reversible) {
	this.reversible = reversible;
    }

    /**
     * Gets whether the step returns instantly after it is shown.
     * This feature is only useful in combination with step actions. An action can perform lengthy operations. A step
     * with instant return set can inform the user that such a lengthy operation takes place, but does not want the user
     * to tell the dialog to proceed, because there is no user interaction necessary.
     *
     * @see #getAction()
     * @return {@code true} if this step is configured to return instantly, {@code false} otherwise.
     */
    public boolean isInstantReturn() {
	return instantReturn;
    }
    /**
     * Sets whether the step returns instantly after it is shown.
     * This feature is only useful in combination with step actions. An action can perform lengthy operations. A step
     * with instant return set can inform the user that such a lengthy operation takes place, but does not want the user
     * to tell the dialog to proceed, because there is no user interaction necessary.
     *
     * @see #getAction()
     * @param instantReturn {@code true} if this step is configured to return instantly, {@code false} otherwise.
     */
    public void setInstantReturn(boolean instantReturn) {
	this.instantReturn = instantReturn;
    }

    /**
     * Gets whether the elements' values on this step reset to their default values, when the step is shown again.
     *
     * @return {@code true} if this step resets its values, {@code false} otherwise.
     */
    public boolean isResetOnLoad() {
	return resetOnLoad;
    }
    /**
     * Sets whether the elements' values on this step reset to their default values, when the step is shown again.
     *
     * @param resetOnLoad {@code true} if this step resets its values, {@code false} otherwise.
     */
    public void setResetOnLoad(boolean resetOnLoad) {
	this.resetOnLoad = resetOnLoad;
    }


    /**
     * Gets the list of elements of this step.
     * The returned list is modifiable and can be used to add and remove elements from the step.
     *
     * @return Modifiable list of the elements of this step.
     */
    public List<InputInfoUnit> getInputInfoUnits() {
	if (inputInfoUnits == null) {
	    inputInfoUnits = new ArrayList<InputInfoUnit>();
	}
	return inputInfoUnits;
    }

    public boolean isMetaStep() {
	return getInputInfoUnits().isEmpty();
    }

    /**
     * Gets the action associated with this step.
     * Actions are a way to bind code to the step which is executed after the step is finished. The
     * {@link ExecutionEngine} takes care of the action execution.
     *
     * @return The action associated with this step, or a {@link DummyAction} if none is set.
     */
    public StepAction getAction() {
	if (action == null) {
	    return new DummyAction(this);
	}
	return action;
    }
    /**
     * Sets the action to be associated with this step.
     * Actions are a way to bind code to the step which is executed after the step is finished. The
     * {@link ExecutionEngine} takes care of the action execution.
     *
     * @param action The action to be associated with this step, or {@code null} if the current action should be
     *   removed.
     */
    public void setAction(StepAction action) {
	this.action = action;
    }

}
