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

package org.openecard.gui.swing.steplayout;

import java.awt.Container;
import java.util.List;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.swing.components.StepComponent;


/**
 * Abstract base class to retrieve layouted components. <br>
 * This class is also used to instantiate an implementation of a layouter
 * depending on the parameters (see static create function).
 *
 * @author Tobias Wich
 */
public abstract class StepLayouter {

    /**
     * Create a layouter instance deping on the dialog type and/or the individual step name.
     * The newly created instance deals with the layouting of the components described in infoUnits.
     *
     * @param infoUnits Abstract description of the components in the step.
     * @param dialogType URI describing the type of the dialog. Empty string when none is given.
     * @param stepName Name of the step. This can be used to have a different layouter for disclaimer and pin entry step for example.
     * @return Layouter which can return panel and components list.
     */
    public static StepLayouter create(List<InputInfoUnit> infoUnits, String dialogType, String stepName) {
	StepLayouter layouter = null;

	// select method to create components
	// it is even possible to use different layouters for the individual steps (see stepName)
	if (dialogType.equals("somefancy dialog type like nPa-eID")) {
	    // TODO: create and return
	}

	if (stepName.equals("some step with special layout needs")) {
	    // TODO: create and return
	}

	// default type if nothing happened so far
	if (layouter == null) {
	    layouter = new DefaultStepLayouter(infoUnits, stepName);
	}

	return layouter;
    }

    /**
     * Get the list of components which have been placed onto the container.
     *
     * @return
     */
    public abstract List<StepComponent> getComponents();

    /**
     * Get panel with layouted components, so it can be embedded in the frame.
     *
     * @return Container panel
     */
    public abstract Container getPanel();

}
