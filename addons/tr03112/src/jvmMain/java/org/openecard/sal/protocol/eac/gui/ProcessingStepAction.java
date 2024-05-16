/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.util.Promise;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action waiting for the EAC process to finish.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class ProcessingStepAction extends StepAction {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingStepAction.class);

    private final DynamicContext ctx;

    public ProcessingStepAction(Step step) {
	super(step);
	ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	Promise<Object> pAuthDone = ctx.getPromise(EACProtocol.AUTHENTICATION_DONE);
	try {
	    pAuthDone.deref(120, TimeUnit.SECONDS);
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (InterruptedException ex) {
	    LOG.error("ProcessingStepAction interrupted by the user or an other thread.", ex);
	    ctx.put(EACProtocol.PACE_EXCEPTION, WSHelper.createException(WSHelper.makeResultError(
		    ECardConstants.Minor.SAL.CANCELLATION_BY_USER, "User canceled the EAC dialog.")));
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (TimeoutException ex) {
	    LOG.info("Timeout while waiting for the authentication to finish.", ex);
	    ctx.put(EACProtocol.PACE_EXCEPTION, WSHelper.createException(WSHelper.makeResultError(
		    ECardConstants.Minor.Disp.TIMEOUT, "Timeout during EAC process.")));
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

}
