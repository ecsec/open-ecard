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
package org.openecard.gui.swing.steplayout

import org.openecard.gui.definition.InputInfoUnit
import org.openecard.gui.swing.components.StepComponent
import java.awt.Container

/**
 * Abstract base class to retrieve layouted components. <br></br>
 * This class is also used to instantiate an implementation of a layouter
 * depending on the parameters (see static create function).
 *
 * @author Tobias Wich
 */
abstract class StepLayouter {
	/**
	 * Get the list of components which have been placed onto the container.
	 *
	 * @return
	 */
	abstract val components: MutableList<StepComponent>

	/**
	 * Get panel with layouted components, so it can be embedded in the frame.
	 *
	 * @return Container panel
	 */
	abstract val panel: Container

	companion object {
		/**
		 * Create a layouter instance deping on the dialog type and/or the individual step name.
		 * The newly created instance deals with the layouting of the components described in infoUnits.
		 *
		 * @param infoUnits Abstract description of the components in the step.
		 * @param dialogType URI describing the type of the dialog. Empty string when none is given.
		 * @param stepName Name of the step. This can be used to have a different layouter for disclaimer and pin entry step for example.
		 * @return Layouter which can return panel and components list.
		 */
		@JvmStatic
		fun create(
			infoUnits: MutableList<InputInfoUnit>,
			dialogType: String,
			stepName: String,
		): StepLayouter {
			var layouter: StepLayouter? = null

			// 	// select method to create components
// 	// it is even possible to use different layouters for the individual steps (see stepName)
// 	if (dialogType.equals("somefancy dialog type like nPa-eID")) {
// 	    // TODO: create and return
// 	}
//
// 	if (stepName.equals("some step with special layout needs")) {
// 	    // TODO: create and return
// 	}

			// chipgateway PIN or Update dialog
			if (dialogType == "pin_entry_dialog" ||
				dialogType == "pin_change_dialog" ||
				dialogType == "update_dialog"
			) {
				layouter = PinEntryStepLayouter(infoUnits, stepName)
			}

			// default type if nothing happened so far
			if (layouter == null) {
				layouter = DefaultStepLayouter(infoUnits, stepName)
			}

			return layouter
		}
	}
}
