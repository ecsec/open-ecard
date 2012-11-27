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
import java.util.concurrent.Callable;
import org.openecard.gui.StepResult;


/**
 * Wrapper class to embed a StepAction into a Callable, so that Futures can be created.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepActionCallable implements Callable<StepActionResult> {

    private final StepAction action;
    private final Map<String, ExecutionResults> oldResults;
    private final StepResult result;

    /**
     * Creates a wrapped StepAction with the parameters needed to execute the action.
     *
     * @param action Action to wrap.
     * @param oldResults First parameter of {@link StepAction#perform(java.util.Map, org.openecard.gui.StepResult)}.
     * @param result Second parameter of {@link StepAction#perform(java.util.Map, org.openecard.gui.StepResult)}.
     */
    public StepActionCallable(StepAction action, Map<String, ExecutionResults> oldResults, StepResult result) {
	this.action = action;
	this.oldResults = oldResults;
	this.result = result;
    }

    @Override
    public StepActionResult call() throws Exception {
	return action.perform(oldResults, result);
    }

}
