/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import java.util.Map;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;


/**
 *
 * @author Tobias Wich
 */
public class CVCStepAction extends StepAction {

    private final BackgroundTask bTask;

    public CVCStepAction(Step step) {
	super(step);
	bTask = step.getBackgroundTask();
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isBack()) {
	    // no going back to the initialization step
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	}

	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);

	EACData eacData = (EACData) ctx.get(EACProtocol.EAC_DATA);
	CHATStep chatStep = new CHATStep(eacData);
	chatStep.setBackgroundTask(bTask);
	StepAction chatAction = new CHATStepAction(eacData, chatStep);
	chatStep.setAction(chatAction);
	return new StepActionResult(StepActionResultStatus.NEXT, chatStep);
    }

}
