/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class capable of displaying and executing a user consent. <br>
 * This class is a helper to display the steps of a user consent. It displays one after the other and reacts differently
 * depending of the outcome of a step. It also executes actions associated with the steps after they are finished.
 *
 * @author Tobias Wich
 */
public class ExecutionEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionEngine.class);

    private final UserConsentNavigator navigator;
    private final TreeMap<String, ExecutionResults> results = new TreeMap<>();


    /**
     * Creates an ExecutionEngine instance and initializes it with the given navigator.
     * The navigator must be previously obtained from a user consent implementation.
     *
     * @param navigator The navigator used to initialize this instance with.
     */
    public ExecutionEngine(UserConsentNavigator navigator) {
	this.navigator = navigator;
    }

    /**
     * Processes the user consent associated with this instance. <br>
     * The following algorithm is used to process the dialog.
     * <ol>
     * <li>Display the first step.</li>
     * <li>Evaluate step result. Break execution on CANCEL.</li>
     * <li>Execute step action. Break execution on CANCEL.</li>
     * <li>Display either next previous or current step, or a replacement according to result.</li>
     * <li>Proceed with point 2.</li>
     * </ol>
     *
     * @return Overall result of the execution.
     * @throws ThreadTerminateException Thrown in case the GUI has been closed externally (interrupted).
     */
    public ResultStatus process() throws ThreadTerminateException {
	try {
	    StepResult next = navigator.next(); // get first step
	    // loop over steps. break inside loop
	    while (true) {
		ResultStatus result = next.getStatus();
		LOG.debug("Step {} finished with result {}.", next.getStepID(), result);
		// close dialog on cancel and interrupt
		if (result == ResultStatus.INTERRUPTED || Thread.currentThread().isInterrupted()) {
		    throw new ThreadTerminateException("GUI has been interrupted.");
		} else if (result == ResultStatus.CANCEL) {
		    return result;
		}

		// get result and put it in resultmap
		List<OutputInfoUnit> stepResults = next.getResults();
		Map<String, ExecutionResults> oldResults = Collections.unmodifiableMap(results);
		results.put(next.getStepID(), new ExecutionResults(next.getStepID(), stepResults));

		// replace InfoInputUnit values in live list
		if (! next.getStep().isResetOnLoad()) {
		    Step s = next.getStep();
		    List<InputInfoUnit> inputInfo = s.getInputInfoUnits();
		    Map<String, InputInfoUnit> infoMap = new HashMap<>();
		    // create index over infos
		    for (InputInfoUnit nextInfo : inputInfo) {
			infoMap.put(nextInfo.getID(), nextInfo);
		    }
		    for (OutputInfoUnit nextOut : stepResults) {
			InputInfoUnit matchingInfo = infoMap.get(nextOut.getID());
			// an entry must exist, otherwise this is an error in the GUI implementation
			// this type of error should be found in tests
			matchingInfo.copyContentFrom(nextOut);
		    }
		}

		// replace step if told by result value
		Step replaceStep = next.getReplacement();
		if (replaceStep != null) {
		    LOG.debug("Replacing with step.id={}.", replaceStep.getID());
		    switch (next.getStatus()) {
			case BACK:
			    next = navigator.replacePrevious(replaceStep);
			    break;
			case OK:
			    if (navigator.hasNext()) {
				next = navigator.replaceNext(replaceStep);
			    } else {
				return convertStatus(StepActionResultStatus.NEXT);
			    }
			    break;
			case RELOAD:
			    next = navigator.replaceCurrent(replaceStep);
			    break;
			default:
			    // fallthrough because CANCEL and INTERRUPTED are already handled
			    break;
		    }
		} else {
		    // step replacement did not happen, so we can execute the action
		    StepAction action = next.getStep().getAction();
		    StepActionCallable actionCallable = new StepActionCallable(action, oldResults, next);
		    // use separate thread or tasks running outside the JVM context, like PCSC calls, won't stop on cancellation
		    ExecutorService execService = Executors.newSingleThreadExecutor();
		    Future<StepActionResult> actionFuture = execService.submit(actionCallable);
		    navigator.setRunningAction(actionFuture);
		    StepActionResult actionResult;
		    try {
			actionResult = actionFuture.get();
			LOG.debug("Step Action {} finished with result {}.", action.getStepID(), actionResult.getStatus());
		    } catch (CancellationException ex) {
			LOG.info("StepAction was canceled.", ex);
			return ResultStatus.CANCEL;
		    } catch (InterruptedException ex) {
			LOG.info("StepAction was interrupted.", ex);
			navigator.close();
			throw new ThreadTerminateException("GUI has been interrupted.");
		    } catch (ExecutionException ex) {
			// there are some special kinds we need to handle here
			if (ex.getCause() instanceof InvocationTargetExceptionUnchecked) {
			    InvocationTargetExceptionUnchecked iex = (InvocationTargetExceptionUnchecked) ex.getCause();
			    if (iex.getCause() instanceof ThreadTerminateException) {
				LOG.info("StepAction was interrupted.", ex);
				throw new ThreadTerminateException("GUI has been interrupted.");
			    }
			}
			// all other types
			LOG.error("StepAction failed with error.", ex.getCause());
			return ResultStatus.CANCEL;
		    }

		    // break out if cancel was returned
		    if (actionResult.getStatus() == StepActionResultStatus.CANCEL) {
			LOG.info("StepAction was canceled.");
			return ResultStatus.CANCEL;
		    }

		    // replace step if told by result value
		    Step actionReplace = actionResult.getReplacement();
		    if (actionReplace != null) {
			LOG.debug("Replacing after action with step.id={}.", actionReplace.getID());
			switch (actionResult.getStatus()) {
			    case BACK:
				next = navigator.replacePrevious(actionReplace);
				break;
			    case NEXT:
				if (navigator.hasNext()) {
				    next = navigator.replaceNext(actionReplace);
				} else {
				    return convertStatus(StepActionResultStatus.NEXT);
				}
				break;
			    case REPEAT:
				next = navigator.replaceCurrent(actionReplace);
				break;
			    default:
				// fallthrough because CANCEL is already handled
				break;
			}
		    } else {
			// no replacement just proceed
			switch (actionResult.getStatus()) {
			    case BACK:
				next = navigator.previous();
				break;
			    case NEXT:
				if (navigator.hasNext()) {
				    next = navigator.next();
				} else {
				    return convertStatus(StepActionResultStatus.NEXT);
				}
				break;
			    case REPEAT:
				next = navigator.current();
				break;
			    default:
				// fallthrough because CANCEL is already handled
				break;
			}
		    }
		}
	    }
	} finally {
	    LOG.debug("Closing UserConsentNavigator.");
	    navigator.close();
	}
    }

    /**
     * Get all step results of the execution.
     *
     * @return Mapping of the step results with step ID as key.
     */
    public Map<String, ExecutionResults> getResults() {
	return Collections.unmodifiableMap(results);
    }

    private ResultStatus convertStatus(StepActionResultStatus in) {
	switch (in) {
	    case BACK:
		return ResultStatus.BACK;
	    case NEXT:
		return ResultStatus.OK;
	    default:
		return ResultStatus.OK; // repeat undefined for this kind of status
	}
    }

}
