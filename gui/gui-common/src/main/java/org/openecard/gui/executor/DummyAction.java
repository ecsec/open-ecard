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

package org.openecard.gui.executor;

import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.Step;


/**
 * Dummy action to produce step results for the execution engine. <br/>
 * The DummyAction is a no-OP action, which always returns a result according to the following mapping:
 * <ul>
 * <li>{@link ResultStatus#BACK} → {@link StepActionResultStatus#BACK}</li>
 * <li>{@link ResultStatus#OK} → {@link StepActionResultStatus#NEXT}</li>
 * <li>* → {@link StepActionResultStatus#CANCEL}</li>
 * </ul>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DummyAction extends StepAction {

    /**
     * Creates a DummyAction for the given step.
     *
     * @param step The step the action should be associated with.
     */
    public DummyAction(Step step) {
	super(step);
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	// REPEAT must be performed explicitly
	switch (result.getStatus()) {
	    case BACK:
		return new StepActionResult(StepActionResultStatus.BACK);
	    case OK:
		return new StepActionResult(StepActionResultStatus.NEXT);
	    case CANCEL:
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    default: // for the sake of the mighty Java compiler
		return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

}
